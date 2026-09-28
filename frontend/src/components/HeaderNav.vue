<template>
  <header class="header-nav">
    <div class="container header-container">
      <div class="brand">
        <div class="logo-box">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
            <line x1="16" y1="13" x2="8" y2="13"></line>
            <line x1="16" y1="17" x2="8" y2="17"></line>
            <polyline points="10 9 9 9 8 9"></polyline>
          </svg>
        </div>
        <div>
          <h1 class="brand-title">JasperReports & RRHH</h1>
          <p class="brand-subtitle">Plataforma de Evaluación Técnica</p>
        </div>
      </div>

      <!-- Navigation Tabs -->
      <nav class="nav-links">
        <button 
          class="nav-tab-btn" 
          :class="{ active: activeTab === 'reports' }" 
          @click="$emit('change-tab', 'reports')">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
            <line x1="12" y1="22.08" x2="12" y2="12"></line>
          </svg>
          Reportes Jasper (Sakila)
        </button>

        <button 
          class="nav-tab-btn" 
          :class="{ active: activeTab === 'attendance' }" 
          @click="$emit('change-tab', 'attendance')">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
          </svg>
          Checador ZKTeco LX50
          <span class="nav-badge-pill">PoC Asistencia</span>
        </button>
      </nav>

      <div class="system-status">
        <span class="badge badge-emerald">
          <span class="status-dot online"></span>
          Backend :8080
        </span>
        <span class="badge badge-cyan">
          <span class="status-dot db"></span>
          MySQL :3306
        </span>
      </div>
    </div>
  </header>
</template>

<script setup>
defineProps({
  activeTab: {
    type: String,
    default: 'attendance'
  }
})

defineEmits(['change-tab'])
</script>

<style scoped>
.header-nav {
  background: #FFFFFF;
  border-bottom: 1px solid var(--border-subtle);
  position: sticky;
  top: 0;
  z-index: 50;
  padding: 8px 0;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-box {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background: #EFF6FF;
  border: 1px solid #BFDBFE;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-primary);
}

.brand-title {
  font-size: 1.02rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 0.73rem;
  color: var(--text-secondary);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #F1F5F9;
  padding: 4px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
}

.nav-tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);
}

.nav-tab-btn:hover {
  color: var(--text-primary);
}

.nav-tab-btn.active {
  background: #FFFFFF;
  color: var(--accent-primary);
  box-shadow: var(--shadow-sm);
}

.nav-badge-pill {
  background: #DCFCE7;
  color: #15803D;
  font-size: 0.68rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 999px;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.online {
  background: #16A34A;
}

.status-dot.db {
  background: #0284C7;
}
</style>
