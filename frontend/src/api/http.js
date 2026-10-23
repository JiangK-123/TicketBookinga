import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 8000,
  headers: { Accept: 'application/json' }
})

http.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(normalizeError(error))
)

function normalizeError(error) {
  const data = error.response?.data
  if (data && typeof data === 'object' && data.message) {
    return {
      status: error.response.status,
      code: data.code || 'ERROR',
      message: data.message,
      details: Array.isArray(data.details) ? data.details : []
    }
  }
  if (error.code === 'ECONNABORTED') {
    return { status: 0, code: 'TIMEOUT', message: '请求超时，请稍后重试', details: [] }
  }
  return {
    status: 0,
    code: 'NETWORK_ERROR',
    message: `无法连接订票服务，请确认后端已启动（${http.defaults.baseURL}）`,
    details: []
  }
}

export default http
