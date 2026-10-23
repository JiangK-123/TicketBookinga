<template>
  <section class="page">
    <div class="hero slim">
      <p class="eyebrow">订单查询</p>
      <h1>用下单手机号找回订单。</h1>
      <p>待支付订单可以付款或取消。已支付和已取消的订单不能再次变更。</p>
    </div>

    <form class="search" @submit.prevent="search">
      <label>
        手机号
        <input v-model="phone" type="tel" maxlength="11" inputmode="numeric" placeholder="11 位手机号" />
      </label>
      <button class="button" type="submit" :disabled="loading">{{ loading ? '查询中…' : '查询' }}</button>
    </form>

    <p v-if="error" class="alert" role="alert">{{ error }}</p>
    <p v-else-if="notice" class="success inline" role="status">{{ notice }}</p>
    <p v-if="searched && !loading && !error && orders.length === 0" class="muted">这个手机号还没有订单。</p>
    <OrderList v-if="orders.length" :orders="orders" :busy-no="busyNo" @pay="pay" @cancel="cancel" />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { cancelOrder, fetchOrders, payOrder } from '../api/orders'
import OrderList from '../components/OrderList.vue'

const route = useRoute()
const phone = ref('')
const orders = ref([])
const loading = ref(false)
const error = ref('')
const notice = ref('')
const searched = ref(false)
const busyNo = ref('')

onMounted(() => {
  const fromQuery = typeof route.query.phone === 'string' ? route.query.phone : ''
  phone.value = fromQuery || sessionStorage.getItem('park-phone') || ''
  if (phone.value) {
    search()
  }
})

async function search(keepNotice = false) {
  error.value = ''
  if (!keepNotice) {
    notice.value = ''
  }
  const value = phone.value.trim()
  if (!value) {
    orders.value = []
    searched.value = false
    error.value = '请填写下单手机号'
    return
  }
  loading.value = true
  try {
    orders.value = await fetchOrders(value)
    sessionStorage.setItem('park-phone', value)
    searched.value = true
  } catch (err) {
    orders.value = []
    searched.value = true
    notice.value = ''
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function pay(order) {
  error.value = ''
  notice.value = ''
  busyNo.value = order.orderNo
  try {
    await payOrder(order.orderNo)
    notice.value = `${order.orderNo} 已支付`
    await search(true)
  } catch (err) {
    error.value = err.message
  } finally {
    busyNo.value = ''
  }
}

async function cancel(order) {
  if (!window.confirm(`取消订单 ${order.orderNo}？余票会退回当天。`)) {
    return
  }
  error.value = ''
  notice.value = ''
  busyNo.value = order.orderNo
  try {
    await cancelOrder(order.orderNo)
    notice.value = `${order.orderNo} 已取消，余票已退回`
    await search(true)
  } catch (err) {
    error.value = err.message
  } finally {
    busyNo.value = ''
  }
}
</script>
