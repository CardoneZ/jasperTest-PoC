package com.example.jasperdemo.dto;

import java.util.List;

public class AttendanceConfirmRequest {
    private String batchId;
    private String fileName;
    private List<AttendanceRecordDto> records;

    public AttendanceConfirmRequest() {}

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<AttendanceRecordDto> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecordDto> records) {
        this.records = records;
    }
}
