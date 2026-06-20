<script setup>
defineProps({
  icon: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    default: '--'
  },
  hint: {
    type: String,
    default: ''
  },
  trend: {
    type: String,
    default: ''
  },
  tone: {
    type: String,
    default: 'blue',
    validator: (value) => ['blue', 'green', 'purple', 'orange', 'red'].includes(value)
  }
})
</script>

<template>
  <article class="metric-card" :class="`metric-card--${tone}`">
    <div class="metric-card__icon" aria-hidden="true">
      <slot name="icon">
        <span>{{ icon }}</span>
      </slot>
    </div>
    <div class="metric-card__body">
      <span class="metric-card__label">{{ label }}</span>
      <strong class="metric-card__value">{{ value }}</strong>
      <span v-if="hint || trend" class="metric-card__hint">
        <span v-if="hint">{{ hint }}</span>
        <b v-if="trend">{{ trend }}</b>
      </span>
    </div>
  </article>
</template>
