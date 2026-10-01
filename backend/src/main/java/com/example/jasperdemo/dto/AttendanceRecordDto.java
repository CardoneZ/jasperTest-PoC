package com.example.jasperdemo.dto;

import java.util.ArrayList;
import java.util.List;

public class AttendanceRecordDto {
    private Integer rowNumber;
    private Integer userId;
    private String employeeName;
    private String timestamp; // Formato "dd/MM/yyyy HH:mm"
    private String date;      // Formato "dd/MM/yyyy"
    private String time;      // Formato "HH:mm"
    private String punchType; // ENTRADA, SALIDA, SALIDA_INTERMEDIA, ENTRADA_INTERMEDIA, REGISTRO
    private String verificationMode; // HUELLA, CONTRASENA, TARJETA, OTRO
    private String deviceId;  // LX50
    private String status;    // VALID, WARNING, INVALID
    private List<String> validationErrors = new ArrayList<>();

    // Campos enriquecidos para Reporte Estadístico / Horas Laborales
    private String department;
    private String normalHours = "240:00";
    private String realHours = "0:00";
    private Integer lateCount = 0;
    private Integer lateMinutes = 0;
    private Integer earlyExitCount = 0;
    private Integer earlyExitMinutes = 0;
    private String attendedDays = "-";
    private Integer absenceDays = 0;
    private Integer leaveDays = 0;
    private String period;

    public AttendanceRecordDto() {}

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPunchType() {
        return punchType;
    }

    public void setPunchType(String punchType) {
        this.punchType = punchType;
    }

    public String getVerificationMode() {
        return verificationMode;
    }

    public void setVerificationMode(String verificationMode) {
        this.verificationMode = verificationMode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public void addValidationError(String error) {
        if (this.validationErrors == null) {
            this.validationErrors = new ArrayList<>();
        }
        this.validationErrors.add(error);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getNormalHours() {
        return normalHours;
    }

    public void setNormalHours(String normalHours) {
        this.normalHours = normalHours;
    }

    public String getRealHours() {
        return realHours;
    }

    public void setRealHours(String realHours) {
        this.realHours = realHours;
    }

    public Integer getLateCount() {
        return lateCount;
    }

    public void setLateCount(Integer lateCount) {
        this.lateCount = lateCount;
    }

    public Integer getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(Integer lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public Integer getEarlyExitCount() {
        return earlyExitCount;
    }

    public void setEarlyExitCount(Integer earlyExitCount) {
        this.earlyExitCount = earlyExitCount;
    }

    public Integer getEarlyExitMinutes() {
        return earlyExitMinutes;
    }

    public void setEarlyExitMinutes(Integer earlyExitMinutes) {
        this.earlyExitMinutes = earlyExitMinutes;
    }

    public String getAttendedDays() {
        return attendedDays;
    }

    public void setAttendedDays(String attendedDays) {
        this.attendedDays = attendedDays;
    }

    public Integer getAbsenceDays() {
        return absenceDays;
    }

    public void setAbsenceDays(Integer absenceDays) {
        this.absenceDays = absenceDays;
    }

    public Integer getLeaveDays() {
        return leaveDays;
    }

    public void setLeaveDays(Integer leaveDays) {
        this.leaveDays = leaveDays;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
