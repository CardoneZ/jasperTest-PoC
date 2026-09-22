<template>
  <div class="app-layout">
    <HeaderNav />

    <main class="main-content container">
      <!-- Hero Section -->
      <section class="hero-section">
        <div class="hero-content">
          <div class="hero-tag">
            <span class="pulse-dot"></span>
            Prueba de Concepto y Validación Tecnológica
          </div>
          <h1 class="hero-title">
            Integración de <span class="gradient-text">JasperReports</span> en Plataformas Web Modernas
          </h1>
          <p class="hero-subtitle">
            Demostración de arquitectura desacoplada para generación de reportes empresariales con Vue 3 en el frontend,
            servicios web independientes en Java (Spring Boot) y base de datos relacional Sakila (MySQL).
          </p>
        </div>

        <div class="quick-kpis">
          <div class="kpi-mini-card">
            <span class="kpi-num">3</span>
            <span class="kpi-lbl">Servicios Web de Reportes</span>
          </div>
          <div class="kpi-mini-card">
            <span class="kpi-num">4</span>
            <span class="kpi-lbl">Procedimientos Almacenados</span>
          </div>
          <div class="kpi-mini-card">
            <span class="kpi-num">100%</span>
            <span class="kpi-lbl">JRXML Nativos Compilados</span>
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
        <p>Prueba de Concepto JasperReports &copy; 2026 | Arquitectura de Servicios Web para Evaluación de Software</p>
        <p class="footer-note">Validación técnica para futura integración en plataforma de Recursos Humanos</p>
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
  padding-top: 36px;
  padding-bottom: 60px;
}

.hero-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 32px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.hero-content {
  max-width: 780px;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--accent-cyan);
  background: rgba(56, 189, 248, 0.1);
  padding: 6px 14px;
  border-radius: 9999px;
  border: 1px solid rgba(56, 189, 248, 0.2);
  margin-bottom: 16px;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-cyan);
  box-shadow: 0 0 10px var(--accent-cyan);
}

.hero-title {
  font-size: 2.4rem;
  line-height: 1.15;
  color: #FFFFFF;
  margin-bottom: 14px;
}

.gradient-text {
  background: linear-gradient(135deg, #38BDF8 0%, #818CF8 50%, #C084FC 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 1.05rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

.quick-kpis {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.kpi-mini-card {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  min-width: 130px;
}

.kpi-num {
  font-family: var(--font-heading);
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--accent-cyan);
  line-height: 1;
}

.kpi-lbl {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-top: 6px;
}

.app-footer {
  border-top: 1px solid var(--border-subtle);
  background: rgba(15, 23, 42, 0.9);
  padding: 24px 0;
  margin-top: auto;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.footer-note {
  color: var(--text-secondary);
  font-style: italic;
}
</style>
