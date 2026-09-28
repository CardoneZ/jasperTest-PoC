package com.example.jasperdemo;

import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RealFileInspectorTest {
    @Test
    public void testParseStatisticalReport() throws Exception {
        File file = new File("C:/Users/wero1/Downloads/2_StandardReport.xls");
        if (!file.exists()) {
            System.out.println("2_StandardReport.xls not found, skipping inspection test.");
            return;
        }
        try (Workbook wb = WorkbookFactory.create(new FileInputStream(file))) {
            Sheet sheet = wb.getSheet("Reporte Estadístico");
            if (sheet == null) {
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    if (wb.getSheetName(i).toLowerCase().contains("estad")) {
                        sheet = wb.getSheetAt(i);
                        break;
                    }
                }
            }
            if (sheet == null) sheet = wb.getSheetAt(0);

            System.out.println("Processing Sheet: " + sheet.getSheetName());

            // 1. Extraer periodo de fechas del encabezado
            String periodStr = "";
            String startDate = "";
            String endDate = "";
            Pattern p = Pattern.compile("(\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2})\\s*[-~aA]+\\s*(\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2})");

            for (int r = 0; r <= Math.min(5, sheet.getLastRowNum()); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null) {
                        String text = cell.toString().trim();
                        Matcher m = p.matcher(text);
                        if (m.find()) {
                            startDate = m.group(1);
                            endDate = m.group(2);
                            periodStr = startDate + " a " + endDate;
                            System.out.println(">>> DETECTED PERIOD: " + periodStr + " (start: " + startDate + ", end: " + endDate + ")");
                            break;
                        }
                    }
                }
                if (!periodStr.isEmpty()) break;
            }

            // 2. Detectar filas de datos de empleados
            for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Cell c0 = row.getCell(0);
                if (c0 == null) continue;

                String c0Text = c0.toString().trim();
                // Verificar si es un ID de empleado numérico
                Integer id = null;
                try {
                    if (c0.getCellType() == CellType.NUMERIC) {
                        id = (int) Math.round(c0.getNumericCellValue());
                    } else if (c0Text.matches("^\\d+(\\.0)?$")) {
                        id = Integer.parseInt(c0Text.replace(".0", ""));
                    }
                } catch (Exception ignored) {}

                if (id != null && id > 0) {
                    Cell cName = row.getCell(1);
                    Cell cDept = row.getCell(2);
                    Cell cHoursNormal = row.getCell(3);
                    Cell cHoursReal = row.getCell(4);
                    Cell cDays = row.getCell(11);
                    Cell cFaltas = row.getCell(13);

                    String name = cName != null ? cName.toString().trim() : "Sin Nombre";
                    String dept = cDept != null ? cDept.toString().trim() : "-";
                    String hoursReal = cHoursReal != null ? cHoursReal.toString().trim() : "0:00";
                    String days = cDays != null ? cDays.toString().trim() : "-";
                    String faltas = cFaltas != null ? cFaltas.toString().trim() : "0";

                    System.out.printf("Employee: ID=%d | Name=%-20s | Dept=%-15s | Date=%s | HoursReal=%s | Days=%s | Faltas=%s%n",
                            id, name, dept, endDate, hoursReal, days, faltas);
                }
            }
        }
    }
}
