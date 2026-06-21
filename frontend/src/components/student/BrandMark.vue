<script setup>
defineProps({
  title: {
    type: String,
    default: '机试在线考试系统'
  },
  subtitle: {
    type: String,
    default: '专业 · 高效 · 智能'
  },
  compact: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <div class="brand-mark" :class="{ 'brand-mark--compact': compact }">
    <span class="brand-mark__symbol" aria-hidden="true">
      <span class="brand-mark__symbol-core">C</span>
    </span>
    <span class="brand-mark__copy">
      <strong>{{ title }}</strong>
      <small v-if="subtitle">{{ subtitle }}</small>
    </span>
  </div>
</template>
