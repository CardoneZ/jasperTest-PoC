package com.example.jasperdemo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReportsRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getReport1GeneralData(Integer categoryId, String rating) {
        return jdbcTemplate.execute((java.sql.Connection conn) -> {
            String sql = "{CALL sp_report_general_films(?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                if (categoryId != null && categoryId > 0) {
                    cs.setInt(1, categoryId);
                } else {
                    cs.setNull(1, Types.INTEGER);
                }

                if (rating != null && !rating.trim().isEmpty() && !rating.equalsIgnoreCase("ALL")) {
                    cs.setString(2, rating.trim());
                } else {
                    cs.setNull(2, Types.VARCHAR);
                }

                return mapResultSet(cs.executeQuery());
            }
        });
    }

    public List<Map<String, Object>> getReport2GroupedData(Integer categoryId, Integer minRentals) {
        return jdbcTemplate.execute((java.sql.Connection conn) -> {
            String sql = "{CALL sp_report_category_rentals(?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                if (categoryId != null && categoryId > 0) {
                    cs.setInt(1, categoryId);
                } else {
                    cs.setNull(1, Types.INTEGER);
                }

                if (minRentals != null && minRentals > 0) {
                    cs.setInt(2, minRentals);
                } else {
                    cs.setNull(2, Types.INTEGER);
                }

                return mapResultSet(cs.executeQuery());
            }
        });
    }

    public List<Map<String, Object>> getReport3AnalyticsData(Integer storeId, Integer limit) {
        return jdbcTemplate.execute((java.sql.Connection conn) -> {
            String sql = "{CALL sp_report_store_analytics(?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                if (storeId != null && storeId > 0) {
                    cs.setInt(1, storeId);
                } else {
                    cs.setNull(1, Types.INTEGER);
                }

                if (limit != null && limit > 0) {
                    cs.setInt(2, limit);
                } else {
                    cs.setInt(2, 16);
                }

                return mapResultSet(cs.executeQuery());
            }
        });
    }

    private List<Map<String, Object>> mapResultSet(ResultSet rs) throws java.sql.SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>(columns);
            for (int i = 1; i <= columns; ++i) {
                row.put(md.getColumnLabel(i), rs.getObject(i));
            }
            results.add(row);
        }
        return results;
    }
}
