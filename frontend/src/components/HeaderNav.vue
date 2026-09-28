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
          <h1 class="brand-title">JasperReports</h1>
          <p class="brand-subtitle">Módulo de Reportes</p>
        </div>
      </div>

      <div class="system-status">
        <span class="badge badge-emerald">
          <span class="status-dot online"></span>
          Backend :8080 Conectado
        </span>
        <span class="badge badge-cyan">
          <span class="status-dot db"></span>
          MySQL :3306
        </span>
        <span class="badge badge-slate">
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
  background: #FFFFFF;
  border-bottom: 1px solid var(--border-subtle);
  position: sticky;
  top: 0;
  z-index: 50;
  padding: 10px 0;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
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
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 0.75rem;
  color: var(--text-secondary);
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
