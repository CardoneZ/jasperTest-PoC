<template>
  <div v-if="isOpen" class="modal-backdrop" @click.self="closeModal">
    <div class="modal-dialog">
      <!-- Modal Header -->
      <div class="modal-header">
        <div class="header-info">
          <div class="badge badge-cyan">Visualizador JasperReports</div>
          <h3 class="modal-title">{{ reportTitle }}</h3>
        </div>

        <div class="header-actions">
          <button id="btn-modal-download" class="btn btn-primary btn-sm" @click="downloadPdf">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
              <polyline points="7 10 12 15 17 10"></polyline>
              <line x1="12" y1="15" x2="12" y2="3"></line>
            </svg>
            Descargar Archivo
          </button>
          <a id="btn-modal-tab" :href="pdfUrl" target="_blank" class="btn btn-secondary btn-sm" title="Abrir en nueva pestaña">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
              <polyline points="15 3 21 3 21 9"></polyline>
              <line x1="10" y1="14" x2="21" y2="3"></line>
            </svg>
            Pestaña Nueva
          </a>
          <button id="btn-modal-close" class="btn-close" @click="closeModal" title="Cerrar (Esc)">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
      </div>

      <!-- Modal Body (PDF Frame) -->
      <div class="modal-body">
        <iframe 
          v-if="pdfUrl" 
          :src="pdfUrl" 
          class="pdf-frame" 
          title="Vista previa del reporte PDF">
        </iframe>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'

const props = defineProps({
  isOpen: Boolean,
  pdfUrl: String,
  reportTitle: String,
  filename: String
})

const emit = defineEmits(['close'])

function closeModal() {
  emit('close')
}

function downloadPdf() {
  if (!props.pdfUrl) return
  const link = document.createElement('a')
  link.href = props.pdfUrl
  link.download = props.filename || 'reporte.pdf'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

function handleKeydown(e) {
  if (e.key === 'Escape' && props.isOpen) {
    closeModal()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 20px;
  animation: fadeIn 0.15s ease-out;
}

.modal-dialog {
  background: #FFFFFF;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  width: 95vw;
  max-width: 1200px;
  height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  animation: scaleUp 0.15s ease-out;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: #FFFFFF;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-title {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-top: 3px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-close {
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);
}

.btn-close:hover {
  background: #F1F5F9;
  color: var(--text-primary);
}

.modal-body {
  flex: 1;
  background: #F1F5F9;
  position: relative;
}

.pdf-frame {
  width: 100%;
  height: 100%;
  border: none;
  background: #FFFFFF;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleUp {
  from { transform: scale(0.98); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>
