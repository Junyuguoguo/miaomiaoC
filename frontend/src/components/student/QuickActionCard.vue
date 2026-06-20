<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  tone: {
    type: String,
    default: 'blue',
    validator: (value) => ['blue', 'green', 'purple', 'orange', 'cyan'].includes(value)
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select'])
</script>

<template>
  <button
    class="quick-action-card"
    :class="[`quick-action-card--${tone}`, { 'quick-action-card--disabled': disabled }]"
    type="button"
    :disabled="disabled"
    @click="emit('select')"
  >
    <span class="quick-action-card__icon" aria-hidden="true">
      <slot name="icon" />
    </span>
    <span class="quick-action-card__copy">
      <strong>{{ title }}</strong>
      <small v-if="description">{{ description }}</small>
    </span>
    <span class="quick-action-card__arrow" aria-hidden="true">›</span>
  </button>
</template>
