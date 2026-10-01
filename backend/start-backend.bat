@echo off
title Jasper Backend (Spring Boot)
cd /d "%~dp0"
echo ==========================================================
echo  Iniciando Backend Spring Boot en http://localhost:8080
echo ==========================================================
call mvnw.cmd spring-boot:run
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Hubo un problema al iniciar el backend.
    pause
)
