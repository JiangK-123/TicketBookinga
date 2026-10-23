<template>
  <div class="shell">
    <AppHeader />
    <main>
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import AppHeader from './components/AppHeader.vue'
</script>
