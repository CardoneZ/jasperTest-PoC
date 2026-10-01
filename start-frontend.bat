@echo off
title Jasper Frontend (Vue 3)
cd /d "%~dp0frontend"
echo ==========================================================
echo  Iniciando Frontend Vue 3 en http://localhost:5173
echo ==========================================================
call npm run dev
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Hubo un problema al iniciar el frontend.
    pause
)
