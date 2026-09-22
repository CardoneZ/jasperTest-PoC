<template>
  <div class="film-catalog card">
    <div class="catalog-header">
      <div>
        <div class="badge badge-cyan">Consulta & Visualización de Información</div>
        <h2 class="catalog-title">Catálogo Cinematográfico (Sakila DB)</h2>
        <p class="catalog-desc">
          Datos provistos en tiempo real mediante el servicio web <code>GET /api/films</code> y el procedimiento almacenado <code>sp_get_film_catalog</code>.
        </p>
      </div>

      <div class="count-badge">
        <span class="badge badge-slate">
          {{ filteredFilms.length }} resultados encontrados
        </span>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <div class="search-box">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
        <input 
          id="search-input"
          type="text" 
          v-model="searchQuery" 
          class="form-control search-input" 
          placeholder="Buscar por título o descripción..." 
          @input="onFilterChange" />
      </div>

      <div class="filter-group">
        <select id="select-category" v-model="selectedCategory" class="form-select filter-select" @change="onFilterChange">
          <option value="0">Todas las Categorías</option>
          <option v-for="cat in categories" :key="cat.categoryId" :value="cat.categoryId">
            {{ cat.name }}
          </option>
        </select>
      </div>

      <div class="filter-group">
        <select id="select-rating" v-model="selectedRating" class="form-select filter-select" @change="onFilterChange">
          <option value="ALL">Todas las Clasificaciones</option>
          <option value="G">G (General)</option>
          <option value="PG">PG (Guía Paternal)</option>
          <option value="PG-13">PG-13 (Mayores 13)</option>
          <option value="R">R (Restringido)</option>
          <option value="NC-17">NC-17 (Adultos)</option>
        </select>
      </div>

      <button id="btn-reset-filters" class="btn btn-secondary" @click="resetFilters" title="Restablecer Filtros">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
          <path d="M3 3v5h5"></path>
        </svg>
        Limpiar
      </button>
    </div>

    <!-- Data Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th>Título</th>
            <th>Categoría</th>
            <th>Idioma</th>
            <th class="text-center">Clasificación</th>
            <th class="text-right">Duración</th>
            <th class="text-right">Tarifa Renta</th>
            <th class="text-right">Reemplazo</th>
            <th class="text-right">Total Rentas</th>
          </tr>
        </thead>
        <tbody v-if="loading">
          <tr v-for="n in 6" :key="n" class="skeleton-row">
            <td colspan="9">
              <div class="skeleton-bar"></div>
            </td>
          </tr>
        </tbody>
        <tbody v-else-if="paginatedFilms.length === 0">
          <tr>
            <td colspan="9" class="no-data">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="8" y1="12" x2="16" y2="12"></line>
              </svg>
              <p>No se encontraron películas con los criterios especificados.</p>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr v-for="film in paginatedFilms" :key="film.filmId" class="film-row">
            <td class="col-id font-mono">{{ film.filmId }}</td>
            <td>
              <div class="film-title">{{ film.title }}</div>
              <div class="film-desc" :title="film.description">{{ film.description }}</div>
            </td>
            <td>
              <span class="category-tag">{{ film.categoryName || 'Sin categoría' }}</span>
            </td>
            <td class="text-secondary">{{ film.languageName }}</td>
            <td class="text-center">
              <span class="badge" :class="getRatingBadgeClass(film.rating)">
                {{ film.rating }}
              </span>
            </td>
            <td class="text-right font-mono">{{ film.length }} min</td>
            <td class="text-right font-mono text-emerald font-bold">
              ${{ Number(film.rentalRate).toFixed(2) }}
            </td>
            <td class="text-right font-mono text-secondary">
              ${{ Number(film.replacementCost).toFixed(2) }}
            </td>
            <td class="text-right font-mono text-cyan font-bold">
              {{ film.totalRentals }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination Controls -->
    <div class="pagination-bar" v-if="totalPages > 1">
      <div class="page-info">
        Página {{ currentPage }} de {{ totalPages }} (mostrando {{ paginatedFilms.length }} registros)
      </div>
      <div class="page-buttons">
        <button 
          id="btn-prev-page"
          class="btn btn-secondary btn-sm" 
          :disabled="currentPage === 1" 
          @click="currentPage--">
          Anterior
        </button>
        <button 
          id="btn-next-page"
          class="btn btn-secondary btn-sm" 
          :disabled="currentPage >= totalPages" 
          @click="currentPage++">
          Siguiente
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  categories: {
    type: Array,
    default: () => []
  }
})

const films = ref([])
const loading = ref(false)
const searchQuery = ref('')
const selectedCategory = ref(0)
const selectedRating = ref('ALL')

const currentPage = ref(1)
const pageSize = 12

let debounceTimer = null

onMounted(() => {
  fetchFilms()
})

async function fetchFilms() {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (selectedCategory.value > 0) params.append('categoryId', selectedCategory.value)
    if (selectedRating.value && selectedRating.value !== 'ALL') params.append('rating', selectedRating.value)
    if (searchQuery.value.trim()) params.append('search', searchQuery.value.trim())

    const res = await fetch(`http://localhost:8080/api/films?${params.toString()}`)
    if (!res.ok) throw new Error('Error al consultar catálogo')
    films.value = await res.json()
    currentPage.value = 1
  } catch (err) {
    console.error('Error fetching films:', err)
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    fetchFilms()
  }, 250)
}

function resetFilters() {
  searchQuery.value = ''
  selectedCategory.value = 0
  selectedRating.value = 'ALL'
  fetchFilms()
}

const filteredFilms = computed(() => films.value)

const totalPages = computed(() => {
  return Math.ceil(filteredFilms.value.length / pageSize) || 1
})

const paginatedFilms = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredFilms.value.slice(start, start + pageSize)
})

function getRatingBadgeClass(rating) {
  switch (rating) {
    case 'G': return 'badge-emerald'
    case 'PG': return 'badge-cyan'
    case 'PG-13': return 'badge-amber'
    case 'R': return 'badge-rose'
    case 'NC-17': return 'badge-rose'
    default: return 'badge-slate'
  }
}
</script>

<style scoped>
.film-catalog {
  margin-bottom: 40px;
}

.catalog-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
}

.catalog-title {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin-top: 6px;
}

.catalog-desc {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-top: 4px;
}

.catalog-desc code {
  color: var(--accent-cyan);
  background: rgba(56, 189, 248, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.85em;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 24px;
  background: rgba(15, 23, 42, 0.5);
  padding: 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
}

.search-box {
  position: relative;
  flex: 1 1 280px;
  display: flex;
  align-items: center;
}

.search-box svg {
  position: absolute;
  left: 12px;
  color: var(--text-muted);
}

.search-input {
  padding-left: 38px;
}

.filter-select {
  min-width: 190px;
}

.table-container {
  overflow-x: auto;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: rgba(15, 23, 42, 0.4);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.data-table th {
  background: rgba(30, 41, 59, 0.8);
  color: var(--text-secondary);
  font-weight: 600;
  text-align: left;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  white-space: nowrap;
  font-family: var(--font-heading);
  letter-spacing: 0.02em;
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  vertical-align: middle;
}

.film-row:hover {
  background: rgba(51, 65, 85, 0.4);
}

.col-id {
  width: 60px;
  color: var(--text-muted);
}

.font-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.film-title {
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.film-desc {
  font-size: 0.75rem;
  color: var(--text-muted);
  max-width: 320px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}

.category-tag {
  color: var(--accent-cyan);
  font-weight: 500;
}

.text-center { text-align: center; }
.text-right { text-align: right; }
.text-secondary { color: var(--text-secondary); }
.text-emerald { color: #34D399; }
.text-cyan { color: var(--accent-cyan); }
.font-bold { font-weight: 700; }

.no-data {
  text-align: center;
  padding: 48px 16px;
  color: var(--text-muted);
}

.no-data svg {
  margin-bottom: 12px;
  stroke: var(--text-muted);
}

.skeleton-row td {
  padding: 16px;
}

.skeleton-bar {
  height: 16px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.04) 25%, rgba(255, 255, 255, 0.08) 50%, rgba(255, 255, 255, 0.04) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 8px 0;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.page-buttons {
  display: flex;
  gap: 8px;
}
</style>
