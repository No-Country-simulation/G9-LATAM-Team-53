const API_URL = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '')
const TOKEN_KEY = 'financeai_token'
const USER_KEY = 'financeai_user'

export class ApiError extends Error {
  constructor(message, status, data) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.data = data
  }
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY))
  } catch {
    return null
  }
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearUser() {
  localStorage.removeItem(USER_KEY)
}

async function parseResponse(response) {
  const contentType = response.headers.get('content-type') || ''
  if (response.status === 204) return null
  if (contentType.includes('application/json')) return response.json()
  return response.text()
}

export async function apiRequest(path, options = {}) {
  const { token = getToken(), headers, body, ...requestOptions } = options
  const response = await fetch(`${API_URL}${path}`, {
    ...requestOptions,
    headers: {
      ...(body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body && typeof body !== 'string' ? JSON.stringify(body) : body,
  })

  const data = await parseResponse(response)
  if (!response.ok) {
    const message =
      data?.mensaje || data?.error || (typeof data === 'string' && data) || 'No se pudo completar la solicitud.'
    throw new ApiError(message, response.status, data)
  }

  return data
}

async function authenticate(path, data) {
  const response = await apiRequest(path, { method: 'POST', token: null, body: data })
  if (response?.token) setToken(response.token)
  if (response?.nombre) setUser(response)
  return response
}

export const authApi = {
  register: (data) => authenticate('/auth/register', data),
  login: (data) => authenticate('/auth/login', data),
  async logout() {
    try {
      return await apiRequest('/auth/logout', { method: 'POST' })
    } finally {
      clearToken()
      clearUser()
    }
  },
}

export const financeApi = {
  getCategories: () => apiRequest('/categorias'),
  createCategory: (data) => apiRequest('/categorias', { method: 'POST', body: data }),
  createTransaction: (data) => apiRequest('/transacciones', { method: 'POST', body: data }),
  updateTransactionCategory: (idTransaccion, categoriaId) =>
    apiRequest(`/transacciones/${idTransaccion}/categoria`, { method: 'PATCH', body: { categoriaId } }),
  getTransactions: (page = 0, size = 8) => apiRequest(`/transacciones?pagina=${page}&tamanio=${size}`),
  getAnalysis: () => apiRequest('/analisis-financiero'),
  getMonthlyIncomeStatus: () => apiRequest('/analisis-financiero/ingreso-mensual'),
  createMonthlyIncome: (data) => apiRequest('/analisis-financiero/ingreso-mensual', { method: 'POST', body: data }),
  updateMonthlyIncome: (data) =>
    apiRequest('/analisis-financiero/ingreso-mensual', { method: 'PATCH', body: data }),
}
