package com.example.jasperdemo.controller;

import com.example.jasperdemo.repository.ReportsRepository;
import com.example.jasperdemo.service.JasperReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportsRepository reportsRepository;
    private final JasperReportService jasperReportService;

    public ReportController(ReportsRepository reportsRepository, JasperReportService jasperReportService) {
        this.reportsRepository = reportsRepository;
        this.jasperReportService = jasperReportService;
    }

    /**
     * SERVICIO WEB 1: Reporte General de Películas
     * Invoca sp_report_general_films y compila/genera report_general_films.jrxml
     */
    @RequestMapping(value = "/general", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> generateGeneralReport(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false, defaultValue = "inline") String disposition) {

        List<Map<String, Object>> data = reportsRepository.getReport1GeneralData(categoryId, rating);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("P_REPORT_TITLE", "Catálogo General de Películas e Inventario");
        parameters.put("P_GENERATION_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        parameters.put("P_CATEGORY_FILTER", categoryId != null && categoryId > 0 ? "Categoría ID: " + categoryId : "Todas las Categorías");
        parameters.put("P_RATING_FILTER", rating != null && !rating.isEmpty() && !rating.equalsIgnoreCase("ALL") ? "Clasificación: " + rating : "Todas");
        parameters.put("P_USER", "Sistema de Gestión - PoC JasperReports");

        byte[] pdf = jasperReportService.generatePdfReport("report_general_films", parameters, (List) data);

        String filename = "Reporte_General_Peliculas_" + System.currentTimeMillis() + ".pdf";
        String contentDisposition = "attachment".equalsIgnoreCase(disposition) 
                ? "attachment; filename=\"" + filename + "\"" 
                : "inline; filename=\"" + filename + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * SERVICIO WEB 2: Reporte con Información Agrupada por Categoría y Filtros
     * Invoca sp_report_category_rentals y compila/genera report_category_rentals.jrxml
     */
    @RequestMapping(value = "/grouped", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> generateGroupedReport(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer minRentals,
            @RequestParam(required = false, defaultValue = "inline") String disposition) {

        List<Map<String, Object>> data = reportsRepository.getReport2GroupedData(categoryId, minRentals);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("P_REPORT_TITLE", "Reporte Agrupado de Rentas e Ingresos por Categoría");
        parameters.put("P_GENERATION_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        parameters.put("P_MIN_RENTALS_FILTER", minRentals != null && minRentals > 0 ? minRentals.toString() : "0");
        parameters.put("P_CATEGORY_FILTER", categoryId != null && categoryId > 0 ? "Categoría ID: " + categoryId : "Todas");
        parameters.put("P_USER", "Sistema de Gestión - PoC JasperReports");

        byte[] pdf = jasperReportService.generatePdfReport("report_category_rentals", parameters, (List) data);

        String filename = "Reporte_Agrupado_Rentas_" + System.currentTimeMillis() + ".pdf";
        String contentDisposition = "attachment".equalsIgnoreCase(disposition) 
                ? "attachment; filename=\"" + filename + "\"" 
                : "inline; filename=\"" + filename + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * SERVICIO WEB 3: Reporte Ejecutivo / Analítica con Gráficos y Estructura Diferente
     * Invoca sp_report_store_analytics y compila/genera report_store_analytics.jrxml
     */
    @RequestMapping(value = "/analytics", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> generateAnalyticsReport(
            @RequestParam(required = false) Integer storeId,
            @RequestParam(required = false, defaultValue = "16") Integer limit,
            @RequestParam(required = false, defaultValue = "inline") String disposition) {

        List<Map<String, Object>> data = reportsRepository.getReport3AnalyticsData(storeId, limit);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("P_REPORT_TITLE", "Panel Ejecutivo de Rendimiento y Análisis de Ingresos");
        parameters.put("P_GENERATION_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        parameters.put("P_STORE_FILTER", storeId != null && storeId > 0 ? "Sucursal / Tienda " + storeId : "Todas las Sucursales");
        parameters.put("P_USER", "Dirección General - PoC JasperReports");

        byte[] pdf = jasperReportService.generatePdfReport("report_store_analytics", parameters, (List) data);

        String filename = "Reporte_Analitica_Ejecutiva_" + System.currentTimeMillis() + ".pdf";
        String contentDisposition = "attachment".equalsIgnoreCase(disposition) 
                ? "attachment; filename=\"" + filename + "\"" 
                : "inline; filename=\"" + filename + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
