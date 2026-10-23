<template>
  <header class="topbar">
    <RouterLink class="brand" to="/">
      <span class="mark" aria-hidden="true">
        <svg viewBox="0 0 24 24" width="22" height="22">
          <path fill="currentColor" d="M12 2.4 14.8 8l6.1.9-4.4 4.3 1 6.1L12 16.7 6.5 19.3l1-6.1L3.1 8.9 9.2 8z" />
        </svg>
      </span>
      <span>
        <strong>星河游乐场</strong>
        <small>当日余票 · 在线订票</small>
      </span>
    </RouterLink>
    <nav>
      <RouterLink to="/">项目</RouterLink>
      <RouterLink to="/orders">我的订单</RouterLink>
      <a :href="docsUrl" target="_blank" rel="noreferrer">接口文档</a>
    </nav>
  </header>
</template>

<script setup>
const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const docsUrl = `${apiBase}/swagger-ui.html`
</script>
