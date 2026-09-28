package com.example.jasperdemo.dto;

public class AttendanceStatsDto {
    private long totalRecords;
    private long distinctEmployees;
    private long distinctDays;
    private long totalCheckIns;
    private long totalCheckOuts;
    private String earliestRecord;
    private String latestRecord;

    public AttendanceStatsDto() {}

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getDistinctEmployees() {
        return distinctEmployees;
    }

    public void setDistinctEmployees(long distinctEmployees) {
        this.distinctEmployees = distinctEmployees;
    }

    public long getDistinctDays() {
        return distinctDays;
    }

    public void setDistinctDays(long distinctDays) {
        this.distinctDays = distinctDays;
    }

    public long getTotalCheckIns() {
        return totalCheckIns;
    }

    public void setTotalCheckIns(long totalCheckIns) {
        this.totalCheckIns = totalCheckIns;
    }

    public long getTotalCheckOuts() {
        return totalCheckOuts;
    }

    public void setTotalCheckOuts(long totalCheckOuts) {
        this.totalCheckOuts = totalCheckOuts;
    }

    public String getEarliestRecord() {
        return earliestRecord;
    }

    public void setEarliestRecord(String earliestRecord) {
        this.earliestRecord = earliestRecord;
    }

    public String getLatestRecord() {
        return latestRecord;
    }

    public void setLatestRecord(String latestRecord) {
        this.latestRecord = latestRecord;
    }
}
