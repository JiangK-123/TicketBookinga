<template>
  <article class="ticket">
    <div class="ticket-side">
      <span class="currency">¥</span>
      <strong>{{ money(attraction.price) }}</strong>
      <span>每人</span>
    </div>
    <div class="ticket-body">
      <p class="eyebrow">{{ attraction.categoryLabel }}</p>
      <h2>{{ attraction.name }}</h2>
      <p class="desc">{{ attraction.description }}</p>
      <ul>
        <li>日限额 {{ attraction.dailyQuota }} 张</li>
        <li>{{ attraction.durationMinutes }} 分钟</li>
        <li>{{ heightLabel(attraction.minHeightCm) }}</li>
        <li>{{ attraction.openTime }}–{{ attraction.closeTime }}</li>
      </ul>
      <RouterLink class="button" :to="{ name: 'attraction', params: { id: attraction.id } }">预订</RouterLink>
    </div>
  </article>
</template>

<script setup>
import { heightLabel, money } from '../utils/format'

defineProps({
  attraction: { type: Object, required: true }
})
</script>
