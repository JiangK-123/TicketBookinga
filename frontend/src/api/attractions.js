import http from './http'

export async function fetchCategories() {
  const { data } = await http.get('/api/attractions/categories')
  return data
}

export async function fetchAttractions(params) {
  const { data } = await http.get('/api/attractions', { params })
  return data
}

export async function fetchAttraction(id) {
  const { data } = await http.get(`/api/attractions/${id}`)
  return data
}

export async function fetchAvailability(id, date) {
  const { data } = await http.get(`/api/attractions/${id}/availability`, { params: { date } })
  return data
}
