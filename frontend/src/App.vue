<template>
  <div class="app-layout">
    <HeaderNav />

    <main class="main-content container">
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
            <span class="kpi-num">3</span>
            <span class="kpi-lbl">Servicios de Reportes</span>
          </div>
          <div class="kpi-mini-card">
            <span class="kpi-num">4</span>
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
    </main>

    <!-- PDF Viewer Modal -->
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
        <p class="footer-note">Integración para módulo de Recursos Humanos y Reportes</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import HeaderNav from './components/HeaderNav.vue'
import ReportControlPanel from './components/ReportControlPanel.vue'
import FilmCatalog from './components/FilmCatalog.vue'
import PdfViewerModal from './components/PdfViewerModal.vue'
import ToastNotification from './components/ToastNotification.vue'

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

.hero-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.hero-content {
  max-width: 760px;
}

.hero-tag {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 600;
  color: #0369A1;
  background: #E0F2FE;
  padding: 3px 8px;
  border-radius: 4px;
  border: 1px solid #BAE6FD;
  margin-bottom: 10px;
}

.hero-title {
  font-size: 1.625rem;
  font-weight: 700;
  line-height: 1.25;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.hero-subtitle {
  font-size: 0.9375rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.quick-kpis {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.kpi-mini-card {
  background: #FFFFFF;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  min-width: 115px;
  box-shadow: var(--shadow-sm);
}

.kpi-num {
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--accent-primary);
  line-height: 1.1;
}

.kpi-lbl {
  font-size: 0.6875rem;
  color: var(--text-muted);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  margin-top: 4px;
}

.app-footer {
  border-top: 1px solid var(--border-subtle);
  background: #FFFFFF;
  padding: 16px 0;
  margin-top: auto;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 0.8125rem;
  color: var(--text-muted);
}

.footer-note {
  color: var(--text-secondary);
}
</style>
