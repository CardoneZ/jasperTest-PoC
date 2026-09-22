package com.example.jasperdemo.controller;

import com.example.jasperdemo.dto.CategoryDto;
import com.example.jasperdemo.dto.FilmDto;
import com.example.jasperdemo.repository.CatalogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogRepository catalogRepository;

    public CatalogController(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @GetMapping("/films")
    public ResponseEntity<List<FilmDto>> getFilms(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String search) {
        List<FilmDto> films = catalogRepository.getFilms(categoryId, rating, search);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        List<CategoryDto> categories = catalogRepository.getCategories();
        return ResponseEntity.ok(categories);
    }
}
