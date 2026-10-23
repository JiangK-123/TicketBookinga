<template>
  <form class="panel" @submit.prevent="submit">
    <h2>填写订单</h2>
    <p class="hint">单笔 1–10 张。可订今天起 30 天，取消未支付订单会退回余票。</p>

    <label>
      游玩日期
      <input v-model="visitDate" type="date" :min="minDate" :max="maxDate" @change="onDateChange" />
    </label>
    <p v-if="fieldErrors.visitDate" class="field-error">{{ fieldErrors.visitDate }}</p>
    <p v-if="availability" class="quota" :class="{ empty: availability.remaining === 0 }">
      {{ visitDate }} 剩余 {{ availability.remaining }} / {{ availability.dailyQuota }} 张
    </p>

    <label>
      数量
      <input v-model.number="quantity" type="number" min="1" max="10" step="1" />
    </label>
    <p v-if="fieldErrors.quantity" class="field-error">{{ fieldErrors.quantity }}</p>

    <label>
      游客姓名
      <input v-model="visitorName" type="text" maxlength="20" placeholder="不能包含数字，最多 20 字" />
    </label>
    <p v-if="fieldErrors.visitorName" class="field-error">{{ fieldErrors.visitorName }}</p>

    <label>
      手机号
      <input v-model="visitorPhone" type="tel" maxlength="11" inputmode="numeric" placeholder="用于查询订单" />
    </label>
    <p v-if="fieldErrors.visitorPhone" class="field-error">{{ fieldErrors.visitorPhone }}</p>
    <p v-if="fieldErrors.attractionId" class="field-error">{{ fieldErrors.attractionId }}</p>

    <p v-if="formError" class="alert" role="alert">{{ formError }}</p>

    <div class="submit-row">
      <button class="button" type="submit" :disabled="submitting">
        {{ submitting ? '提交中…' : `提交订单 · ¥${estimated}` }}
      </button>
    </div>
  </form>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchAvailability } from '../api/attractions'
import { createOrder } from '../api/orders'
import { addDaysIso, money, todayIso } from '../utils/format'

const props = defineProps({
  attraction: { type: Object, required: true }
})
const emit = defineEmits(['booked'])

const minDate = todayIso()
const maxDate = addDaysIso(minDate, 30)
const visitDate = ref(minDate)
const quantity = ref(1)
const visitorName = ref('')
const visitorPhone = ref('')
const availability = ref(null)
const fieldErrors = ref({})
const formError = ref('')
const submitting = ref(false)
let requestId = 0

const estimated = computed(() => {
  const count = Number(quantity.value)
  if (!Number.isInteger(count) || count < 1) {
    return '--'
  }
  return money(props.attraction.price * count)
})

onMounted(loadAvailability)

function onDateChange() {
  formError.value = ''
  fieldErrors.value = {}
  loadAvailability()
}

async function loadAvailability() {
  const current = ++requestId
  availability.value = null
  if (!visitDate.value) {
    return
  }
  try {
    const data = await fetchAvailability(props.attraction.id, visitDate.value)
    if (current === requestId) {
      availability.value = data
    }
  } catch (error) {
    if (current === requestId) {
      formError.value = error.message
    }
  }
}

async function submit() {
  fieldErrors.value = {}
  formError.value = ''
  submitting.value = true
  try {
    const order = await createOrder({
      attractionId: props.attraction.id,
      visitDate: visitDate.value,
      quantity: quantity.value,
      visitorName: visitorName.value,
      visitorPhone: visitorPhone.value
    })
    sessionStorage.setItem('park-phone', order.visitorPhone)
    emit('booked', order)
    await loadAvailability()
  } catch (error) {
    applyError(error)
    await loadAvailability()
  } finally {
    submitting.value = false
  }
}

function applyError(error) {
  const next = {}
  for (const item of error.details || []) {
    if (item?.field) {
      next[item.field] = item.message
    }
  }
  fieldErrors.value = next
  formError.value = error.message || '下单失败'
}
</script>
