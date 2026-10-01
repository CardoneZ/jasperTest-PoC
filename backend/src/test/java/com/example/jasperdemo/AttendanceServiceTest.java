package com.example.jasperdemo;

import com.example.jasperdemo.dto.AttendancePreviewResponse;
import com.example.jasperdemo.dto.AttendanceRecordDto;
import com.example.jasperdemo.service.AttendanceService;
import com.example.jasperdemo.service.ZktecoExcelParserService;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class AttendanceServiceTest {

    @Test
    public void testGenerateAndParseSampleExcel() throws Exception {
        AttendanceService attendanceService = new AttendanceService(new ZktecoExcelParserService(), null);
        ZktecoExcelParserService parserService = new ZktecoExcelParserService();

        // 1. Generar archivo Excel de prueba con formato de ZKTeco LX50
        byte[] excelBytes = attendanceService.generateSampleZktecoExcel();
        Assertions.assertNotNull(excelBytes);
        Assertions.assertTrue(excelBytes.length > 0);

        // 2. Guardar en carpeta samples/ para uso del usuario
        File samplesDir = new File("../samples");
        if (!samplesDir.exists()) {
            samplesDir.mkdirs();
        }
        File sampleFile = new File(samplesDir, "ZKTeco_LX50_Asistencia_Ejemplo.xlsx");
        try (FileOutputStream fos = new FileOutputStream(sampleFile)) {
            fos.write(excelBytes);
        }
        Assertions.assertTrue(sampleFile.exists());

        // 3. Procesar y limpiar con el servicio parser
        try (ByteArrayInputStream bais = new ByteArrayInputStream(excelBytes)) {
            AttendancePreviewResponse preview = parserService.parseZktecoExcel(bais, "ZKTeco_LX50_Asistencia_Ejemplo.xlsx");

            Assertions.assertEquals(17, preview.getTotalRows());
            Assertions.assertEquals(13, preview.getValidRows());
            Assertions.assertEquals(3, preview.getWarningRows());
            Assertions.assertEquals(1, preview.getInvalidRows());
            Assertions.assertEquals(17, preview.getRecords().size());
        }
    }

    @Test
    public void testRealStandardReportZktecoExcel() throws Exception {
        File file = new File("C:/Users/wero1/Downloads/2_StandardReport.xls");
        if (!file.exists()) {
            System.out.println("2_StandardReport.xls no encontrado en Downloads, omitiendo test de integración local.");
            return;
        }

        byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
        ZktecoExcelParserService parserService = new ZktecoExcelParserService();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
            AttendancePreviewResponse preview = parserService.parseZktecoExcel(bais, "2_StandardReport.xls");

            for (AttendanceRecordDto r : preview.getRecords()) {
                System.out.printf("Row %2d | ID: %2d | Nombre: %-18s | Depto: %-14s | Horas: %-6s | Retardos: %4d min | SalidasTemp: %4d min | Dias: %-5s | Faltas: %2d | Permisos: %2d%n",
                        r.getRowNumber(), r.getUserId(), r.getEmployeeName(), r.getDepartment(), r.getRealHours(), r.getLateMinutes(), r.getEarlyExitMinutes(), r.getAttendedDays(), r.getAbsenceDays(), r.getLeaveDays());
            }

            // Validar primer empleado Alberto Hernandez
            AttendanceRecordDto rec1 = preview.getRecords().get(0);
            Assertions.assertEquals("Alberto Hernandez", rec1.getEmployeeName());
            Assertions.assertEquals("Direccion", rec1.getDepartment());
            Assertions.assertEquals("0:00", rec1.getRealHours());
            Assertions.assertEquals(0, rec1.getLateMinutes());
            Assertions.assertEquals("20/0", rec1.getAttendedDays());
            Assertions.assertEquals(20, rec1.getAbsenceDays());

            // Validar empleado Christian (con checadas reales)
            AttendanceRecordDto recChristian = preview.getRecords().stream()
                    .filter(r -> r.getUserId() != null && r.getUserId() == 36)
                    .findFirst().orElseThrow();
            Assertions.assertEquals("Christian", recChristian.getEmployeeName());
            Assertions.assertEquals("Empresa", recChristian.getDepartment());
            Assertions.assertEquals("162:44", recChristian.getRealHours());
            Assertions.assertEquals(50, recChristian.getLateMinutes());
            Assertions.assertEquals(6, recChristian.getLateCount());
            Assertions.assertEquals(3146, recChristian.getEarlyExitMinutes());
            Assertions.assertEquals(18, recChristian.getEarlyExitCount());
            Assertions.assertEquals("20/19", recChristian.getAttendedDays());
            Assertions.assertEquals(1, recChristian.getAbsenceDays());
            Assertions.assertEquals(0, recChristian.getLeaveDays());

            Assertions.assertEquals(10, preview.getTotalRows(), "Deben ser exactamente los 10 empleados reales");
            Assertions.assertEquals(10, preview.getValidRows(), "Todos los 10 empleados deben ser válidos con fecha del periodo");
            Assertions.assertEquals(0, preview.getInvalidRows(), "No deben haber filas inválidas");
        }
    }
}
