package com.example.jasperdemo.dto;

import java.util.ArrayList;
import java.util.List;

public class AttendancePreviewResponse {
    private String fileName;
    private String batchId;
    private int totalRows;
    private int validRows;
    private int warningRows;
    private int invalidRows;
    private List<AttendanceRecordDto> records = new ArrayList<>();

    public AttendancePreviewResponse() {}

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getValidRows() {
        return validRows;
    }

    public void setValidRows(int validRows) {
        this.validRows = validRows;
    }

    public int getWarningRows() {
        return warningRows;
    }

    public void setWarningRows(int warningRows) {
        this.warningRows = warningRows;
    }

    public int getInvalidRows() {
        return invalidRows;
    }

    public void setInvalidRows(int invalidRows) {
        this.invalidRows = invalidRows;
    }

    public List<AttendanceRecordDto> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecordDto> records) {
        this.records = records;
    }
}
