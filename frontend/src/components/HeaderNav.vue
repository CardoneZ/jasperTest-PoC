<template>
  <header class="header-nav">
    <div class="container header-container">
      <div class="brand">
        <div class="logo-box">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
            <line x1="16" y1="13" x2="8" y2="13"></line>
            <line x1="16" y1="17" x2="8" y2="17"></line>
            <polyline points="10 9 9 9 8 9"></polyline>
          </svg>
        </div>
        <div>
          <h1 class="brand-title">JasperReports Studio</h1>
          <p class="brand-subtitle">Prueba de Concepto & Integración Tecnológica</p>
        </div>
      </div>

      <div class="system-status">
        <span class="badge badge-emerald">
          <span class="status-dot online"></span>
          Backend :8080 Conectado
        </span>
        <span class="badge badge-cyan">
          <span class="status-dot db"></span>
          Sakila DB :3307
        </span>
        <span class="badge badge-indigo">
          JasperReports 6.21.3
        </span>
      </div>
    </div>
  </header>
</template>

<script setup>
</script>

<style scoped>
.header-nav {
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-subtle);
  position: sticky;
  top: 0;
  z-index: 50;
  padding: 14px 0;
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
  gap: 14px;
}

.logo-box {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.2) 0%, rgba(99, 102, 241, 0.2) 100%);
  border: 1px solid rgba(56, 189, 248, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-cyan);
}

.brand-title {
  font-size: 1.25rem;
  color: var(--text-primary);
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.system-status {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.online {
  background: #10B981;
  box-shadow: 0 0 8px #10B981;
}

.status-dot.db {
  background: #38BDF8;
  box-shadow: 0 0 8px #38BDF8;
}
</style>
