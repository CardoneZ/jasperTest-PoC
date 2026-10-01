<template>
  <div class="date-input-container">
    <input 
      type="text" 
      class="form-control date-display-input" 
      :value="displayValue" 
      :placeholder="placeholder" 
      maxlength="10"
      @input="handleInput" 
      @blur="handleBlur"
    />
    <div class="calendar-btn-wrapper" title="Seleccionar fecha" @click="triggerNativePicker">
      <svg class="calendar-svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
        <line x1="16" y1="2" x2="16" y2="6"></line>
        <line x1="8" y1="2" x2="8" y2="6"></line>
        <line x1="3" y1="10" x2="21" y2="10"></line>
      </svg>
      <input 
        type="date" 
        ref="nativePicker" 
        class="native-date-overlay" 
        :value="isoValue" 
        @change="handleNativeChange" 
        tabindex="-1"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: 'dd / mm / aaaa'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const nativePicker = ref(null)
const displayValue = ref('')

function toDisplay(val) {
  if (!val) return ''
  const str = String(val).trim()
  // YYYY-MM-DD
  const m1 = str.match(/^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})$/)
  if (m1) {
    return `${m1[3].padStart(2, '0')}/${m1[2].padStart(2, '0')}/${m1[1]}`
  }
  // DD/MM/YYYY or DD-MM-YYYY
  const m2 = str.match(/^(\d{1,2})[-/.](\d{1,2})[-/.](\d{4})$/)
  if (m2) {
    return `${m2[1].padStart(2, '0')}/${m2[2].padStart(2, '0')}/${m2[3]}`
  }
  return str
}

function toIso(val) {
  if (!val) return ''
  const str = String(val).trim()
  // DD/MM/YYYY or DD-MM-YYYY
  const m1 = str.match(/^(\d{1,2})[-/.](\d{1,2})[-/.](\d{4})$/)
  if (m1) {
    return `${m1[3]}-${m1[2].padStart(2, '0')}-${m1[1].padStart(2, '0')}`
  }
  // YYYY-MM-DD
  const m2 = str.match(/^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})$/)
  if (m2) {
    return `${m2[1]}-${m2[2].padStart(2, '0')}-${m2[3].padStart(2, '0')}`
  }
  return ''
}

const isoValue = computed(() => toIso(props.modelValue || displayValue.value))

watch(() => props.modelValue, (newVal) => {
  displayValue.value = toDisplay(newVal)
}, { immediate: true })

function triggerNativePicker() {
  if (nativePicker.value) {
    if (typeof nativePicker.value.showPicker === 'function') {
      try {
        nativePicker.value.showPicker()
        return
      } catch (e) {}
    }
    nativePicker.value.focus()
  }
}

function handleNativeChange(e) {
  const val = e.target.value
  if (val) {
    displayValue.value = toDisplay(val)
    emit('update:modelValue', val)
    emit('change', val)
  } else {
    displayValue.value = ''
    emit('update:modelValue', '')
    emit('change', '')
  }
}

function handleInput(e) {
  let val = e.target.value.replace(/[^\d/]/g, '')
  if (val.length === 2 && !val.includes('/')) {
    val = val + '/'
  } else if (val.length === 5 && val.split('/').length === 2) {
    val = val + '/'
  }
  displayValue.value = val

  if (val.length === 10) {
    const iso = toIso(val)
    if (iso) {
      emit('update:modelValue', iso)
      emit('change', iso)
    }
  } else if (val === '') {
    emit('update:modelValue', '')
    emit('change', '')
  }
}

function handleBlur() {
  if (displayValue.value && displayValue.value.length === 10) {
    const iso = toIso(displayValue.value)
    if (iso) {
      emit('update:modelValue', iso)
      emit('change', iso)
    }
  } else if (!displayValue.value) {
    emit('update:modelValue', '')
    emit('change', '')
  }
}
</script>

<style scoped>
.date-input-container {
  position: relative;
  display: flex;
  align-items: center;
  width: 145px;
}

.date-display-input {
  width: 100%;
  padding-right: 32px !important;
  font-family: inherit;
  font-size: 0.85rem;
}

.calendar-btn-wrapper {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 2;
}

.calendar-svg {
  color: var(--text-muted, #64748b);
  pointer-events: none;
}

.calendar-btn-wrapper:hover .calendar-svg {
  color: var(--accent-primary, #2563eb);
}

.native-date-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  border: none;
  margin: 0;
  padding: 0;
}
</style>
