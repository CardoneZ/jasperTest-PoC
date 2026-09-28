-- =============================================================================
-- TABLA Y PROCEDIMIENTOS ALMACENADOS PARA ASISTENCIA CHECADOR ZKTECO LX50
-- Base de Datos: Sakila
-- =============================================================================

USE sakila;

-- -----------------------------------------------------------------------------
-- 1. TABLA: attendance_record
-- Almacena los registros de checadas importados desde el Excel del checador LX50.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS attendance_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT 'ID biométrico asignado en el checador ZKTeco LX50 (No. AC)',
    employee_name VARCHAR(100) NULL COMMENT 'Nombre del empleado (opcional en checador)',
    department VARCHAR(100) NULL COMMENT 'Departamento del empleado',
    punch_time DATETIME NOT NULL COMMENT 'Fecha y hora exacta de la checada o corte',
    punch_type VARCHAR(30) NOT NULL DEFAULT 'ENTRADA' COMMENT 'ENTRADA, SALIDA, ASISTENCIA, REGISTRO',
    normal_hours VARCHAR(20) DEFAULT '240:00' COMMENT 'Horas laborales normales del mes',
    real_hours VARCHAR(20) DEFAULT '0:00' COMMENT 'Horas laborales reales laboradas',
    late_count INT DEFAULT 0 COMMENT 'Cantidad de retardos',
    late_minutes INT DEFAULT 0 COMMENT 'Minutos acumulados de retardos',
    early_exit_count INT DEFAULT 0 COMMENT 'Cantidad de salidas temprano',
    early_exit_minutes INT DEFAULT 0 COMMENT 'Minutos acumulados de salidas temprano',
    attended_days VARCHAR(20) NULL COMMENT 'Días asistidos (Normal/Real)',
    absence_days INT DEFAULT 0 COMMENT 'Días de falta',
    leave_days INT DEFAULT 0 COMMENT 'Días de permiso',
    period_range VARCHAR(60) NULL COMMENT 'Rango del periodo reportado',
    verification_mode VARCHAR(30) DEFAULT 'HUELLA' COMMENT 'HUELLA, CONTRASENA, TARJETA, OTRO',
    device_id VARCHAR(50) DEFAULT 'LX50' COMMENT 'Identificador de la terminal biométrica',
    source_file VARCHAR(255) NULL COMMENT 'Nombre del archivo Excel origen extraído por USB',
    import_batch_id VARCHAR(64) NULL COMMENT 'Identificador UUID del lote de importación',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_punch UNIQUE (user_id, punch_time),
    INDEX idx_punch_time (punch_time),
    INDEX idx_user_id (user_id),
    INDEX idx_batch (import_batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DELIMITER $$

-- -----------------------------------------------------------------------------
-- 2. PROCEDIMIENTO: sp_insert_attendance_record
-- Inserta o actualiza un registro enriquecido de asistencia evitando duplicados.
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_insert_attendance_record $$
CREATE PROCEDURE sp_insert_attendance_record(
    IN p_user_id INT,
    IN p_employee_name VARCHAR(100),
    IN p_department VARCHAR(100),
    IN p_punch_time DATETIME,
    IN p_punch_type VARCHAR(30),
    IN p_normal_hours VARCHAR(20),
    IN p_real_hours VARCHAR(20),
    IN p_late_count INT,
    IN p_late_minutes INT,
    IN p_early_exit_count INT,
    IN p_early_exit_minutes INT,
    IN p_attended_days VARCHAR(20),
    IN p_absence_days INT,
    IN p_leave_days INT,
    IN p_period_range VARCHAR(60),
    IN p_verification_mode VARCHAR(30),
    IN p_device_id VARCHAR(50),
    IN p_source_file VARCHAR(255),
    IN p_batch_id VARCHAR(64),
    OUT p_inserted INT
)
BEGIN
    DECLARE duplicate_count INT DEFAULT 0;

    SELECT COUNT(*) INTO duplicate_count
    FROM attendance_record
    WHERE user_id = p_user_id AND punch_time = p_punch_time;

    IF duplicate_count = 0 THEN
        INSERT INTO attendance_record (
            user_id, employee_name, department, punch_time, punch_type,
            normal_hours, real_hours, late_count, late_minutes,
            early_exit_count, early_exit_minutes, attended_days,
            absence_days, leave_days, period_range,
            verification_mode, device_id, source_file, import_batch_id
        ) VALUES (
            p_user_id, p_employee_name, p_department, p_punch_time, p_punch_type,
            COALESCE(p_normal_hours, '240:00'), COALESCE(p_real_hours, '0:00'),
            COALESCE(p_late_count, 0), COALESCE(p_late_minutes, 0),
            COALESCE(p_early_exit_count, 0), COALESCE(p_early_exit_minutes, 0),
            p_attended_days, COALESCE(p_absence_days, 0), COALESCE(p_leave_days, 0),
            p_period_range,
            p_verification_mode, p_device_id, p_source_file, p_batch_id
        );
        SET p_inserted = 1;
    ELSE
        UPDATE attendance_record SET
            employee_name = COALESCE(p_employee_name, employee_name),
            department = COALESCE(p_department, department),
            normal_hours = COALESCE(p_normal_hours, normal_hours),
            real_hours = COALESCE(p_real_hours, real_hours),
            late_count = COALESCE(p_late_count, late_count),
            late_minutes = COALESCE(p_late_minutes, late_minutes),
            early_exit_count = COALESCE(p_early_exit_count, early_exit_count),
            early_exit_minutes = COALESCE(p_early_exit_minutes, early_exit_minutes),
            attended_days = COALESCE(p_attended_days, attended_days),
            absence_days = COALESCE(p_absence_days, absence_days),
            leave_days = COALESCE(p_leave_days, leave_days),
            period_range = COALESCE(p_period_range, period_range),
            import_batch_id = COALESCE(p_batch_id, import_batch_id)
        WHERE user_id = p_user_id AND punch_time = p_punch_time;
        SET p_inserted = 0;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 3. PROCEDIMIENTO: sp_get_attendance_records
-- Consulta los registros almacenados con filtros de búsqueda y fechas.
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_get_attendance_records $$
CREATE PROCEDURE sp_get_attendance_records(
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_user_id INT,
    IN p_search VARCHAR(100),
    IN p_limit INT
)
BEGIN
    DECLARE v_limit INT DEFAULT 500;
    IF p_limit IS NOT NULL AND p_limit > 0 THEN
        SET v_limit = p_limit;
    END IF;

    SELECT 
        record_id,
        user_id,
        COALESCE(employee_name, 'No registrado') AS employee_name,
        COALESCE(department, 'General') AS department,
        punch_time,
        DATE(punch_time) AS punch_date,
        TIME(punch_time) AS punch_time_only,
        punch_type,
        COALESCE(normal_hours, '240:00') AS normal_hours,
        COALESCE(real_hours, '0:00') AS real_hours,
        COALESCE(late_count, 0) AS late_count,
        COALESCE(late_minutes, 0) AS late_minutes,
        COALESCE(early_exit_count, 0) AS early_exit_count,
        COALESCE(early_exit_minutes, 0) AS early_exit_minutes,
        COALESCE(attended_days, '-') AS attended_days,
        COALESCE(absence_days, 0) AS absence_days,
        COALESCE(leave_days, 0) AS leave_days,
        COALESCE(period_range, '-') AS period_range,
        verification_mode,
        device_id,
        source_file,
        import_batch_id,
        created_at
    FROM attendance_record
    WHERE 
        (p_start_date IS NULL OR DATE(punch_time) >= p_start_date)
        AND (p_end_date IS NULL OR DATE(punch_time) <= p_end_date)
        AND (p_user_id IS NULL OR p_user_id = 0 OR user_id = p_user_id)
        AND (p_search IS NULL OR p_search = '' 
             OR employee_name LIKE CONCAT('%', p_search, '%')
             OR CAST(user_id AS CHAR) LIKE CONCAT('%', p_search, '%')
             OR department LIKE CONCAT('%', p_search, '%')
             OR device_id LIKE CONCAT('%', p_search, '%'))
    ORDER BY user_id ASC, punch_time DESC
    LIMIT v_limit;
END $$

-- -----------------------------------------------------------------------------
-- 4. PROCEDIMIENTO: sp_get_attendance_stats
-- Retorna estadísticas generales del módulo de asistencia.
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_get_attendance_stats $$
CREATE PROCEDURE sp_get_attendance_stats()
BEGIN
    SELECT 
        COUNT(*) AS total_records,
        COUNT(DISTINCT user_id) AS distinct_employees,
        COUNT(DISTINCT DATE(punch_time)) AS distinct_days,
        SUM(CASE WHEN punch_type = 'ENTRADA' THEN 1 ELSE 0 END) AS total_check_ins,
        SUM(CASE WHEN punch_type = 'SALIDA' THEN 1 ELSE 0 END) AS total_check_outs,
        MIN(punch_time) AS earliest_record,
        MAX(punch_time) AS latest_record
    FROM attendance_record;
END $$

-- -----------------------------------------------------------------------------
-- 5. PROCEDIMIENTO: sp_report_attendance_log
-- Procedimiento para alimentar el reporte JasperReports de Asistencia de Personal.
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_report_attendance_log $$
CREATE PROCEDURE sp_report_attendance_log(
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_user_id INT
)
BEGIN
    SELECT 
        user_id,
        COALESCE(employee_name, CONCAT('Empleado #', user_id)) AS employee_name,
        COALESCE(department, 'General') AS department,
        DATE(punch_time) AS attendance_date,
        TIME(punch_time) AS punch_time_str,
        punch_time,
        punch_type,
        COALESCE(normal_hours, '240:00') AS normal_hours,
        COALESCE(real_hours, '0:00') AS real_hours,
        COALESCE(late_count, 0) AS late_count,
        COALESCE(late_minutes, 0) AS late_minutes,
        COALESCE(early_exit_count, 0) AS early_exit_count,
        COALESCE(early_exit_minutes, 0) AS early_exit_minutes,
        COALESCE(attended_days, '-') AS attended_days,
        COALESCE(absence_days, 0) AS absence_days,
        COALESCE(leave_days, 0) AS leave_days,
        COALESCE(period_range, '-') AS period_range,
        verification_mode,
        device_id
    FROM attendance_record
    WHERE 
        (p_start_date IS NULL OR DATE(punch_time) >= p_start_date)
        AND (p_end_date IS NULL OR DATE(punch_time) <= p_end_date)
        AND (p_user_id IS NULL OR p_user_id = 0 OR user_id = p_user_id)
    ORDER BY user_id ASC, punch_time ASC;
END $$

DELIMITER ;
