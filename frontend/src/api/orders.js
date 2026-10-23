import http from './http'

export async function createOrder(payload) {
  const { data } = await http.post('/api/orders', payload)
  return data
}

export async function fetchOrders(phone) {
  const { data } = await http.get('/api/orders', { params: { phone } })
  return data
}

export async function payOrder(orderNo) {
  const { data } = await http.post(`/api/orders/${encodeURIComponent(orderNo)}/pay`)
  return data
}

export async function cancelOrder(orderNo) {
  const { data } = await http.post(`/api/orders/${encodeURIComponent(orderNo)}/cancel`)
  return data
}
