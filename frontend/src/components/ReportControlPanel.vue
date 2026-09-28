<template>
  <div class="report-panel card">
    <div class="panel-header">
      <div>
        <div class="badge badge-indigo">Servicios Web de Reportes</div>
        <h2 class="panel-title">Centro de Generación JasperReports</h2>
        <p class="panel-desc">
          Tres servicios web independientes integrados con plantillas <code>.jrxml</code> y procedimientos almacenados en MySQL.
        </p>
      </div>
    </div>

    <div class="reports-grid">
      <!-- REPORTE 1: General -->
      <div class="report-card">
        <div class="card-top">
          <div class="report-icon r1">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
            </svg>
          </div>
          <div>
            <span class="badge badge-cyan">Servicio Web 1</span>
            <h3 class="report-name">Reporte General de Catálogo</h3>
          </div>
        </div>
        <p class="report-text">
          Listado tabular corporativo de inventario con filtros de categoría y clasificación, totales de títulos, promedios y paginación.
        </p>

        <div class="report-params">
          <div class="form-group">
            <label class="form-label" for="r1-category">Categoría:</label>
            <select id="r1-category" v-model="r1Category" class="form-select">
              <option value="0">Todas las Categorías</option>
              <option v-for="cat in categories" :key="cat.categoryId" :value="cat.categoryId">
                {{ cat.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label" for="r1-rating">Clasificación:</label>
            <select id="r1-rating" v-model="r1Rating" class="form-select">
              <option value="ALL">Todas las Clasificaciones</option>
              <option value="G">G</option>
              <option value="PG">PG</option>
              <option value="PG-13">PG-13</option>
              <option value="R">R</option>
              <option value="NC-17">NC-17</option>
            </select>
          </div>
        </div>

        <div class="card-actions">
          <button 
            id="btn-r1-preview"
            class="btn btn-primary" 
            :disabled="r1Loading" 
            @click="handleGenerate('r1', 'inline')">
            <svg v-if="!r1Loading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
            <span v-else class="spinner"></span>
            Vista Previa
          </button>
          <button 
            id="btn-r1-download"
            class="btn btn-secondary" 
            :disabled="r1Loading" 
            @click="handleGenerate('r1', 'attachment')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
              <polyline points="7 10 12 15 17 10"></polyline>
              <line x1="12" y1="15" x2="12" y2="3"></line>
            </svg>
            Descargar
          </button>
        </div>
      </div>

      <!-- REPORTE 2: Agrupado -->
      <div class="report-card">
        <div class="card-top">
          <div class="report-icon r2">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"></rect>
              <rect x="14" y="3" width="7" height="7"></rect>
              <rect x="14" y="14" width="7" height="7"></rect>
              <rect x="3" y="14" width="7" height="7"></rect>
            </svg>
          </div>
          <div>
            <span class="badge badge-indigo">Servicio Web 2</span>
            <h3 class="report-name">Reporte Agrupado por Género</h3>
          </div>
        </div>
        <p class="report-text">
          Agrupación jerárquica con encabezados de categoría, subtotales de rentas e ingresos acumulados y resumen general al pie.
        </p>

        <div class="report-params">
          <div class="form-group">
            <label class="form-label" for="r2-category">Categoría:</label>
            <select id="r2-category" v-model="r2Category" class="form-select">
              <option value="0">Todas las Categorías</option>
              <option v-for="cat in categories" :key="cat.categoryId" :value="cat.categoryId">
                {{ cat.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label" for="r2-min-rentals">Mínimo de Rentas:</label>
            <input 
              id="r2-min-rentals"
              type="number" 
              v-model.number="r2MinRentals" 
              min="0" 
              max="50" 
              class="form-control" 
              placeholder="Ej. 10" />
          </div>
        </div>

        <div class="card-actions">
          <button 
            id="btn-r2-preview"
            class="btn btn-primary" 
            :disabled="r2Loading" 
            @click="handleGenerate('r2', 'inline')">
            <svg v-if="!r2Loading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
            <span v-else class="spinner"></span>
            Vista Previa
          </button>
          <button 
            id="btn-r2-download"
            class="btn btn-secondary" 
            :disabled="r2Loading" 
            @click="handleGenerate('r2', 'attachment')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
              <polyline points="7 10 12 15 17 10"></polyline>
              <line x1="12" y1="15" x2="12" y2="3"></line>
            </svg>
            Descargar
          </button>
        </div>
      </div>

      <!-- REPORTE 3: Analítica y Gráficos -->
      <div class="report-card">
        <div class="card-top">
          <div class="report-icon r3">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21.21 15.89A10 10 0 1 1 8 2.83"></path>
              <path d="M22 12A10 10 0 0 0 12 2v10z"></path>
            </svg>
          </div>
          <div>
            <span class="badge badge-emerald">Servicio Web 3</span>
            <h3 class="report-name">Dashboard Ejecutivo & Gráfico</h3>
          </div>
        </div>
        <p class="report-text">
          Formato analítico con gráfico de pastel (JFreeChart) nativo, métricas de rendimiento por categoría y tarjetas de resumen KPI.
        </p>

        <div class="report-params">
          <div class="form-group">
            <label class="form-label" for="r3-store">Sucursal / Tienda:</label>
            <select id="r3-store" v-model="r3Store" class="form-select">
              <option value="0">Todas las Sucursales</option>
              <option value="1">Tienda 1 (Store 1)</option>
              <option value="2">Tienda 2 (Store 2)</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label" for="r3-limit">Límite de Categorías:</label>
            <input 
              id="r3-limit"
              type="number" 
              v-model.number="r3Limit" 
              min="5" 
              max="16" 
              class="form-control" 
              placeholder="16" />
          </div>
        </div>

        <div class="card-actions">
          <button 
            id="btn-r3-preview"
            class="btn btn-primary" 
            :disabled="r3Loading" 
            @click="handleGenerate('r3', 'inline')">
            <svg v-if="!r3Loading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
            <span v-else class="spinner"></span>
            Vista Previa
          </button>
          <button 
            id="btn-r3-download"
            class="btn btn-secondary" 
            :disabled="r3Loading" 
            @click="handleGenerate('r3', 'attachment')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
              <polyline points="7 10 12 15 17 10"></polyline>
              <line x1="12" y1="15" x2="12" y2="3"></line>
            </svg>
            Descargar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  categories: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['preview-report', 'show-toast'])

// Form models
const r1Category = ref(0)
const r1Rating = ref('ALL')
const r1Loading = ref(false)

const r2Category = ref(0)
const r2MinRentals = ref(10)
const r2Loading = ref(false)

const r3Store = ref(0)
const r3Limit = ref(16)
const r3Loading = ref(false)

const API_BASE = 'http://localhost:8080/api/reports'

async function handleGenerate(reportType, disposition) {
  let url = ''
  let filename = ''
  let title = ''

  if (reportType === 'r1') {
    r1Loading.value = true
    url = `${API_BASE}/general?categoryId=${r1Category.value}&rating=${r1Rating.value}&disposition=${disposition}`
    filename = 'Reporte_General_Peliculas.pdf'
    title = 'Reporte General de Catálogo de Películas'
  } else if (reportType === 'r2') {
    r2Loading.value = true
    url = `${API_BASE}/grouped?categoryId=${r2Category.value}&minRentals=${r2MinRentals.value}&disposition=${disposition}`
    filename = 'Reporte_Agrupado_Rentas.pdf'
    title = 'Reporte Agrupado de Rentas por Categoría'
  } else if (reportType === 'r3') {
    r3Loading.value = true
    url = `${API_BASE}/analytics?storeId=${r3Store.value}&limit=${r3Limit.value}&disposition=${disposition}`
    filename = 'Dashboard_Analitica_Ejecutiva.pdf'
    title = 'Dashboard Ejecutivo y Gráfico de Analítica'
  }

  try {
    const res = await fetch(url, { method: 'POST' })
    if (!res.ok) {
      throw new Error(`Error en el servidor: HTTP ${res.status}`)
    }
    const blob = await res.blob()

    if (disposition === 'attachment') {
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
      emit('show-toast', { type: 'success', text: `Descarga de ${filename} iniciada.` })
    } else {
      const blobUrl = window.URL.createObjectURL(blob)
      emit('preview-report', { url: blobUrl, title, filename })
      emit('show-toast', { type: 'info', text: `Visualizando ${title}.` })
    }
  } catch (err) {
    emit('show-toast', { type: 'error', text: `No se pudo generar el reporte: ${err.message}` })
  } finally {
    if (reportType === 'r1') r1Loading.value = false
    if (reportType === 'r2') r2Loading.value = false
    if (reportType === 'r3') r3Loading.value = false
  }
}
</script>

<style scoped>
.report-panel {
  margin-bottom: 24px;
}

.panel-header {
  margin-bottom: 16px;
}

.panel-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-top: 4px;
}

.panel-desc {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin-top: 4px;
}

.panel-desc code {
  color: #0369A1;
  background: #E0F2FE;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.85em;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.reports-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(310px, 1fr));
  gap: 16px;
}

.report-card {
  background: #FFFFFF;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-sm);
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
}

.report-card:hover {
  border-color: #CBD5E1;
}

.card-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.report-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.report-icon.r1 {
  background: #EFF6FF;
  color: #2563EB;
  border: 1px solid #DBEAFE;
}

.report-icon.r2 {
  background: #EEF2FF;
  color: #4F46E5;
  border: 1px solid #E0E7FF;
}

.report-icon.r3 {
  background: #ECFDF5;
  color: #059669;
  border: 1px solid #D1FAE5;
}

.report-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-top: 2px;
}

.report-text {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.4;
  margin-bottom: 12px;
}

.report-params {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
  background: #F8FAFC;
  padding: 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-subtle);
}

.card-actions {
  display: flex;
  gap: 8px;
}

.card-actions .btn {
  flex: 1;
}

.spinner {
  width: 13px;
  height: 13px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #FFFFFF;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
