package com.example.jasperdemo.controller;

import com.example.jasperdemo.dto.*;
import com.example.jasperdemo.repository.AttendanceRepository;
import com.example.jasperdemo.service.AttendanceService;
import com.example.jasperdemo.service.JasperReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;
    private final JasperReportService jasperReportService;

    public AttendanceController(AttendanceService attendanceService,
                                AttendanceRepository attendanceRepository,
                                JasperReportService jasperReportService) {
        this.attendanceService = attendanceService;
        this.attendanceRepository = attendanceRepository;
        this.jasperReportService = jasperReportService;
    }

    /**
     * ETAPA 1 & 2: Carga de archivo Excel del checador, procesamiento y visualización de preview.
     */
    @PostMapping(value = "/upload-preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAndPreview(@RequestParam("file") MultipartFile file) {
        try {
            AttendancePreviewResponse preview = attendanceService.processExcelFile(file);
            return ResponseEntity.ok(preview);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Error procesando el archivo del checador: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    /**
     * ETAPA 3: Confirmación y almacenamiento de los registros en la base de datos.
     */
    @PostMapping("/confirm")
    public ResponseEntity<AttendanceConfirmResponse> confirmImport(@RequestBody AttendanceConfirmRequest request) {
        AttendanceConfirmResponse response = attendanceService.confirmAndSave(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Consulta de registros almacenados en la base de datos (con filtros).
     */
    @GetMapping("/records")
    public ResponseEntity<List<Map<String, Object>>> getStoredRecords(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "200") Integer limit) {

        String isoStart = normalizeToIsoDate(startDate);
        String isoEnd = normalizeToIsoDate(endDate);
        List<Map<String, Object>> records = attendanceService.getStoredRecords(isoStart, isoEnd, userId, search, limit);
        return ResponseEntity.ok(records);
    }

    /**
     * Estadísticas y métricas generales del módulo de asistencia.
     */
    @GetMapping("/stats")
    public ResponseEntity<AttendanceStatsDto> getStats() {
        AttendanceStatsDto stats = attendanceService.getStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Descarga de archivo de prueba auténtico con estructura de ZKTeco LX50.
     */
    @GetMapping("/sample")
    public ResponseEntity<byte[]> downloadSampleFile() {
        try {
            byte[] excelBytes = attendanceService.generateSampleZktecoExcel();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ZKTeco_LX50_Asistencia_Ejemplo.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * INTEGRACIÓN CON JASPERREPORTS: Generación de reporte PDF de asistencia.
     */
    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> generateAttendanceReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false, defaultValue = "inline") String disposition) {

        String isoStart = normalizeToIsoDate(startDate);
        String isoEnd = normalizeToIsoDate(endDate);
        List<Map<String, Object>> data = attendanceRepository.getReportData(isoStart, isoEnd, userId);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("P_REPORT_TITLE", "Reporte Estadístico de Asistencia (Horas, Retardos, Días) - ZKTeco LX50");
        parameters.put("P_GENERATION_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        String filterStart = (startDate != null && !startDate.isBlank()) ? formatDateToDdMmYyyy(startDate) : "Inicio";
        String filterEnd = (endDate != null && !endDate.isBlank()) ? formatDateToDdMmYyyy(endDate) : "Fin";
        parameters.put("P_DATE_FILTER", filterStart + " al " + filterEnd);
        parameters.put("P_USER_FILTER", userId != null && userId > 0 ? "ID Empleado: " + userId : "Todos los Empleados");
        parameters.put("P_USER", "Recursos Humanos - PoC JasperReports");

        byte[] pdf = jasperReportService.generatePdfReport("report_attendance_log", parameters, (List) data);

        String filename = "Reporte_Asistencia_ZKTeco_" + System.currentTimeMillis() + ".pdf";
        String contentDisposition = "attachment".equalsIgnoreCase(disposition)
                ? "attachment; filename=\"" + filename + "\""
                : "inline; filename=\"" + filename + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String normalizeToIsoDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        String trimmed = dateStr.trim();
        if (trimmed.matches("^\\d{1,2}/\\d{1,2}/\\d{4}$")) {
            String[] p = trimmed.split("/");
            return String.format("%04d-%02d-%02d", Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
        }
        if (trimmed.matches("^\\d{1,2}-\\d{1,2}-\\d{4}$")) {
            String[] p = trimmed.split("-");
            return String.format("%04d-%02d-%02d", Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
        }
        if (trimmed.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$")) {
            return trimmed;
        }
        return trimmed;
    }

    private String formatDateToDdMmYyyy(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return dateStr;
        String trimmed = dateStr.trim();
        if (trimmed.matches("^\\d{1,2}/\\d{1,2}/\\d{4}$")) {
            return trimmed;
        }
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }
}
