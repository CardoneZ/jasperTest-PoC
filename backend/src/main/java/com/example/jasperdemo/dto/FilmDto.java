package com.example.jasperdemo.dto;

import java.math.BigDecimal;

public class FilmDto {
    private Integer filmId;
    private String title;
    private String description;
    private Integer releaseYear;
    private String languageName;
    private Integer categoryId;
    private String categoryName;
    private BigDecimal rentalRate;
    private Integer length;
    private String rating;
    private BigDecimal replacementCost;
    private Long totalRentals;

    public FilmDto() {
    }

    public FilmDto(Integer filmId, String title, String description, Integer releaseYear,
                   String languageName, Integer categoryId, String categoryName,
                   BigDecimal rentalRate, Integer length, String rating,
                   BigDecimal replacementCost, Long totalRentals) {
        this.filmId = filmId;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.languageName = languageName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.rentalRate = rentalRate;
        this.length = length;
        this.rating = rating;
        this.replacementCost = replacementCost;
        this.totalRentals = totalRentals;
    }

    // Getters and Setters
    public Integer getFilmId() { return filmId; }
    public void setFilmId(Integer filmId) { this.filmId = filmId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public String getLanguageName() { return languageName; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getRentalRate() { return rentalRate; }
    public void setRentalRate(BigDecimal rentalRate) { this.rentalRate = rentalRate; }

    public Integer getLength() { return length; }
    public void setLength(Integer length) { this.length = length; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public BigDecimal getReplacementCost() { return replacementCost; }
    public void setReplacementCost(BigDecimal replacementCost) { this.replacementCost = replacementCost; }

    public Long getTotalRentals() { return totalRentals; }
    public void setTotalRentals(Long totalRentals) { this.totalRentals = totalRentals; }
}
