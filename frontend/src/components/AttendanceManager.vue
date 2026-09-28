<template>
  <div class="attendance-manager">
    <!-- Header Section -->
    <div class="module-header card">
      <div class="header-left">
        <div class="badge-row">
          <span class="badge badge-indigo">Terminal Biométrica ZKTeco LX50</span>
          <span class="badge badge-emerald">PoC Importación & Persistencia</span>
        </div>
        <h2 class="module-title">Módulo de Asistencia: Checador de Tiempo ZKTeco LX50</h2>
        <p class="module-description">
          Flujo automatizado de ingesta para archivos de asistencia extraídos vía memoria USB.
          Procesa, limpia inconsistencias de formato, detecta anomalías, muestra preview interactivo y persiste registros únicos en MySQL.
        </p>
      </div>

      <div class="header-actions">
        <button class="btn btn-outline" @click="downloadSampleExcel" :disabled="downloadingSample">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="7 10 12 15 17 10"></polyline>
            <line x1="12" y1="15" x2="12" y2="3"></line>
          </svg>
          {{ downloadingSample ? 'Descargando...' : 'Descargar Archivo Muestra LX50 (.xlsx)' }}
        </button>

        <button class="btn btn-secondary" @click="fetchStats">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"></polyline>
            <polyline points="1 20 1 14 7 14"></polyline>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
          </svg>
          Actualizar Estadísticas
        </button>

        <button class="btn btn-clean" @click="clearCurrentPage" title="Limpiar y reiniciar la pantalla actual">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 6h18"></path>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            <line x1="10" y1="11" x2="10" y2="17"></line>
            <line x1="14" y1="11" x2="14" y2="17"></line>
          </svg>
          Limpiar Página
        </button>
      </div>
    </div>

    <!-- Live BD Statistics KPIs -->
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-icon-wrap bg-blue-subtle">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
            <line x1="16" y1="13" x2="8" y2="13"></line>
            <line x1="16" y1="17" x2="8" y2="17"></line>
          </svg>
        </div>
        <div class="kpi-data">
          <span class="kpi-val">{{ stats.totalRecords || 0 }}</span>
          <span class="kpi-label">Checadas en BD</span>
        </div>
      </div>

      <div class="kpi-card">
        <div class="kpi-icon-wrap bg-emerald-subtle">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
        </div>
        <div class="kpi-data">
          <span class="kpi-val">{{ stats.distinctEmployees || 0 }}</span>
          <span class="kpi-label">Empleados Distintos</span>
        </div>
      </div>

      <div class="kpi-card">
        <div class="kpi-icon-wrap bg-indigo-subtle">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
        </div>
        <div class="kpi-data">
          <span class="kpi-val">{{ stats.distinctDays || 0 }} días</span>
          <span class="kpi-label">Cobertura Temporal</span>
        </div>
      </div>

      <div class="kpi-card">
        <div class="kpi-icon-wrap bg-amber-subtle">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 11 12 14 22 4"></polyline>
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path>
          </svg>
        </div>
        <div class="kpi-data">
          <span class="kpi-val">{{ stats.totalCheckIns || 0 }} / {{ stats.totalCheckOuts || 0 }}</span>
          <span class="kpi-label">Entradas / Salidas</span>
        </div>
      </div>
    </div>

    <!-- Navigation Sub-Tabs -->
    <div class="nav-subtabs">
      <button 
        class="subtab-btn" 
        :class="{ active: currentSubTab === 'upload' }" 
        @click="currentSubTab = 'upload'">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
          <polyline points="17 8 12 3 7 8"></polyline>
          <line x1="12" y1="3" x2="12" y2="15"></line>
        </svg>
        1. Carga, Limpieza y Preview del Archivo
      </button>

      <button 
        class="subtab-btn" 
        :class="{ active: currentSubTab === 'database' }" 
        @click="currentSubTab = 'database'; fetchStoredRecords()">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
          <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
          <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
        </svg>
        2. Registros Guardados en BD & Reportes Jasper
        <span class="subtab-counter">{{ stats.totalRecords || 0 }}</span>
      </button>
    </div>

    <!-- SUBTAB 1: CARGA Y PREVIEW DE ARCHIVO EXCEL -->
    <div v-if="currentSubTab === 'upload'" class="tab-pane">
      <!-- File Upload Dropzone (Visible if no preview or if resetting) -->
      <div v-if="!previewData" class="card dropzone-card">
        <div 
          class="dropzone-area" 
          :class="{ 'is-dragging': isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleFileDrop"
          @click="triggerFileInput">
          
          <input 
            type="file" 
            ref="fileInputRef" 
            class="hidden-file-input" 
            accept=".xlsx, .xls" 
            @change="handleFileSelect" />

          <div class="dropzone-icon-box">
            <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="8" y1="13" x2="16" y2="13"></line>
              <line x1="8" y1="17" x2="16" y2="17"></line>
              <polyline points="10 9 9 9 8 9"></polyline>
            </svg>
          </div>

          <h3 class="dropzone-title">Arrastra aquí el archivo Excel del Checador ZKTeco LX50</h3>
          <p class="dropzone-hint">
            Formatos soportados: <strong>.xlsx</strong> y <strong>.xls</strong> (Descarga SSR o Registro de Asistencia desde USB)
          </p>

          <div class="dropzone-actions" @click.stop>
            <button class="btn btn-primary" @click="triggerFileInput" :disabled="isUploading">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              {{ isUploading ? 'Procesando archivo...' : 'Seleccionar Archivo Excel' }}
            </button>
          </div>

          <div v-if="selectedFile" class="selected-file-chip" @click.stop>
            <span class="file-name">{{ selectedFile.name }} ({{ formatBytes(selectedFile.size) }})</span>
            <button class="btn-chip-process" @click="uploadAndProcessFile" :disabled="isUploading">
              {{ isUploading ? 'Analizando...' : 'Procesar y Ver Preview' }}
            </button>
            <button class="btn-chip-cancel" @click="clearCurrentPage" title="Limpiar archivo seleccionado">
              ✕
            </button>
          </div>
        </div>

        <div class="format-notes">
          <h4 class="format-notes-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="16" x2="12" y2="12"></line>
              <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
            Capacidades del Motor de Limpieza Integrado (ZKTeco LX50):
          </h4>
          <ul class="notes-list">
            <li><strong>Detección dinámica de cabeceras:</strong> Omite automáticamente filas decorativas, logos o títulos superiores como "Reporte de Asistencia LX50" o "Periodo: ...".</li>
            <li><strong>Soporte multi-formato:</strong> Reconoce tanto columnas unificadas de fecha/hora como columnas separadas (Fecha + Hora).</li>
            <li><strong>Normalización de eventos:</strong> Mapea códigos de estado numéricos (0, 1, 2) o texto ("Entrada", "Check-In", "Salida", "C/Out") al estándar oficial.</li>
            <li><strong>Detección de anomalías:</strong> Identifica dobles checadas consecutivas (&lt; 60 seg), fechas en el futuro y registros duplicados ya existentes en la BD.</li>
          </ul>
        </div>
      </div>

      <!-- Preview Section (Visible when file has been parsed) -->
      <div v-else class="preview-workspace">
        <!-- Preview Summary Banner -->
        <div class="card preview-header-card">
          <div class="preview-meta">
            <div class="file-badge">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
              </svg>
              <div>
                <strong class="file-title">{{ previewData.fileName }}</strong>
                <span class="batch-lbl">Lote ID: {{ previewData.batchId.substring(0, 13) }}...</span>
              </div>
            </div>

            <!-- Metric Pills -->
            <div class="preview-counts">
              <div class="p-pill pill-total">
                <span class="p-num">{{ previewData.totalRows }}</span>
                <span class="p-lbl">Total Filas</span>
              </div>
              <div class="p-pill pill-valid">
                <span class="p-num">{{ previewData.validRows }}</span>
                <span class="p-lbl">Válidos</span>
              </div>
              <div class="p-pill pill-warning">
                <span class="p-num">{{ previewData.warningRows }}</span>
                <span class="p-lbl">Advertencias</span>
              </div>
              <div class="p-pill pill-invalid">
                <span class="p-num">{{ previewData.invalidRows }}</span>
                <span class="p-lbl">Inválidos</span>
              </div>
            </div>
          </div>

          <!-- Action bar to confirm or reset -->
          <div class="preview-actions">
            <button 
              class="btn btn-emerald btn-lg" 
              @click="confirmAndSaveToDb" 
              :disabled="isSaving || (previewData.validRows + previewData.warningRows === 0)">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              {{ isSaving ? 'Guardando en BD...' : `Confirmar e Importar ${previewData.validRows + previewData.warningRows} Registros Válidos` }}
            </button>

            <button class="btn btn-clean btn-lg" @click="clearCurrentPage" :disabled="isSaving" title="Limpiar y descartar el archivo cargado">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 6h18"></path>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                <line x1="10" y1="11" x2="10" y2="17"></line>
                <line x1="14" y1="11" x2="14" y2="17"></line>
              </svg>
              Limpiar Página / Descartar
            </button>
          </div>
        </div>

        <!-- Confirmation Result Banner (If just confirmed) -->
        <div v-if="confirmResult" class="card result-banner">
          <div class="result-header">
            <div class="result-icon-box">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#16A34A" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
            </div>
            <div>
              <h3 class="result-title">¡Importación Completada Exitosamente en la Base de Datos!</h3>
              <p class="result-message">{{ confirmResult.message }}</p>
            </div>
          </div>

          <div class="result-kpis">
            <div class="res-stat">
              <span class="res-val">{{ confirmResult.totalSubmitted }}</span>
              <span class="res-lbl">Enviados</span>
            </div>
            <div class="res-stat text-emerald">
              <span class="res-val">{{ confirmResult.insertedCount }}</span>
              <span class="res-lbl">Insertados en BD</span>
            </div>
            <div class="res-stat text-amber">
              <span class="res-val">{{ confirmResult.duplicateCount }}</span>
              <span class="res-lbl">Duplicados Omitidos</span>
            </div>
            <div class="res-stat text-rose">
              <span class="res-val">{{ confirmResult.rejectedCount }}</span>
              <span class="res-lbl">Rechazados</span>
            </div>
          </div>

          <div class="result-actions">
            <button class="btn btn-primary" @click="currentSubTab = 'database'; fetchStoredRecords()">
              Ver Registros Almacenados en BD
            </button>
            <button class="btn btn-outline" @click="generateAttendanceReportPdf">
              Generar Reporte PDF Oficial con JasperReports
            </button>
            <button class="btn btn-clean" @click="clearCurrentPage" title="Limpiar pantalla para cargar otro archivo">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 6h18"></path>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
              Limpiar y Cargar Otro Archivo
            </button>
          </div>
        </div>

        <!-- Filter & Search Toolbar for Preview Table -->
        <div class="card table-card">
          <div class="table-toolbar">
            <div class="filter-tabs">
              <button 
                class="ftab" 
                :class="{ active: previewFilter === 'ALL' }" 
                @click="previewFilter = 'ALL'">
                Todos ({{ previewData.records.length }})
              </button>
              <button 
                class="ftab ftab-valid" 
                :class="{ active: previewFilter === 'VALID' }" 
                @click="previewFilter = 'VALID'">
                Válidos ({{ previewData.validRows }})
              </button>
              <button 
                class="ftab ftab-warning" 
                :class="{ active: previewFilter === 'WARNING' }" 
                @click="previewFilter = 'WARNING'">
                Advertencias ({{ previewData.warningRows }})
              </button>
              <button 
                class="ftab ftab-invalid" 
                :class="{ active: previewFilter === 'INVALID' }" 
                @click="previewFilter = 'INVALID'">
                Inválidos ({{ previewData.invalidRows }})
              </button>
            </div>

            <div class="toolbar-search-wrap">
              <div class="search-box">
                <input 
                  type="text" 
                  v-model="previewSearch" 
                  placeholder="Buscar por empleado o ID..." 
                  class="form-control search-input" />
              </div>
              <button class="btn btn-sm btn-clean" @click="clearCurrentPage" title="Limpiar y volver a la pantalla de carga">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 6h18"></path>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
                Limpiar Vista
              </button>
            </div>
          </div>

          <!-- Preview Table -->
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th style="width: 40px;" class="text-center">#</th>
                  <th style="width: 65px;" class="text-center">ID AC</th>
                  <th>Nombre Empleado</th>
                  <th>Departamento</th>
                  <th class="text-center">Horas Laborales (Real / Normal)</th>
                  <th class="text-center">Retardos (Minutos)</th>
                  <th class="text-center">Salidas Temp. (Minutos)</th>
                  <th class="text-center">Días Asistidos</th>
                  <th class="text-center">Faltas (Días)</th>
                  <th class="text-center">Permisos (Días)</th>
                  <th class="text-center">Periodo</th>
                  <th class="text-center">Estatus</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="rec in filteredPreviewRecords" 
                  :key="rec.rowNumber"
                  :class="{ 
                    'row-warning': rec.status === 'WARNING', 
                    'row-invalid': rec.status === 'INVALID' 
                  }">
                  <td class="text-center font-mono">{{ rec.rowNumber }}</td>
                  <td class="font-bold text-center">
                    <span v-if="rec.userId" class="badge badge-slate">{{ rec.userId }}</span>
                    <span v-else class="text-rose font-bold">Faltante</span>
                  </td>
                  <td>
                    <span :class="{ 'text-muted italic': rec.employeeName === 'Sin nombre registrado' }" class="font-semibold text-primary">
                      {{ rec.employeeName }}
                    </span>
                  </td>
                  <td>
                    <span class="badge badge-indigo">{{ rec.department || 'General' }}</span>
                  </td>
                  <td class="text-center font-mono">
                    <strong class="text-blue font-bold">{{ rec.realHours || '0:00' }}</strong>
                    <span class="text-muted text-xs"> / {{ rec.normalHours || '240:00' }}</span>
                  </td>
                  <td class="text-center">
                    <span v-if="rec.lateMinutes > 0" class="badge badge-amber font-mono font-bold">
                      {{ rec.lateMinutes }} min <span class="text-xs font-normal">({{ rec.lateCount }})</span>
                    </span>
                    <span v-else class="badge badge-slate text-muted">0 min</span>
                  </td>
                  <td class="text-center">
                    <span v-if="rec.earlyExitMinutes > 0" class="badge badge-rose font-mono font-bold">
                      {{ rec.earlyExitMinutes }} min <span class="text-xs font-normal">({{ rec.earlyExitCount }})</span>
                    </span>
                    <span v-else class="badge badge-slate text-muted">0 min</span>
                  </td>
                  <td class="text-center font-mono">
                    <span class="badge badge-emerald font-bold">{{ rec.attendedDays || '-' }}</span>
                  </td>
                  <td class="text-center font-mono">
                    <span :class="rec.absenceDays > 0 ? 'badge badge-rose font-bold' : 'badge badge-slate text-muted'">
                      {{ rec.absenceDays ?? 0 }}
                    </span>
                  </td>
                  <td class="text-center font-mono">
                    <span :class="rec.leaveDays > 0 ? 'badge badge-indigo font-bold' : 'badge badge-slate text-muted'">
                      {{ rec.leaveDays ?? 0 }}
                    </span>
                  </td>
                  <td class="text-center font-mono text-xs text-secondary">
                    {{ rec.period || rec.date || '-' }}
                  </td>
                  <td class="text-center">
                    <span class="badge" :class="getStatusBadge(rec.status)">
                      {{ rec.status }}
                    </span>
                  </td>
                </tr>
                <tr v-if="filteredPreviewRecords.length === 0">
                  <td colspan="12" class="text-center py-8 text-muted">
                    No hay registros que coincidan con los filtros seleccionados.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- SUBTAB 2: REGISTROS ALMACENADOS EN BD & REPORTES JASPER -->
    <div v-if="currentSubTab === 'database'" class="tab-pane">
      <div class="card filter-bar-card">
        <div class="filter-controls-row">
          <div class="form-group">
            <label class="form-label">Fecha Desde:</label>
            <input type="date" v-model="dbFilterStartDate" class="form-control" />
          </div>

          <div class="form-group">
            <label class="form-label">Fecha Hasta:</label>
            <input type="date" v-model="dbFilterEndDate" class="form-control" />
          </div>

          <div class="form-group">
            <label class="form-label">ID Empleado:</label>
            <input type="number" v-model="dbFilterUserId" placeholder="Todos" class="form-control" style="width: 110px;" />
          </div>

          <div class="form-group flex-1">
            <label class="form-label">Búsqueda General:</label>
            <input type="text" v-model="dbFilterSearch" placeholder="Nombre, terminal, lote..." class="form-control" />
          </div>

          <div class="form-actions">
            <button class="btn btn-secondary" @click="fetchStoredRecords" :disabled="loadingDbRecords">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
              Filtrar
            </button>

            <button class="btn btn-primary" @click="generateAttendanceReportPdf" :disabled="loadingPdf">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
              </svg>
              {{ loadingPdf ? 'Generando PDF...' : 'Generar Reporte PDF Jasper' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Stored Records Table -->
      <div class="card table-card">
        <div class="card-header-bar">
          <h3 class="card-subtitle">
            Registros Persistidos en Tabla <code>attendance_record</code> (Sakila / MySQL)
          </h3>
          <span class="badge badge-slate">{{ dbRecords.length }} registros cargados</span>
        </div>

        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th style="width: 45px;" class="text-center">#</th>
                <th style="width: 65px;" class="text-center">ID AC</th>
                <th>Nombre Empleado</th>
                <th>Departamento</th>
                <th class="text-center">Horas Laborales (Real / Normal)</th>
                <th class="text-center">Retardos (Minutos)</th>
                <th class="text-center">Salidas Temp. (Minutos)</th>
                <th class="text-center">Días Asistidos</th>
                <th class="text-center">Faltas (Días)</th>
                <th class="text-center">Permisos (Días)</th>
                <th class="text-center">Periodo</th>
                <th class="text-center">Fecha Ingesta</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in dbRecords" :key="row.record_id">
                <td class="font-mono text-center text-xs text-muted">#{{ row.record_id }}</td>
                <td class="text-center font-bold">
                  <span class="badge badge-slate">{{ row.user_id }}</span>
                </td>
                <td class="font-semibold text-primary">{{ row.employee_name || 'Sin Nombre' }}</td>
                <td>
                  <span class="badge badge-indigo">{{ row.department || 'General' }}</span>
                </td>
                <td class="text-center font-mono">
                  <strong class="text-blue font-bold">{{ row.real_hours || '0:00' }}</strong>
                  <span class="text-muted text-xs"> / {{ row.normal_hours || '240:00' }}</span>
                </td>
                <td class="text-center">
                  <span v-if="row.late_minutes > 0" class="badge badge-amber font-mono font-bold">
                    {{ row.late_minutes }} min <span class="text-xs font-normal">({{ row.late_count }})</span>
                  </span>
                  <span v-else class="badge badge-slate text-muted">0 min</span>
                </td>
                <td class="text-center">
                  <span v-if="row.early_exit_minutes > 0" class="badge badge-rose font-mono font-bold">
                    {{ row.early_exit_minutes }} min <span class="text-xs font-normal">({{ row.early_exit_count }})</span>
                  </span>
                  <span v-else class="badge badge-slate text-muted">0 min</span>
                </td>
                <td class="text-center font-mono">
                  <span class="badge badge-emerald font-bold">{{ row.attended_days || '-' }}</span>
                </td>
                <td class="text-center font-mono">
                  <span :class="row.absence_days > 0 ? 'badge badge-rose font-bold' : 'badge badge-slate text-muted'">
                    {{ row.absence_days ?? 0 }}
                  </span>
                </td>
                <td class="text-center font-mono">
                  <span :class="row.leave_days > 0 ? 'badge badge-indigo font-bold' : 'badge badge-slate text-muted'">
                    {{ row.leave_days ?? 0 }}
                  </span>
                </td>
                <td class="text-center font-mono text-xs text-secondary">
                  {{ row.period_range || row.punch_date || '-' }}
                </td>
                <td class="text-xs text-muted text-center font-mono">{{ formatTimestamp(row.created_at) }}</td>
              </tr>
              <tr v-if="dbRecords.length === 0">
                <td colspan="12" class="text-center py-10 text-muted">
                  <div class="empty-state-box">
                    <p>No se encontraron registros de checadas en la base de datos.</p>
                    <button class="btn btn-outline mt-3" @click="currentSubTab = 'upload'">
                      Cargar Archivo Excel de Asistencia
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const emit = defineEmits(['preview-report', 'show-toast'])

// State
const currentSubTab = ref('upload')
const stats = ref({
  totalRecords: 0,
  distinctEmployees: 0,
  distinctDays: 0,
  totalCheckIns: 0,
  totalCheckOuts: 0
})

// Upload & Preview state
const isDragging = ref(false)
const selectedFile = ref(null)
const fileInputRef = ref(null)
const isUploading = ref(false)
const previewData = ref(null)
const previewFilter = ref('ALL')
const previewSearch = ref('')
const isSaving = ref(false)
const confirmResult = ref(null)
const downloadingSample = ref(false)

// Database tab state
const dbRecords = ref([])
const loadingDbRecords = ref(false)
const dbFilterStartDate = ref('')
const dbFilterEndDate = ref('')
const dbFilterUserId = ref('')
const dbFilterSearch = ref('')
const loadingPdf = ref(false)

onMounted(() => {
  fetchStats()
  fetchStoredRecords()
})

// KPI Stats
async function fetchStats() {
  try {
    const res = await fetch('http://localhost:8080/api/attendance/stats')
    if (res.ok) {
      stats.value = await res.json()
    }
  } catch (err) {
    console.error('Error fetching attendance stats:', err)
  }
}

// Download authentic ZKTeco LX50 sample excel
async function downloadSampleExcel() {
  downloadingSample.value = true
  try {
    const res = await fetch('http://localhost:8080/api/attendance/sample')
    if (res.ok) {
      const blob = await res.blob()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'ZKTeco_LX50_Asistencia_Ejemplo.xlsx'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(url)
      emit('show-toast', { type: 'success', text: 'Archivo de muestra ZKTeco LX50 descargado con éxito.' })
    } else {
      throw new Error('Error al descargar archivo de prueba')
    }
  } catch (err) {
    emit('show-toast', { type: 'error', text: 'No se pudo descargar la plantilla de ejemplo.' })
  } finally {
    downloadingSample.value = false
  }
}

// File Drag & Drop / Selection
function triggerFileInput() {
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

function handleFileSelect(e) {
  const file = e.target.files[0]
  if (file) {
    selectedFile.value = file
    uploadAndProcessFile()
  }
}

function handleFileDrop(e) {
  isDragging.value = false
  const file = e.dataTransfer.files[0]
  if (file) {
    selectedFile.value = file
    uploadAndProcessFile()
  }
}

// Upload & Process Excel
async function uploadAndProcessFile() {
  if (!selectedFile.value) return
  isUploading.value = true
  confirmResult.value = null

  const formData = new FormData()
  formData.append('file', selectedFile.value)

  try {
    const res = await fetch('http://localhost:8080/api/attendance/upload-preview', {
      method: 'POST',
      body: formData
    })

    if (!res.ok) {
      const errJson = await res.json().catch(() => ({}))
      throw new Error(errJson.error || 'Fallo al procesar el archivo Excel')
    }

    const data = await res.json()
    previewData.value = data
    emit('show-toast', { 
      type: 'success', 
      text: `Archivo procesado: ${data.totalRows} filas analizadas (${data.validRows} válidas).` 
    })
  } catch (err) {
    emit('show-toast', { type: 'error', text: err.message || 'Error al conectar con el servidor.' })
  } finally {
    isUploading.value = false
  }
}

// Confirm & Persist to MySQL
async function confirmAndSaveToDb() {
  if (!previewData.value || !previewData.value.records) return
  isSaving.value = true

  const payload = {
    batchId: previewData.value.batchId,
    fileName: previewData.value.fileName,
    records: previewData.value.records
  }

  try {
    const res = await fetch('http://localhost:8080/api/attendance/confirm', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (!res.ok) {
      throw new Error('Fallo al guardar registros en la base de datos.')
    }

    const result = await res.json()
    confirmResult.value = result
    emit('show-toast', { type: 'success', text: result.message })
    fetchStats()
  } catch (err) {
    emit('show-toast', { type: 'error', text: err.message })
  } finally {
    isSaving.value = false
  }
}

function clearCurrentPage() {
  previewData.value = null
  selectedFile.value = null
  confirmResult.value = null
  previewFilter.value = 'ALL'
  previewSearch.value = ''
  isUploading.value = false
  isSaving.value = false
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
  emit('show-toast', { 
    type: 'info', 
    text: 'Página limpiada correctamente. Listo para procesar un nuevo archivo.' 
  })
}

function resetPreview() {
  clearCurrentPage()
}

// Filtered Records for Preview
const filteredPreviewRecords = computed(() => {
  if (!previewData.value || !previewData.value.records) return []
  return previewData.value.records.filter(r => {
    // Status filter
    if (previewFilter.value !== 'ALL' && r.status !== previewFilter.value) {
      return false
    }
    // Search filter
    if (previewSearch.value.trim() !== '') {
      const q = previewSearch.value.toLowerCase()
      const matchName = r.employeeName && r.employeeName.toLowerCase().includes(q)
      const matchId = r.userId && r.userId.toString().includes(q)
      const matchDept = r.department && r.department.toLowerCase().includes(q)
      const matchType = r.punchType && r.punchType.toLowerCase().includes(q)
      if (!matchName && !matchId && !matchDept && !matchType) return false
    }
    return true
  })
})

// Stored Records Query
async function fetchStoredRecords() {
  loadingDbRecords.value = true
  try {
    const params = new URLSearchParams()
    if (dbFilterStartDate.value) params.append('startDate', dbFilterStartDate.value)
    if (dbFilterEndDate.value) params.append('endDate', dbFilterEndDate.value)
    if (dbFilterUserId.value) params.append('userId', dbFilterUserId.value)
    if (dbFilterSearch.value) params.append('search', dbFilterSearch.value)

    const res = await fetch(`http://localhost:8080/api/attendance/records?${params.toString()}`)
    if (res.ok) {
      dbRecords.value = await res.json()
    }
  } catch (err) {
    console.error('Error fetching stored records:', err)
  } finally {
    loadingDbRecords.value = false
  }
}

// Generate JasperReports PDF
async function generateAttendanceReportPdf() {
  loadingPdf.value = true
  try {
    const params = new URLSearchParams()
    if (dbFilterStartDate.value) params.append('startDate', dbFilterStartDate.value)
    if (dbFilterEndDate.value) params.append('endDate', dbFilterEndDate.value)
    if (dbFilterUserId.value) params.append('userId', dbFilterUserId.value)
    params.append('disposition', 'inline')

    const res = await fetch(`http://localhost:8080/api/attendance/report/pdf?${params.toString()}`)
    if (!res.ok) {
      throw new Error('Error generando el reporte PDF de asistencia.')
    }

    const blob = await res.blob()
    const url = window.URL.createObjectURL(blob)
    emit('preview-report', {
      url,
      title: 'Reporte Estadístico de Asistencia (Horas, Retardos, Días) - ZKTeco LX50',
      filename: `Reporte_Estadistico_ZKTeco_${Date.now()}.pdf`
    })
    emit('show-toast', { type: 'success', text: 'Reporte JasperReports generado y listo en el visor.' })
  } catch (err) {
    emit('show-toast', { type: 'error', text: err.message })
  } finally {
    loadingPdf.value = false
  }
}

// Helpers
function formatBytes(bytes) {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

function formatTimestamp(ts) {
  if (!ts) return '-'
  return ts.replace('T', ' ').substring(0, 19)
}

function getPunchTypeBadge(type) {
  if (!type) return 'badge-slate'
  if (type === 'ENTRADA') return 'badge-emerald'
  if (type === 'SALIDA') return 'badge-cyan'
  if (type.includes('INTERMEDIA')) return 'badge-indigo'
  return 'badge-slate'
}

function getStatusBadge(status) {
  if (status === 'VALID') return 'badge-emerald'
  if (status === 'WARNING') return 'badge-amber'
  if (status === 'INVALID') return 'badge-rose'
  return 'badge-slate'
}
</script>

<style scoped>
.attendance-manager {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  flex-wrap: wrap;
}

.header-left {
  max-width: 780px;
}

.badge-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.module-title {
  font-size: 1.45rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
  line-height: 1.25;
}

.module-description {
  font-size: 0.92rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

/* KPI Grid */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.kpi-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
}

.kpi-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bg-blue-subtle { background: #EFF6FF; color: #2563EB; }
.bg-emerald-subtle { background: #F0FDF4; color: #16A34A; }
.bg-indigo-subtle { background: #EEF2FF; color: #4F46E5; }
.bg-amber-subtle { background: #FFFBEB; color: #D97706; }

.kpi-data {
  display: flex;
  flex-direction: column;
}

.kpi-val {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.kpi-label {
  font-size: 0.78rem;
  color: var(--text-muted);
  font-weight: 500;
}

/* Nav Subtabs */
.nav-subtabs {
  display: flex;
  gap: 12px;
  border-bottom: 2px solid var(--border-subtle);
  padding-bottom: 2px;
}

.subtab-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 0.92rem;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  margin-bottom: -4px;
  transition: var(--transition);
}

.subtab-btn:hover {
  color: var(--accent-primary);
}

.subtab-btn.active {
  color: var(--accent-primary);
  border-bottom-color: var(--accent-primary);
}

.subtab-counter {
  background: #E2E8F0;
  color: #334155;
  font-size: 0.72rem;
  padding: 2px 7px;
  border-radius: 999px;
  font-weight: 700;
}

.subtab-btn.active .subtab-counter {
  background: #EFF6FF;
  color: var(--accent-primary);
}

/* Dropzone */
.dropzone-card {
  padding: 36px 24px;
}

.dropzone-area {
  border: 2px dashed #CBD5E1;
  border-radius: var(--radius-lg);
  padding: 48px 24px;
  text-align: center;
  background: #F8FAFC;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.dropzone-area:hover, .dropzone-area.is-dragging {
  border-color: var(--accent-primary);
  background: #EFF6FF;
}

.dropzone-icon-box {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #FFFFFF;
  border: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-primary);
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
}

.dropzone-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.dropzone-hint {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-bottom: 20px;
}

.hidden-file-input {
  display: none;
}

.selected-file-chip {
  margin-top: 18px;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  background: #FFFFFF;
  border: 1px solid #BFDBFE;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.file-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-primary);
}

.btn-chip-process {
  background: var(--accent-primary);
  color: #FFFFFF;
  border: none;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  font-size: 0.78rem;
  font-weight: 600;
  cursor: pointer;
}

.format-notes {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-subtle);
}

.format-notes-title {
  font-size: 0.92rem;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.notes-list {
  padding-left: 20px;
  font-size: 0.84rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Preview Section */
.preview-workspace {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.preview-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.file-badge {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-title {
  display: block;
  font-size: 1.05rem;
  color: var(--text-primary);
}

.batch-lbl {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-family: monospace;
}

.preview-counts {
  display: flex;
  gap: 10px;
}

.p-pill {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 6px 14px;
  border-radius: var(--radius-md);
  min-width: 75px;
}

.p-num {
  font-size: 1.15rem;
  font-weight: 700;
  line-height: 1.1;
}

.p-lbl {
  font-size: 0.68rem;
  font-weight: 600;
  text-transform: uppercase;
}

.pill-total { background: #F1F5F9; color: #334155; }
.pill-valid { background: #DCFCE7; color: #15803D; }
.pill-warning { background: #FEF3C7; color: #B45309; }
.pill-invalid { background: #FEE2E2; color: #B91C1C; }

.preview-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn-emerald {
  background: #16A34A;
  color: #FFFFFF;
  border: 1px solid #15803D;
  font-weight: 600;
}
.btn-emerald:hover:not(:disabled) {
  background: #15803D;
}

.btn-clean {
  background: #FFF1F2;
  color: #BE123C;
  border: 1px solid #FECDD3;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-clean:hover:not(:disabled) {
  background: #FFE4E6;
  border-color: #FDA4AF;
  color: #9F1239;
  transform: translateY(-1px);
  box-shadow: 0 2px 5px rgba(190, 18, 60, 0.12);
}

.btn-chip-cancel {
  background: #FEE2E2;
  color: #B91C1C;
  border: none;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.75rem;
  transition: all 0.15s ease;
}

.btn-chip-cancel:hover {
  background: #FCA5A5;
  color: #7F1D1D;
}

.toolbar-search-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-lg {
  padding: 10px 20px;
  font-size: 0.95rem;
}

/* Result Banner */
.result-banner {
  background: #F0FDF4;
  border-color: #86EFAC;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.result-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #DCFCE7;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.result-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: #14532D;
}

.result-message {
  font-size: 0.88rem;
  color: #166534;
}

.result-kpis {
  display: flex;
  gap: 16px;
  background: #FFFFFF;
  border: 1px solid #BBF7D0;
  padding: 12px 20px;
  border-radius: var(--radius-md);
  flex-wrap: wrap;
}

.res-stat {
  display: flex;
  flex-direction: column;
  min-width: 100px;
}

.res-val {
  font-size: 1.25rem;
  font-weight: 700;
}

.res-lbl {
  font-size: 0.72rem;
  color: var(--text-muted);
  text-transform: uppercase;
}

.text-emerald { color: #16A34A; }
.text-amber { color: #D97706; }
.text-rose { color: #DC2626; }

.result-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* Table Toolbar */
.table-card {
  padding: 0;
  overflow: hidden;
}

.table-toolbar {
  padding: 14px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #F8FAFC;
  border-bottom: 1px solid var(--border-subtle);
  flex-wrap: wrap;
  gap: 12px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.ftab {
  background: transparent;
  border: 1px solid var(--border-subtle);
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  color: var(--text-secondary);
}

.ftab.active {
  background: #FFFFFF;
  border-color: var(--accent-primary);
  color: var(--accent-primary);
  box-shadow: var(--shadow-sm);
}

.search-input {
  width: 250px;
  font-size: 0.85rem;
  padding: 6px 12px;
}

/* Tables */
.table-responsive {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
  text-align: left;
}

.data-table th {
  background: #F8FAFC;
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-subtle);
}

.data-table td {
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-subtle);
  vertical-align: middle;
}

.row-warning {
  background-color: #FFFBEB !important;
}

.row-invalid {
  background-color: #FEF2F2 !important;
}

.error-pill-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 0;
}

.err-item-red {
  background: #FEE2E2;
  color: #991B1B;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.73rem;
  font-weight: 500;
  display: inline-block;
}

.err-item-amber {
  background: #FEF3C7;
  color: #92400E;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.73rem;
  font-weight: 500;
  display: inline-block;
}

/* Stored Records Filter Bar */
.filter-bar-card {
  padding: 18px 24px;
}

.filter-controls-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-label {
  font-size: 0.76rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.form-control {
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-sm);
  padding: 7px 12px;
  font-size: 0.85rem;
  outline: none;
  background: #FFFFFF;
}

.form-control:focus {
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.1);
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.card-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #F8FAFC;
  border-bottom: 1px solid var(--border-subtle);
}

.card-subtitle {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
</style>
