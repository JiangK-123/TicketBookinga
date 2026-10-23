<template>
  <section class="page">
    <div class="hero">
      <p class="eyebrow">今晚的灯已经亮了</p>
      <h1>选一个项目，订今天或接下来 30 天的票。</h1>
      <p>价格按人计价。下单即占当日余票，未支付的订单可以取消并退回余票。</p>
    </div>

    <div class="filters">
      <button type="button" :class="{ active: category === '' }" @click="category = ''">全部</button>
      <button
        v-for="item in categories"
        :key="item.code"
        type="button"
        :class="{ active: category === item.code }"
        @click="category = item.code"
      >
        {{ item.label }}
      </button>
      <input v-model="keyword" type="search" placeholder="搜索项目名称" aria-label="搜索项目名称" />
    </div>

    <p v-if="error" class="alert" role="alert">{{ error }}</p>
    <p v-else-if="loading" class="muted">正在加载项目…</p>
    <p v-else-if="attractions.length === 0" class="muted">没有符合条件的项目。</p>
    <div v-else class="grid">
      <AttractionCard v-for="item in attractions" :key="item.id" :attraction="item" />
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { fetchAttractions, fetchCategories } from '../api/attractions'
import AttractionCard from '../components/AttractionCard.vue'

const categories = ref([])
const attractions = ref([])
const category = ref('')
const keyword = ref('')
const loading = ref(false)
const error = ref('')
let requestId = 0
let timer = 0

onMounted(async () => {
  try {
    categories.value = await fetchCategories()
  } catch (err) {
    error.value = err.message
  }
  await load()
})

watch(category, () => load())
watch(keyword, () => {
  clearTimeout(timer)
  timer = setTimeout(load, 300)
})

async function load() {
  const current = ++requestId
  loading.value = true
  error.value = ''
  try {
    const data = await fetchAttractions({
      category: category.value || undefined,
      keyword: keyword.value.trim() || undefined
    })
    if (current === requestId) {
      attractions.value = data
    }
  } catch (err) {
    if (current === requestId) {
      attractions.value = []
      error.value = err.message
    }
  } finally {
    if (current === requestId) {
      loading.value = false
    }
  }
}
</script>
