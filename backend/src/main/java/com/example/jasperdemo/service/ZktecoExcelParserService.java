package com.example.jasperdemo.service;

import com.example.jasperdemo.dto.AttendancePreviewResponse;
import com.example.jasperdemo.dto.AttendanceRecordDto;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ZktecoExcelParserService {

    private static final Logger log = LoggerFactory.getLogger(ZktecoExcelParserService.class);

    private static final Pattern PERIOD_PATTERN = Pattern.compile(
            "(\\d{1,4}[-/.]\\d{1,2}[-/.]\\d{2,4})\\s*[-~aA]+\\s*(\\d{1,4}[-/.]\\d{1,2}[-/.]\\d{2,4})"
    );

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("M/d/yy HH:mm"),
            DateTimeFormatter.ofPattern("M/d/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("M/d/yyyy HH:mm")
    );

    private static final List<DateTimeFormatter> DATE_ONLY_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yy")
    );

    private static final List<DateTimeFormatter> TIME_ONLY_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm")
    );

    /**
     * Procesa, limpia y normaliza archivos Excel exportados por checadores ZKTeco LX50.
     * Soporta tanto reportes de checadas individuales como reportes estadísticos SSR (2_StandardReport.xls).
     */
    public AttendancePreviewResponse parseZktecoExcel(InputStream inputStream, String fileName) throws Exception {
        AttendancePreviewResponse response = new AttendancePreviewResponse();
        response.setFileName(fileName);
        response.setBatchId(UUID.randomUUID().toString());

        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = selectBestSheet(workbook);

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();

            if (lastRowNum < 0 || lastRowNum < firstRowNum) {
                throw new IllegalArgumentException("La hoja de cálculo de Excel está vacía.");
            }

            // 1. Extraer periodo global si existe en encabezados (ej. "Periodo: 2026-09-01 ~ 2026-09-28")
            String[] periodDates = extractGlobalPeriodDates(sheet, firstRowNum, Math.min(lastRowNum, firstRowNum + 6));
            String globalEndDate = periodDates != null ? periodDates[1] : null;
            String globalStartDate = periodDates != null ? periodDates[0] : null;

            log.info("Hoja seleccionada: '{}' | Periodo detectado: {} a {}", 
                    sheet.getSheetName(), globalStartDate, globalEndDate);

            // 2. Detectar fila de encabezados dinámicamente
            int headerRowIndex = findHeaderRow(sheet, firstRowNum, Math.min(lastRowNum, firstRowNum + 20));
            if (headerRowIndex == -1) {
                headerRowIndex = firstRowNum;
            }

            Row headerRow = sheet.getRow(headerRowIndex);
            Map<String, Integer> colMap = detectColumns(headerRow);
            boolean isStatisticalSheet = isStatisticalReportSheet(sheet.getSheetName(), headerRow);

            log.info("Encabezados detectados en fila {}: {} (Es reporte estadístico: {})", 
                    headerRowIndex + 1, colMap, isStatisticalSheet);

            List<AttendanceRecordDto> records = new ArrayList<>();
            int totalProcessed = 0;
            int validCount = 0;
            int warningCount = 0;
            int invalidCount = 0;

            Map<Integer, LocalDateTime> lastPunchMap = new HashMap<>();
            int visualRowNumber = 1;

            for (int r = headerRowIndex + 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                // Omitir sub-encabezados decorativos (ej. filas con "Normal", "Real", "Cantidad", "Minuto", "Lun", "Mar", etc.)
                if (isSubHeaderRow(row)) {
                    log.debug("Omitiendo fila de sub-encabezado {}", r);
                    continue;
                }

                // A. ID de Usuario (No. AC / User ID)
                Integer userId = extractUserId(row, colMap);
                if (userId == null || userId <= 0) {
                    // Si la fila no tiene ID numérico y no tiene nombre, ignorar (filas de formato/espaciado)
                    String rawName = extractEmployeeName(row, colMap);
                    if (rawName == null || rawName.isBlank()) {
                        continue;
                    }
                }

                AttendanceRecordDto dto = new AttendanceRecordDto();
                dto.setRowNumber(visualRowNumber++);
                totalProcessed++;
                dto.setUserId(userId);

                if (userId == null || userId <= 0) {
                    dto.addValidationError("ID de usuario (No. AC) vacío o no numérico.");
                }

                // B. Nombre del Empleado y Departamento
                String employeeName = extractEmployeeName(row, colMap);
                dto.setEmployeeName(employeeName);
                if (employeeName == null || employeeName.isBlank() || employeeName.equalsIgnoreCase("Sin Nombre")) {
                    dto.setEmployeeName("Sin nombre registrado");
                    dto.addValidationError("Empleado sin nombre configurado en la terminal.");
                }

                // C. Fecha y Hora de la checada
                LocalDateTime punchTime = extractPunchDateTime(row, colMap);

                // Si la fila no tiene fecha explícita pero hay un Periodo detectado en el encabezado (Reporte Estadístico ZKTeco)
                if (punchTime == null && globalEndDate != null) {
                    try {
                        LocalDate pDate = parseDateString(globalEndDate);
                        if (pDate != null) {
                            punchTime = LocalDateTime.of(pDate, LocalTime.of(0, 0, 0));
                        }
                    } catch (Exception ignored) {}
                }

                if (punchTime == null) {
                    dto.addValidationError("Fecha u hora de checada no válida o ausente.");
                } else {
                    dto.setTimestamp(punchTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    dto.setDate(punchTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    dto.setTime(punchTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

                    // Validación: Fecha en el futuro (> 1 día de hoy)
                    if (punchTime.isAfter(LocalDateTime.now().plusDays(1))) {
                        dto.addValidationError("Fecha/hora posterior a la actual (verificar reloj de terminal).");
                    }

                    // Validación: Checada duplicada consecutiva en menos de 60 segundos
                    if (userId != null && !isStatisticalSheet) {
                        LocalDateTime prev = lastPunchMap.get(userId);
                        if (prev != null) {
                            long secondsBetween = Math.abs(java.time.Duration.between(prev, punchTime).getSeconds());
                            if (secondsBetween <= 60) {
                                dto.addValidationError("Posible doble marcaje consecutivo (" + secondsBetween + "s de diferencia).");
                            }
                        }
                        lastPunchMap.put(userId, punchTime);
                    }
                }

                // D. Tipo de Checada (Entrada, Salida, etc.)
                String rawType = extractString(row, colMap.get("type"));
                if (isStatisticalSheet) {
                    dto.setPunchType("ASISTENCIA");
                } else {
                    dto.setPunchType(normalizePunchType(rawType, punchTime));
                }

                // E. Modo de Verificación (Huella, Contraseña, etc.)
                String rawMode = extractString(row, colMap.get("mode"));
                dto.setVerificationMode(normalizeVerificationMode(rawMode));

                // F. Dispositivo
                String rawDevice = extractString(row, colMap.get("device"));
                dto.setDeviceId(rawDevice != null && !rawDevice.isBlank() ? rawDevice.trim() : "LX50");

                // G. Campos estadísticos enriquecidos si es reporte estadístico
                if (isStatisticalSheet) {
                    String dept = extractString(row, colMap.getOrDefault("dept", 2));
                    String normH = extractString(row, 3);
                    String realH = extractString(row, 4);
                    Integer lateCnt = extractInteger(row, 5);
                    Integer lateMin = extractInteger(row, 6);
                    Integer earlyCnt = extractInteger(row, 7);
                    Integer earlyMin = extractInteger(row, 8);
                    String attDays = extractString(row, 11);
                    Integer absDays = extractInteger(row, 13);
                    Integer lveDays = extractInteger(row, 14);

                    dto.setDepartment(dept != null && !dept.isBlank() ? dept.trim() : "General");
                    dto.setNormalHours(normH != null && !normH.isBlank() ? normH.trim() : "240:00");
                    dto.setRealHours(realH != null && !realH.isBlank() ? realH.trim() : "0:00");
                    dto.setLateCount(lateCnt != null ? lateCnt : 0);
                    dto.setLateMinutes(lateMin != null ? lateMin : 0);
                    dto.setEarlyExitCount(earlyCnt != null ? earlyCnt : 0);
                    dto.setEarlyExitMinutes(earlyMin != null ? earlyMin : 0);
                    dto.setAttendedDays(attDays != null && !attDays.isBlank() ? attDays.trim() : "-");
                    dto.setAbsenceDays(absDays != null ? absDays : 0);
                    dto.setLeaveDays(lveDays != null ? lveDays : 0);
                    if (globalStartDate != null && globalEndDate != null) {
                        dto.setPeriod(globalStartDate + " ~ " + globalEndDate);
                    }
                } else {
                    String dept = extractString(row, colMap.get("dept"));
                    dto.setDepartment(dept != null && !dept.isBlank() ? dept.trim() : "General");
                    dto.setNormalHours("240:00");
                    dto.setRealHours("-");
                    dto.setAttendedDays("1/1");
                    if (globalStartDate != null && globalEndDate != null) {
                        dto.setPeriod(globalStartDate + " ~ " + globalEndDate);
                    }
                }

                // H. Determinar Estatus General del Registro
                if (dto.getUserId() == null || dto.getTimestamp() == null) {
                    dto.setStatus("INVALID");
                    invalidCount++;
                } else if (!dto.getValidationErrors().isEmpty()) {
                    dto.setStatus("WARNING");
                    warningCount++;
                } else {
                    dto.setStatus("VALID");
                    validCount++;
                }

                records.add(dto);
            }

            response.setTotalRows(totalProcessed);
            response.setValidRows(validCount);
            response.setWarningRows(warningCount);
            response.setInvalidRows(invalidCount);
            response.setRecords(records);

            return response;
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    /**
     * Selecciona la hoja más adecuada para importar de un libro de ZKTeco LX50.
     * Prioriza "Reporte Estadístico", "Reporte de Asistencia" o "Reporte de Excepciones".
     */
    private Sheet selectBestSheet(Workbook wb) {
        int numSheets = wb.getNumberOfSheets();
        if (numSheets == 1) {
            return wb.getSheetAt(0);
        }

        // Buscar hoja de Reporte Estadístico (formato preferido en ZKTeco LX50 USB SSR)
        for (int i = 0; i < numSheets; i++) {
            String name = wb.getSheetName(i).toLowerCase();
            if (name.contains("estad") || name.contains("statistic") || name.contains("resumen")) {
                return wb.getSheetAt(i);
            }
        }

        // Buscar hoja de Asistencia o Excepciones
        for (int i = 0; i < numSheets; i++) {
            String name = wb.getSheetName(i).toLowerCase();
            if (name.contains("asistencia") || name.contains("evento") || name.contains("excep") || name.contains("attlog")) {
                return wb.getSheetAt(i);
            }
        }

        return wb.getSheetAt(0);
    }

    /**
     * Extrae las fechas de inicio y fin del periodo desde encabezados como:
     * "Periodo: 2026-09-01 ~ 2026-09-28"
     */
    private String[] extractGlobalPeriodDates(Sheet sheet, int startRow, int endRow) {
        for (int r = startRow; r <= endRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell == null) continue;
                String text = cell.toString().trim();
                Matcher m = PERIOD_PATTERN.matcher(text);
                if (m.find()) {
                    LocalDate start = parseDateString(m.group(1));
                    LocalDate end = parseDateString(m.group(2));
                    String sStr = start != null ? start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : m.group(1).replace('/', '-');
                    String eStr = end != null ? end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : m.group(2).replace('/', '-');
                    return new String[]{ sStr, eStr };
                }
            }
        }
        return null;
    }

    /**
     * Identifica si la fila actual es un sub-encabezado (como las filas que desglosan "Normal", "Real", "Cantidad", etc.)
     */
    private boolean isSubHeaderRow(Row row) {
        if (row == null) return false;
        int subHeaderKeywords = 0;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell == null) continue;
            String text = cell.toString().toLowerCase().trim();
            if (text.equals("normal") || text.equals("real") || text.equals("cantidad") 
                    || text.equals("minuto") || text.equals("mar") || text.equals("mié") 
                    || text.equals("mie") || text.equals("jue") || text.equals("vie") 
                    || text.equals("sáb") || text.equals("sab") || text.equals("dom") 
                    || text.equals("lun") || text.contains("laborales") || text.contains("festivos")) {
                subHeaderKeywords++;
            }
        }
        return subHeaderKeywords >= 2;
    }

    private boolean isStatisticalReportSheet(String sheetName, Row headerRow) {
        if (sheetName != null && (sheetName.toLowerCase().contains("estad") || sheetName.toLowerCase().contains("statistic"))) {
            return true;
        }
        if (headerRow != null) {
            String fullRowText = headerRow.toString().toLowerCase();
            return fullRowText.contains("retardos") || fullRowText.contains("laborales") || fullRowText.contains("salida temprano");
        }
        return false;
    }

    /**
     * Busca la fila de encabezados analizando coincidencias de palabras clave de ZKTeco LX50.
     */
    private int findHeaderRow(Sheet sheet, int startRow, int maxRow) {
        for (int r = startRow; r <= maxRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            int matchCount = 0;
            for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell == null) continue;
                String text = cell.toString().toLowerCase().trim();

                if (text.contains("no. ac") || text.contains("ac-no") || text.contains("ac no") 
                        || text.equals("id") || text.equals("id:") || text.contains("usuario") || text.contains("no.")) {
                    matchCount++;
                }
                if (text.contains("tiempo") || text.contains("fecha") || text.contains("date") 
                        || text.contains("hora") || text.contains("time") || text.contains("nombre") 
                        || text.contains("departamento") || text.contains("laborales")
                        || text.contains("estado") || text.contains("state")) {
                    matchCount++;
                }
            }

            if (matchCount >= 2) {
                return r;
            }
        }
        return -1;
    }

    /**
     * Mapea nombres de columnas a índices en la hoja.
     */
    private Map<String, Integer> detectColumns(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) return map;

        for (int c = headerRow.getFirstCellNum(); c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            if (cell == null) continue;
            String text = cell.toString().toLowerCase().trim();

            if ((text.contains("no. ac") || text.contains("ac-no") || text.contains("ac no") 
                    || text.equals("id") || text.equals("no") || text.equals("no.") || text.contains("usuario") || text.contains("user id"))
                    && !map.containsKey("userId")) {
                map.put("userId", c);
            } else if ((text.contains("nombre") || text.contains("name") || text.contains("empleado")) 
                    && !map.containsKey("name")) {
                map.put("name", c);
            } else if ((text.contains("departamento") || text.contains("depto") || text.contains("dept")) 
                    && !map.containsKey("dept")) {
                map.put("dept", c);
            } else if ((text.contains("tiempo") || text.contains("fecha/hora") || text.contains("datetime") || text.contains("fecha y hora"))
                    && !map.containsKey("timestamp")) {
                map.put("timestamp", c);
            } else if ((text.equals("fecha") || text.equals("date") || text.contains("fecha checada"))
                    && !map.containsKey("date")) {
                map.put("date", c);
            } else if ((text.equals("hora") || text.equals("time") || text.contains("hora checada"))
                    && !map.containsKey("time")) {
                map.put("time", c);
            } else if ((text.contains("estado") || text.contains("state") || text.contains("tipo") || text.contains("evento"))
                    && !map.containsKey("type")) {
                map.put("type", c);
            } else if ((text.contains("operaci") || text.contains("sensor") || text.contains("verificaci") || text.contains("modo"))
                    && !map.containsKey("mode")) {
                map.put("mode", c);
            } else if ((text.contains("dispositivo") || text.contains("device") || text.contains("terminal") || text.contains("reloj"))
                    && !map.containsKey("device")) {
                map.put("device", c);
            }
        }

        if (!map.containsKey("userId")) map.put("userId", 0);
        if (!map.containsKey("name")) map.put("name", 1);
        if (!map.containsKey("dept")) map.put("dept", 2);

        return map;
    }

    private Integer extractUserId(Row row, Map<String, Integer> colMap) {
        Integer colIdx = colMap.get("userId");
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) Math.round(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().trim();
                if (val.contains(".")) {
                    val = val.substring(0, val.indexOf('.'));
                }
                val = val.replaceAll("[^0-9]", "");
                return val.isEmpty() ? null : Integer.parseInt(val);
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer userId de celda: {}", cell);
        }
        return null;
    }

    private String extractEmployeeName(Row row, Map<String, Integer> colMap) {
        Integer colIdx = colMap.get("name");
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;

        String val = cell.toString().trim();
        if (val.isEmpty() || val.equalsIgnoreCase("null")) {
            return null;
        }
        return val.replaceAll("[\"']", "").replaceAll("\\s+", " ").trim();
    }

    private LocalDateTime extractPunchDateTime(Row row, Map<String, Integer> colMap) {
        // Opción 1: Columna única Timestamp
        Integer tsCol = colMap.get("timestamp");
        if (tsCol != null) {
            Cell cell = row.getCell(tsCol);
            if (cell != null) {
                LocalDateTime dt = parseDateTimeCell(cell);
                if (dt != null) return dt;
            }
        }

        // Opción 2: Columnas separadas de Fecha y Hora
        Integer dateCol = colMap.get("date");
        Integer timeCol = colMap.get("time");
        if (dateCol != null) {
            Cell cellDate = row.getCell(dateCol);
            LocalDate datePart = parseDateCell(cellDate);
            if (datePart != null) {
                LocalTime timePart = timeCol != null ? parseTimeCell(row.getCell(timeCol)) : null;
                if (timePart == null) {
                    timePart = LocalTime.of(0, 0, 0);
                }
                return LocalDateTime.of(datePart, timePart);
            }
        }

        return null;
    }

    private LocalDateTime parseDateTimeCell(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                Date d = cell.getDateCellValue();
                return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            }
        }

        String text = cell.toString().trim();
        for (DateTimeFormatter fmt : DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(text, fmt);
            } catch (DateTimeParseException ignored) {}
        }

        // Intentar parsear solo fecha
        LocalDate d = parseDateString(text);
        if (d != null) {
            return LocalDateTime.of(d, LocalTime.of(0, 0, 0));
        }

        return null;
    }

    private LocalDate parseDateCell(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date d = cell.getDateCellValue();
            return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        return parseDateString(cell.toString().trim());
    }

    private LocalDate parseDateString(String text) {
        if (text == null || text.isBlank()) return null;
        text = text.trim().replace('/', '-');
        for (DateTimeFormatter fmt : DATE_ONLY_FORMATTERS) {
            try {
                return LocalDate.parse(text, fmt);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    private LocalTime parseTimeCell(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date d = cell.getDateCellValue();
            return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
        }
        String text = cell.toString().trim();
        for (DateTimeFormatter fmt : TIME_ONLY_FORMATTERS) {
            try {
                return LocalTime.parse(text, fmt);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    private String normalizePunchType(String raw, LocalDateTime punchTime) {
        if (raw == null || raw.isBlank()) {
            if (punchTime != null) {
                int hour = punchTime.getHour();
                return (hour < 12) ? "ENTRADA" : "SALIDA";
            }
            return "REGISTRO";
        }

        String s = raw.toLowerCase().trim();
        if (s.equals("0") || s.contains("entrada") || s.contains("check-in") || s.contains("c/in") || s.equals("ent.") || s.equals("in")) {
            return "ENTRADA";
        }
        if (s.equals("1") || s.contains("salida") || s.contains("check-out") || s.contains("c/out") || s.equals("sal.") || s.equals("out")) {
            return "SALIDA";
        }
        if (s.equals("2") || s.contains("salida almuerzo") || s.contains("break-out") || s.contains("comida sal")) {
            return "SALIDA_INTERMEDIA";
        }
        if (s.equals("3") || s.contains("regreso almuerzo") || s.contains("break-in") || s.contains("comida ent")) {
            return "ENTRADA_INTERMEDIA";
        }
        if (s.equals("4") || s.contains("ot-in") || s.contains("extra in")) {
            return "ENTRADA_EXTRA";
        }
        if (s.equals("5") || s.contains("ot-out") || s.contains("extra out")) {
            return "SALIDA_EXTRA";
        }

        return "REGISTRO";
    }

    private String normalizeVerificationMode(String raw) {
        if (raw == null || raw.isBlank()) {
            return "HUELLA";
        }
        String s = raw.toLowerCase().trim();
        if (s.equals("1") || s.contains("huella") || s.contains("fp") || s.contains("finger")) {
            return "HUELLA";
        }
        if (s.equals("2") || s.contains("contrase") || s.contains("password") || s.contains("pw") || s.contains("pin") || s.contains("clave")) {
            return "CONTRASENA";
        }
        if (s.equals("3") || s.contains("tarjeta") || s.contains("card") || s.contains("rfid")) {
            return "TARJETA";
        }
        if (s.contains("rostro") || s.contains("face") || s.contains("facial")) {
            return "FACIAL";
        }
        return "HUELLA";
    }

    private String extractString(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            double d = cell.getNumericCellValue();
            if (d == (long) d) {
                return String.format("%d", (long) d);
            } else {
                return String.valueOf(d);
            }
        }
        return cell.toString().trim();
    }

    private Integer extractInteger(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) Math.round(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().trim();
                if (val.contains(".")) {
                    val = val.substring(0, val.indexOf('.'));
                }
                val = val.replaceAll("[^0-9-]", "");
                return val.isEmpty() ? null : Integer.parseInt(val);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
