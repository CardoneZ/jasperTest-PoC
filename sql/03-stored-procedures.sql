-- =============================================================================
-- PROCEDIMIENTOS ALMACENADOS PARA LA PRUEBA DE CONCEPTO (PoC) JASPERREPORTS
-- Base de Datos: Sakila
-- =============================================================================

USE sakila;

DROP PROCEDURE IF EXISTS sp_get_film_catalog;
DROP PROCEDURE IF EXISTS sp_report_general_films;
DROP PROCEDURE IF EXISTS sp_report_category_rentals;
DROP PROCEDURE IF EXISTS sp_report_store_analytics;

DELIMITER $$

-- -----------------------------------------------------------------------------
-- 1. SP PARA LLENADO DE TABLA EN VUE 3: sp_get_film_catalog
-- Consulta el catálogo con filtros opcionales de categoría, clasificación y búsqueda.
-- -----------------------------------------------------------------------------
CREATE PROCEDURE sp_get_film_catalog(
    IN p_category_id INT,
    IN p_rating VARCHAR(10),
    IN p_search VARCHAR(100)
)
BEGIN
    SELECT 
        f.film_id,
        f.title,
        f.description,
        f.release_year,
        l.name AS language_name,
        c.category_id,
        c.name AS category_name,
        f.rental_rate,
        f.length,
        f.rating,
        f.replacement_cost,
        COUNT(r.rental_id) AS total_rentals
    FROM film f
    INNER JOIN language l ON f.language_id = l.language_id
    LEFT JOIN film_category fc ON f.film_id = fc.film_id
    LEFT JOIN category c ON fc.category_id = c.category_id
    LEFT JOIN inventory i ON f.film_id = i.film_id
    LEFT JOIN rental r ON i.inventory_id = r.inventory_id
    WHERE 
        (p_category_id IS NULL OR p_category_id = 0 OR c.category_id = p_category_id)
        AND (p_rating IS NULL OR p_rating = '' OR p_rating = 'ALL' OR f.rating = p_rating)
        AND (p_search IS NULL OR p_search = '' OR f.title LIKE CONCAT('%', p_search, '%') OR f.description LIKE CONCAT('%', p_search, '%'))
    GROUP BY 
        f.film_id, f.title, f.description, f.release_year, l.name, c.category_id, c.name, 
        f.rental_rate, f.length, f.rating, f.replacement_cost
    ORDER BY f.title ASC
    LIMIT 200;
END $$

-- -----------------------------------------------------------------------------
-- 2. SP PARA REPORTE 1 (GENERAL): sp_report_general_films
-- Genera el dataset del reporte general de inventario de películas con métricas.
-- -----------------------------------------------------------------------------
CREATE PROCEDURE sp_report_general_films(
    IN p_category_id INT,
    IN p_rating VARCHAR(10)
)
BEGIN
    SELECT 
        f.film_id,
        f.title,
        c.name AS category_name,
        f.rating,
        f.length,
        f.rental_duration,
        f.rental_rate,
        f.replacement_cost,
        COUNT(r.rental_id) AS total_rentals,
        COALESCE(SUM(p.amount), 0.00) AS total_revenue
    FROM film f
    LEFT JOIN film_category fc ON f.film_id = fc.film_id
    LEFT JOIN category c ON fc.category_id = c.category_id
    LEFT JOIN inventory i ON f.film_id = i.film_id
    LEFT JOIN rental r ON i.inventory_id = r.inventory_id
    LEFT JOIN payment p ON r.rental_id = p.rental_id
    WHERE 
        (p_category_id IS NULL OR p_category_id = 0 OR c.category_id = p_category_id)
        AND (p_rating IS NULL OR p_rating = '' OR p_rating = 'ALL' OR f.rating = p_rating)
    GROUP BY 
        f.film_id, f.title, c.name, f.rating, f.length, f.rental_duration, 
        f.rental_rate, f.replacement_cost
    ORDER BY f.title ASC;
END $$

-- -----------------------------------------------------------------------------
-- 3. SP PARA REPORTE 2 (AGRUPADO Y FILTRADO): sp_report_category_rentals
-- Datos agrupados por categoría con métricas de alquiler e ingresos.
-- -----------------------------------------------------------------------------
CREATE PROCEDURE sp_report_category_rentals(
    IN p_category_id INT,
    IN p_min_rentals INT
)
BEGIN
    SELECT 
        COALESCE(c.name, 'Sin Categoría') AS category_name,
        f.film_id,
        f.title,
        f.rating,
        f.rental_rate,
        COUNT(r.rental_id) AS rental_count,
        COALESCE(SUM(p.amount), 0.00) AS total_revenue,
        ROUND(AVG(f.length), 0) AS avg_duration
    FROM film f
    INNER JOIN film_category fc ON f.film_id = fc.film_id
    INNER JOIN category c ON fc.category_id = c.category_id
    LEFT JOIN inventory i ON f.film_id = i.film_id
    LEFT JOIN rental r ON i.inventory_id = r.inventory_id
    LEFT JOIN payment p ON r.rental_id = p.rental_id
    WHERE 
        (p_category_id IS NULL OR p_category_id = 0 OR c.category_id = p_category_id)
    GROUP BY 
        c.name, f.film_id, f.title, f.rating, f.rental_rate
    HAVING 
        (p_min_rentals IS NULL OR p_min_rentals = 0 OR COUNT(r.rental_id) >= p_min_rentals)
    ORDER BY 
        category_name ASC, total_revenue DESC;
END $$

-- -----------------------------------------------------------------------------
-- 4. SP PARA REPORTE 3 (ESTRUCTURA DIFERENTE / GRÁFICOS Y ANALÍTICA): sp_report_store_analytics
-- Resumen por categoría para graficación (Bar/Pie Chart) e indicadores ejecutivos (KPIs).
-- -----------------------------------------------------------------------------
CREATE PROCEDURE sp_report_store_analytics(
    IN p_store_id INT,
    IN p_limit INT
)
BEGIN
    SET @limit_val = IFNULL(p_limit, 16);
    
    SELECT 
        c.category_id,
        c.name AS category_name,
        COUNT(DISTINCT f.film_id) AS total_films,
        COUNT(r.rental_id) AS total_rentals,
        COALESCE(SUM(p.amount), 0.00) AS total_revenue,
        ROUND(AVG(f.rental_rate), 2) AS avg_rental_rate,
        ROUND(COALESCE(SUM(p.amount), 0.00) / NULLIF(COUNT(r.rental_id), 0), 2) AS revenue_per_rental
    FROM category c
    INNER JOIN film_category fc ON c.category_id = fc.category_id
    INNER JOIN film f ON fc.film_id = f.film_id
    INNER JOIN inventory i ON f.film_id = i.film_id
    LEFT JOIN rental r ON i.inventory_id = r.inventory_id
    LEFT JOIN payment p ON r.rental_id = p.rental_id
    WHERE 
        (p_store_id IS NULL OR p_store_id = 0 OR i.store_id = p_store_id)
    GROUP BY 
        c.category_id, c.name
    ORDER BY 
        total_revenue DESC
    LIMIT 16;
END $$

DELIMITER ;
