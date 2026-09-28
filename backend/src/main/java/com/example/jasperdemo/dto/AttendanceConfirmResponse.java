package com.example.jasperdemo.dto;

public class AttendanceConfirmResponse {
    private String batchId;
    private int totalSubmitted;
    private int insertedCount;
    private int duplicateCount;
    private int rejectedCount;
    private String message;

    public AttendanceConfirmResponse() {}

    public AttendanceConfirmResponse(String batchId, int totalSubmitted, int insertedCount, int duplicateCount, int rejectedCount, String message) {
        this.batchId = batchId;
        this.totalSubmitted = totalSubmitted;
        this.insertedCount = insertedCount;
        this.duplicateCount = duplicateCount;
        this.rejectedCount = rejectedCount;
        this.message = message;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public int getTotalSubmitted() {
        return totalSubmitted;
    }

    public void setTotalSubmitted(int totalSubmitted) {
        this.totalSubmitted = totalSubmitted;
    }

    public int getInsertedCount() {
        return insertedCount;
    }

    public void setInsertedCount(int insertedCount) {
        this.insertedCount = insertedCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
