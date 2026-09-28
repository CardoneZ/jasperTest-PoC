package com.example.jasperdemo.repository;

import com.example.jasperdemo.dto.AttendanceRecordDto;
import com.example.jasperdemo.dto.AttendanceStatsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
public class AttendanceRepository {

    private static final Logger log = LoggerFactory.getLogger(AttendanceRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public AttendanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Inserción por lotes con prevención de duplicados (INSERT IGNORE).
     * Retorna un mapa con {insertedCount, duplicateCount}.
     */
    public Map<String, Integer> batchInsert(List<AttendanceRecordDto> records, String fileName, String batchId) {
        String sql = "INSERT INTO attendance_record " +
                "(user_id, employee_name, department, punch_time, punch_type, normal_hours, real_hours, " +
                "late_count, late_minutes, early_exit_count, early_exit_minutes, attended_days, " +
                "absence_days, leave_days, period_range, verification_mode, device_id, source_file, import_batch_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "employee_name = VALUES(employee_name), " +
                "department = VALUES(department), " +
                "normal_hours = VALUES(normal_hours), " +
                "real_hours = VALUES(real_hours), " +
                "late_count = VALUES(late_count), " +
                "late_minutes = VALUES(late_minutes), " +
                "early_exit_count = VALUES(early_exit_count), " +
                "early_exit_minutes = VALUES(early_exit_minutes), " +
                "attended_days = VALUES(attended_days), " +
                "absence_days = VALUES(absence_days), " +
                "leave_days = VALUES(leave_days), " +
                "period_range = VALUES(period_range), " +
                "import_batch_id = VALUES(import_batch_id)";

        int[][] updateCounts = jdbcTemplate.batchUpdate(sql, records, records.size(),
                (ps, dto) -> {
                    ps.setInt(1, dto.getUserId());
                    ps.setString(2, dto.getEmployeeName());
                    ps.setString(3, dto.getDepartment() != null ? dto.getDepartment() : "General");
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.parse(dto.getTimestamp(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    ps.setString(5, dto.getPunchType());
                    ps.setString(6, dto.getNormalHours() != null ? dto.getNormalHours() : "240:00");
                    ps.setString(7, dto.getRealHours() != null ? dto.getRealHours() : "0:00");
                    ps.setInt(8, dto.getLateCount() != null ? dto.getLateCount() : 0);
                    ps.setInt(9, dto.getLateMinutes() != null ? dto.getLateMinutes() : 0);
                    ps.setInt(10, dto.getEarlyExitCount() != null ? dto.getEarlyExitCount() : 0);
                    ps.setInt(11, dto.getEarlyExitMinutes() != null ? dto.getEarlyExitMinutes() : 0);
                    ps.setString(12, dto.getAttendedDays() != null ? dto.getAttendedDays() : "-");
                    ps.setInt(13, dto.getAbsenceDays() != null ? dto.getAbsenceDays() : 0);
                    ps.setInt(14, dto.getLeaveDays() != null ? dto.getLeaveDays() : 0);
                    ps.setString(15, dto.getPeriod() != null ? dto.getPeriod() : "-");
                    ps.setString(16, dto.getVerificationMode());
                    ps.setString(17, dto.getDeviceId());
                    ps.setString(18, fileName);
                    ps.setString(19, batchId);
                });

        int inserted = 0;
        int duplicates = 0;

        for (int[] batch : updateCounts) {
            for (int count : batch) {
                if (count == 1) {
                    inserted++;
                } else {
                    duplicates++;
                }
            }
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("inserted", inserted);
        result.put("duplicates", duplicates);
        return result;
    }

    /**
     * Consulta registros almacenados en la BD usando el procedimiento sp_get_attendance_records.
     */
    public List<Map<String, Object>> getStoredRecords(String startDate, String endDate, Integer userId, String search, Integer limit) {
        String sql = "CALL sp_get_attendance_records(?, ?, ?, ?, ?)";
        return jdbcTemplate.queryForList(sql, startDate, endDate, userId, search, limit != null ? limit : 200);
    }

    /**
     * Obtiene estadísticas agregadas mediante sp_get_attendance_stats.
     */
    public AttendanceStatsDto getStats() {
        String sql = "CALL sp_get_attendance_stats()";
        List<AttendanceStatsDto> list = jdbcTemplate.query(sql, new RowMapper<AttendanceStatsDto>() {
            @Override
            public AttendanceStatsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                AttendanceStatsDto dto = new AttendanceStatsDto();
                dto.setTotalRecords(rs.getLong("total_records"));
                dto.setDistinctEmployees(rs.getLong("distinct_employees"));
                dto.setDistinctDays(rs.getLong("distinct_days"));
                dto.setTotalCheckIns(rs.getLong("total_check_ins"));
                dto.setTotalCheckOuts(rs.getLong("total_check_outs"));

                Timestamp minTs = rs.getTimestamp("earliest_record");
                Timestamp maxTs = rs.getTimestamp("latest_record");
                dto.setEarliestRecord(minTs != null ? minTs.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "-");
                dto.setLatestRecord(maxTs != null ? maxTs.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "-");
                return dto;
            }
        });

        return list.isEmpty() ? new AttendanceStatsDto() : list.get(0);
    }

    /**
     * Consulta datos para el reporte JasperReports de asistencia.
     */
    public List<Map<String, Object>> getReportData(String startDate, String endDate, Integer userId) {
        String sql = "CALL sp_report_attendance_log(?, ?, ?)";
        return jdbcTemplate.queryForList(sql, startDate, endDate, userId);
    }

    /**
     * Comprueba si una checada (userId + punchTime) ya existe en la base de datos.
     */
    public boolean existsPunch(int userId, LocalDateTime punchTime) {
        String sql = "SELECT COUNT(*) FROM attendance_record WHERE user_id = ? AND punch_time = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, Timestamp.valueOf(punchTime));
        return count != null && count > 0;
    }
}
