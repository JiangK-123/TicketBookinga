<template>
  <div class="orders">
    <article v-for="order in orders" :key="order.orderNo" class="order">
      <header>
        <div>
          <p class="eyebrow">{{ order.orderNo }}</p>
          <h2>{{ order.attractionName }}</h2>
        </div>
        <span class="badge" :data-status="order.status">{{ order.statusLabel }}</span>
      </header>
      <dl>
        <div><dt>游玩日期</dt><dd>{{ order.visitDate }}</dd></div>
        <div><dt>数量</dt><dd>{{ order.quantity }} 张</dd></div>
        <div><dt>金额</dt><dd>¥{{ money(order.totalAmount) }}</dd></div>
        <div><dt>游客</dt><dd>{{ order.visitorName }} · {{ order.visitorPhone }}</dd></div>
        <div><dt>下单时间</dt><dd>{{ formatDateTime(order.createdAt) }}</dd></div>
      </dl>
      <div v-if="order.status === 'UNPAID'" class="actions">
        <button class="button" type="button" :disabled="busyNo === order.orderNo" @click="emit('pay', order)">支付</button>
        <button class="button ghost" type="button" :disabled="busyNo === order.orderNo" @click="emit('cancel', order)">取消</button>
      </div>
    </article>
  </div>
</template>

<script setup>
import { formatDateTime, money } from '../utils/format'

defineProps({
  orders: { type: Array, required: true },
  busyNo: { type: String, default: '' }
})

const emit = defineEmits(['pay', 'cancel'])
</script>
