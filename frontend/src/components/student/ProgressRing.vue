<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: {
    type: Number,
    default: 0
  },
  max: {
    type: Number,
    default: 100
  },
  size: {
    type: Number,
    default: 116
  },
  stroke: {
    type: Number,
    default: 10
  },
  label: {
    type: String,
    default: ''
  },
  caption: {
    type: String,
    default: ''
  },
  tone: {
    type: String,
    default: 'blue',
    validator: (value) => ['blue', 'green', 'purple', 'orange', 'red'].includes(value)
  }
})

const radius = computed(() => (props.size - props.stroke) / 2)
const center = computed(() => props.size / 2)
const circumference = computed(() => 2 * Math.PI * radius.value)
const percent = computed(() => {
  const max = props.max > 0 ? props.max : 100
  const raw = (props.value / max) * 100
  return Math.max(0, Math.min(100, raw))
})
const dashOffset = computed(() => circumference.value * (1 - percent.value / 100))
const displayLabel = computed(() => props.label || `${Math.round(percent.value)}%`)
</script>

<template>
  <div
    class="progress-ring"
    :class="`progress-ring--${tone}`"
    :style="{ '--ring-size': `${size}px` }"
  >
    <svg
      class="progress-ring__svg"
      :width="size"
      :height="size"
      :viewBox="`0 0 ${size} ${size}`"
      role="img"
      :aria-label="`${displayLabel}${caption ? ` ${caption}` : ''}`"
    >
      <circle
        class="progress-ring__track"
        :cx="center"
        :cy="center"
        :r="radius"
        :stroke-width="stroke"
      />
      <circle
        class="progress-ring__bar"
        :cx="center"
        :cy="center"
        :r="radius"
        :stroke-width="stroke"
        :stroke-dasharray="circumference"
        :stroke-dashoffset="dashOffset"
      />
    </svg>
    <span class="progress-ring__content">
      <strong>{{ displayLabel }}</strong>
      <small v-if="caption">{{ caption }}</small>
    </span>
  </div>
</template>
