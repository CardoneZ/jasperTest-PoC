package com.example.jasperdemo.service;

import com.example.jasperdemo.dto.*;
import com.example.jasperdemo.repository.AttendanceRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);

    private final ZktecoExcelParserService parserService;
    private final AttendanceRepository attendanceRepository;

    public AttendanceService(ZktecoExcelParserService parserService, AttendanceRepository attendanceRepository) {
        this.parserService = parserService;
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * Paso 1 y 2: Carga, procesamiento, limpieza y validación con preview interactivo.
     */
    public AttendancePreviewResponse processExcelFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo seleccionado está vacío.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".xlsx") && !originalFilename.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("Formato no soportado. Debe ser un archivo Excel (.xlsx o .xls) exportado por el checador ZKTeco.");
        }

        try (InputStream is = file.getInputStream()) {
            AttendancePreviewResponse preview = parserService.parseZktecoExcel(is, originalFilename);

            // Verificar si algún registro ya existe previamente en la BD para advertir al usuario en el preview
            for (AttendanceRecordDto dto : preview.getRecords()) {
                if (dto.getUserId() != null && dto.getTimestamp() != null) {
                    try {
                        LocalDateTime ts = parseFlexibleDateTime(dto.getTimestamp());
                        if (ts != null && attendanceRepository.existsPunch(dto.getUserId(), ts)) {
                            dto.addValidationError("Registro ya almacenado previamente en BD (se ignorará duplicado al confirmar).");
                            if ("VALID".equals(dto.getStatus())) {
                                dto.setStatus("WARNING");
                                preview.setValidRows(preview.getValidRows() - 1);
                                preview.setWarningRows(preview.getWarningRows() + 1);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            return preview;
        }
    }

    /**
     * Paso 3: Confirmación y almacenamiento persistente en la base de datos.
     */
    public AttendanceConfirmResponse confirmAndSave(AttendanceConfirmRequest request) {
        if (request == null || request.getRecords() == null || request.getRecords().isEmpty()) {
            return new AttendanceConfirmResponse(
                    request != null ? request.getBatchId() : "",
                    0, 0, 0, 0,
                    "No se recibieron registros para confirmar."
            );
        }

        // Filtrar registros que no sean inválidos (solo VALID o WARNING)
        List<AttendanceRecordDto> validRecords = request.getRecords().stream()
                .filter(r -> !"INVALID".equalsIgnoreCase(r.getStatus()))
                .filter(r -> r.getUserId() != null && r.getTimestamp() != null)
                .collect(Collectors.toList());

        int totalSubmitted = request.getRecords().size();
        int rejectedInvalid = totalSubmitted - validRecords.size();

        if (validRecords.isEmpty()) {
            return new AttendanceConfirmResponse(
                    request.getBatchId(),
                    totalSubmitted,
                    0,
                    0,
                    rejectedInvalid,
                    "Todos los registros enviados presentan inconsistencias o son inválidos."
            );
        }

        String batchId = request.getBatchId() != null ? request.getBatchId() : UUID.randomUUID().toString();
        String fileName = request.getFileName() != null ? request.getFileName() : "importacion_checador.xlsx";

        Map<String, Integer> insertStats = attendanceRepository.batchInsert(validRecords, fileName, batchId);
        int inserted = insertStats.getOrDefault("inserted", 0);
        int duplicates = insertStats.getOrDefault("duplicates", 0);

        String message = String.format("Importación completada: %d registros insertados, %d duplicados omitidos, %d registros inválidos rechazados.",
                inserted, duplicates, rejectedInvalid);

        return new AttendanceConfirmResponse(batchId, totalSubmitted, inserted, duplicates, rejectedInvalid, message);
    }

    /**
     * Consulta registros almacenados en BD.
     */
    public List<Map<String, Object>> getStoredRecords(String startDate, String endDate, Integer userId, String search, Integer limit) {
        return attendanceRepository.getStoredRecords(startDate, endDate, userId, search, limit);
    }

    /**
     * Métricas y estadísticas para el dashboard.
     */
    public AttendanceStatsDto getStats() {
        return attendanceRepository.getStats();
    }

    /**
     * Genera un archivo Excel de ejemplo auténtico con estructura de ZKTeco LX50 para pruebas inmediatas.
     */
    public byte[] generateSampleZktecoExcel() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Registro de Asistencia");

            // Estilos
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 12);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Fila 0: Título exportado por LX50
            Row r0 = sheet.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("Reporte de Asistencia - Terminal Biométrica ZKTeco LX50");
            c0.setCellStyle(titleStyle);

            // Fila 1: Metadatos de periodo y dispositivo
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Periodo: 01/09/2026 ~ 30/09/2026 | Terminal: LX50_RH_PRINCIPAL");

            // Fila 2: Espacio en blanco (común en SSR de ZKTeco)
            sheet.createRow(2);

            // Fila 3: Encabezados de tabla de checadas
            Row r3 = sheet.createRow(3);
            String[] headers = {
                    "No. AC", "Nombre", "Tiempo", "Estado", "Nuevo Estado", "Excepción", "Operación", "Dispositivo"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell ch = r3.createCell(i);
                ch.setCellValue(headers[i]);
                ch.setCellStyle(headerStyle);
            }

            // Datos de prueba con casos válidos y casos extremos para probar validación
            Object[][] data = {
                    // Normales válidos
                    {101, "Carlos Mendoza Flores", "15/09/2026 07:58", "Entrada", "0", "", "Huella", "LX50"},
                    {101, "Carlos Mendoza Flores", "15/09/2026 14:02", "Salida Almuerzo", "0", "", "Huella", "LX50"},
                    {101, "Carlos Mendoza Flores", "15/09/2026 15:01", "Regreso Almuerzo", "0", "", "Huella", "LX50"},
                    {101, "Carlos Mendoza Flores", "15/09/2026 18:05", "Salida", "0", "", "Huella", "LX50"},

                    {102, "Mariana Ruiz Galindo", "15/09/2026 08:04", "Entrada", "0", "", "Huella", "LX50"},
                    {102, "Mariana Ruiz Galindo", "15/09/2026 17:30", "Salida", "0", "", "Huella", "LX50"},

                    {103, "Jorge Luis Hernandez", "15/09/2026 08:15", "Entrada", "0", "", "Contraseña", "LX50"},
                    {103, "Jorge Luis Hernandez", "15/09/2026 18:00", "Salida", "0", "", "Contraseña", "LX50"},

                    {104, "Laura Patricia Ortiz", "15/09/2026 08:00", "Entrada", "0", "", "Tarjeta", "LX50"},
                    {104, "Laura Patricia Ortiz", "15/09/2026 18:10", "Salida", "0", "", "Tarjeta", "LX50"},

                    // Empleado sin nombre registrado en el reloj (muy común en ZKTeco)
                    {105, "", "15/09/2026 08:02", "Entrada", "0", "", "Huella", "LX50"},
                    {105, "", "15/09/2026 17:59", "Salida", "0", "", "Huella", "LX50"},

                    // Caso de doble checada consecutiva (< 1 min) -> Generará WARNING
                    {106, "Roberto Sanchez Peña", "15/09/2026 07:55", "Entrada", "0", "", "Huella", "LX50"},
                    {106, "Roberto Sanchez Peña", "15/09/2026 07:55", "Entrada", "0", "", "Huella", "LX50"}, // Doble toque
                    {106, "Roberto Sanchez Peña", "15/09/2026 17:02", "Salida", "0", "", "Huella", "LX50"},

                    // Caso inválido: ID de usuario vacío -> Generará INVALID
                    {"", "Empleado Fantasma", "15/09/2026 08:30", "Entrada", "0", "", "Huella", "LX50"},

                    // Caso inválido: Fecha corrupta -> Generará INVALID
                    {107, "Beatriz Domínguez", "FECHA_INVALIDA", "Entrada", "0", "", "Huella", "LX50"}
            };

            int rowIdx = 4;
            for (Object[] rowData : data) {
                Row r = sheet.createRow(rowIdx++);
                for (int c = 0; c < rowData.length; c++) {
                    Cell cell = r.createCell(c);
                    Object val = rowData[c];
                    if (val instanceof Integer) {
                        cell.setCellValue((Integer) val);
                    } else {
                        cell.setCellValue(val != null ? val.toString() : "");
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(baos);
            return baos.toByteArray();
        }
    }

    public static LocalDateTime parseFlexibleDateTime(String str) {
        if (str == null || str.isBlank()) return null;
        String trimmed = str.trim();
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        );
        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDateTime.parse(trimmed, fmt);
            } catch (Exception ignored) {}
        }
        try {
            return LocalDateTime.parse(trimmed);
        } catch (Exception ignored) {}
        return null;
    }
}
