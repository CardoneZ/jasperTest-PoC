package com.example.jasperdemo.repository;

import com.example.jasperdemo.dto.CategoryDto;
import com.example.jasperdemo.dto.FilmDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CatalogRepository {

    private final JdbcTemplate jdbcTemplate;

    public CatalogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FilmDto> getFilms(Integer categoryId, String rating, String search) {
        return jdbcTemplate.execute((java.sql.Connection conn) -> {
            String sql = "{CALL sp_get_film_catalog(?, ?, ?)}";
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

                if (search != null && !search.trim().isEmpty()) {
                    cs.setString(3, search.trim());
                } else {
                    cs.setNull(3, Types.VARCHAR);
                }

                List<FilmDto> list = new ArrayList<>();
                try (ResultSet rs = cs.executeQuery()) {
                    while (rs.next()) {
                        FilmDto dto = new FilmDto();
                        dto.setFilmId(rs.getInt("film_id"));
                        dto.setTitle(rs.getString("title"));
                        dto.setDescription(rs.getString("description"));
                        dto.setReleaseYear(rs.getInt("release_year"));
                        dto.setLanguageName(rs.getString("language_name"));
                        dto.setCategoryId(rs.getInt("category_id"));
                        dto.setCategoryName(rs.getString("category_name"));
                        dto.setRentalRate(rs.getBigDecimal("rental_rate"));
                        dto.setLength(rs.getInt("length"));
                        dto.setRating(rs.getString("rating"));
                        dto.setReplacementCost(rs.getBigDecimal("replacement_cost"));
                        dto.setTotalRentals(rs.getLong("total_rentals"));
                        list.add(dto);
                    }
                }
                return list;
            }
        });
    }

    public List<CategoryDto> getCategories() {
        String sql = "SELECT category_id, name FROM category ORDER BY name ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            new CategoryDto(rs.getInt("category_id"), rs.getString("name"))
        );
    }
}
