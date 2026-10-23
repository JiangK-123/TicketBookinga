export function todayIso() {
  return formatDate(new Date())
}

export function addDaysIso(iso, days) {
  const [year, month, day] = iso.split('-').map(Number)
  const date = new Date(year, month - 1, day)
  date.setDate(date.getDate() + days)
  return formatDate(date)
}

export function money(value) {
  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return '--'
  }
  return amount.toFixed(2)
}

export function formatDateTime(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

export function heightLabel(cm) {
  return cm > 0 ? `身高 ${cm} cm 起` : '无身高限制'
}

function formatDate(date) {
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}
