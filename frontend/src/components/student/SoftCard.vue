<script setup>
defineProps({
  as: {
    type: String,
    default: 'section'
  },
  padding: {
    type: String,
    default: 'md',
    validator: (value) => ['none', 'sm', 'md', 'lg'].includes(value)
  },
  interactive: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <component
    :is="as"
    class="soft-card"
    :class="[
      `soft-card--pad-${padding}`,
      { 'soft-card--interactive': interactive }
    ]"
  >
    <slot />
  </component>
</template>
