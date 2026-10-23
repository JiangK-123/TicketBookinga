<template>
  <section class="page">
    <RouterLink class="back" to="/">返回项目列表</RouterLink>
    <p v-if="loading" class="muted">正在加载项目…</p>
    <p v-else-if="error" class="alert" role="alert">{{ error }}</p>
    <template v-else-if="attraction">
      <div class="detail">
        <article class="detail-card">
          <p class="eyebrow">{{ attraction.categoryLabel }}</p>
          <h1>{{ attraction.name }}</h1>
          <p class="lead">{{ attraction.description }}</p>
          <ul class="facts">
            <li><span>票价</span><strong>¥{{ money(attraction.price) }} / 人</strong></li>
            <li><span>日限额</span><strong>{{ attraction.dailyQuota }} 张</strong></li>
            <li><span>时长</span><strong>{{ attraction.durationMinutes }} 分钟</strong></li>
            <li><span>身高</span><strong>{{ attraction.minHeightCm > 0 ? `${attraction.minHeightCm} cm 起` : '无限制' }}</strong></li>
            <li><span>开放</span><strong>{{ attraction.openTime }}–{{ attraction.closeTime }}</strong></li>
          </ul>
        </article>
        <BookingForm :key="attraction.id" :attraction="attraction" @booked="created = $event" />
      </div>
      <div v-if="created" class="success" role="status">
        <h2>订单已提交，等待支付</h2>
        <p>
          订单号 {{ created.orderNo }}，{{ created.attractionName }}，{{ created.visitDate }}，
          {{ created.quantity }} 张，合计 ¥{{ money(created.totalAmount) }}。
        </p>
        <RouterLink class="button" :to="{ name: 'orders', query: { phone: created.visitorPhone } }">去支付或查看订单</RouterLink>
      </div>
    </template>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { fetchAttraction } from '../api/attractions'
import BookingForm from '../components/BookingForm.vue'
import { money } from '../utils/format'

const props = defineProps({
  id: { type: String, required: true }
})

const attraction = ref(null)
const created = ref(null)
const loading = ref(false)
const error = ref('')

onMounted(load)
watch(() => props.id, load)

async function load() {
  loading.value = true
  error.value = ''
  created.value = null
  attraction.value = null
  try {
    attraction.value = await fetchAttraction(props.id)
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>
