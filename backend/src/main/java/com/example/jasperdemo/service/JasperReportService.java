package com.example.jasperdemo.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JasperReportService {

    private static final Logger log = LoggerFactory.getLogger(JasperReportService.class);
    private final Map<String, JasperReport> reportCache = new ConcurrentHashMap<>();

    public byte[] generatePdfReport(String templateName, Map<String, Object> parameters, Collection<Map<String, ?>> data) {
        try {
            log.info("Iniciando generación de reporte: {} con {} registros", templateName, data != null ? data.size() : 0);
            long startTime = System.currentTimeMillis();

            JasperReport jasperReport = getCompiledReport(templateName);

            JRDataSource dataSource = (data != null && !data.isEmpty())
                    ? new JRMapCollectionDataSource(data)
                    : new JREmptyDataSource();

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Reporte {} generado exitosamente ({} bytes) en {} ms", templateName, pdfBytes.length, elapsed);

            return pdfBytes;
        } catch (JRException e) {
            log.error("Error generando reporte JasperReports: {}", templateName, e);
            throw new RuntimeException("Error al generar el reporte PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado en reporte: {}", templateName, e);
            throw new RuntimeException("Error inesperado al procesar reporte: " + e.getMessage(), e);
        }
    }

    private JasperReport getCompiledReport(String templateName) throws Exception {
        if (reportCache.containsKey(templateName)) {
            return reportCache.get(templateName);
        }

        String path = "reports/" + templateName + ".jrxml";
        log.info("Compilando plantilla JasperReports desde: {}", path);
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IllegalArgumentException("No se encontró la plantilla de reporte: " + path);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            JasperReport compiled = JasperCompileManager.compileReport(inputStream);
            reportCache.put(templateName, compiled);
            return compiled;
        }
    }
}
