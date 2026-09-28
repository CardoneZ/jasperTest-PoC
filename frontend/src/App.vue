<template>
  <div class="app-layout">
    <HeaderNav :active-tab="activeTab" @change-tab="(tab) => activeTab = tab" />

    <main class="main-content container">
      <!-- MÓDULO 1: ASISTENCIA Y CHECADOR ZKTECO LX50 -->
      <section v-if="activeTab === 'attendance'" id="section-attendance">
        <AttendanceManager 
          @preview-report="openPdfModal" 
          @show-toast="addToast" />
      </section>

      <!-- MÓDULO 2: REPORTES JASPER Y CATÁLOGO SAKILA -->
      <div v-else-if="activeTab === 'reports'" class="reports-workspace">
        <!-- Hero Section -->
        <section class="hero-section">
          <div class="hero-content">
            <div class="hero-tag">
              Prueba de Concepto y Validación Tecnológica
            </div>
            <h1 class="hero-title">
              Integración de JasperReports en Plataformas Web
            </h1>
            <p class="hero-subtitle">
              Demostración técnica de generación y exportación de reportes corporativos con Vue 3, Spring Boot y base de datos relacional Sakila (MySQL).
            </p>
          </div>

          <div class="quick-kpis">
            <div class="kpi-mini-card">
              <span class="kpi-num">4</span>
              <span class="kpi-lbl">Servicios de Reportes</span>
            </div>
            <div class="kpi-mini-card">
              <span class="kpi-num">5</span>
              <span class="kpi-lbl">Procedimientos SQL</span>
            </div>
            <div class="kpi-mini-card">
              <span class="kpi-num">100%</span>
              <span class="kpi-lbl">Plantillas JRXML</span>
            </div>
          </div>
        </section>

        <!-- Section 1: Report Generation Center -->
        <section id="section-reports">
          <ReportControlPanel 
            :categories="categories" 
            @preview-report="openPdfModal" 
            @show-toast="addToast" />
        </section>

        <!-- Section 2: Catalog Consultation & Visualization -->
        <section id="section-catalog">
          <FilmCatalog 
            :categories="categories" />
        </section>
      </div>
    </main>

    <!-- PDF Viewer Modal (Compartido para todos los reportes Jasper) -->
    <PdfViewerModal 
      :is-open="isModalOpen" 
      :pdf-url="modalPdfUrl" 
      :report-title="modalReportTitle" 
      :filename="modalFilename" 
      @close="closePdfModal" />

    <!-- Toast Notifications -->
    <ToastNotification :toasts="toasts" />

    <!-- Footer -->
    <footer class="app-footer">
      <div class="container footer-content">
        <p>Prueba de Concepto JasperReports &copy; 2026 | Arquitectura de Servicios Web para Evaluación Técnica</p>
        <p class="footer-note">Integración para módulo de Recursos Humanos, Asistencia ZKTeco LX50 y Reportes</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import HeaderNav from './components/HeaderNav.vue'
import AttendanceManager from './components/AttendanceManager.vue'
import ReportControlPanel from './components/ReportControlPanel.vue'
import FilmCatalog from './components/FilmCatalog.vue'
import PdfViewerModal from './components/PdfViewerModal.vue'
import ToastNotification from './components/ToastNotification.vue'

// Active Tab ('attendance' | 'reports')
const activeTab = ref('attendance')
const categories = ref([])

// Modal State
const isModalOpen = ref(false)
const modalPdfUrl = ref('')
const modalReportTitle = ref('')
const modalFilename = ref('')

// Toast State
const toasts = ref([])
let toastCounter = 0

onMounted(async () => {
  try {
    const res = await fetch('http://localhost:8080/api/categories')
    if (res.ok) {
      categories.value = await res.json()
    }
  } catch (err) {
    console.error('Error loading categories:', err)
  }
})

function openPdfModal({ url, title, filename }) {
  modalPdfUrl.value = url
  modalReportTitle.value = title
  modalFilename.value = filename
  isModalOpen.value = true
}

function closePdfModal() {
  isModalOpen.value = false
  if (modalPdfUrl.value) {
    window.URL.revokeObjectURL(modalPdfUrl.value)
    modalPdfUrl.value = ''
  }
}

function addToast({ type, text }) {
  const id = ++toastCounter
  toasts.value.push({ id, type, text })
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }, 4000)
}
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  padding-top: 24px;
  padding-bottom: 40px;
}

.reports-workspace {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.hero-content {
  max-width: 760px;
}

.hero-tag {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--accent-primary);
  background: #EFF6FF;
  border: 1px solid #BFDBFE;
  padding: 3px 10px;
  border-radius: 999px;
  margin-bottom: 10px;
}

.hero-title {
  font-size: 1.85rem;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.hero-subtitle {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.quick-kpis {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.kpi-mini-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: 12px 18px;
  min-width: 120px;
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.kpi-num {
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--accent-primary);
  line-height: 1.1;
}

.kpi-lbl {
  font-size: 0.72rem;
  color: var(--text-muted);
  font-weight: 500;
}

.app-footer {
  background: #FFFFFF;
  border-top: 1px solid var(--border-subtle);
  padding: 16px 0;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.footer-note {
  font-size: 0.75rem;
  color: var(--text-secondary);
}
</style>
