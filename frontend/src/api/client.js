const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const ACCESS_TOKEN_KEY = 'accessToken'

export class ApiError extends Error {
  constructor(status, code, message) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

function saveAccessToken(token) {
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

function clearAccessToken() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}

async function parseBody(response) {
  if (response.status === 204) return null
  const contentType = response.headers.get('content-type') || ''
  if (!contentType.includes('application/json')) {
    const message = await response.text()
    if (!response.ok) throw new ApiError(response.status, null, message || '요청을 처리하지 못했습니다.')
    return message
  }

  const body = await response.json()
  if (!response.ok || body.success === false) {
    throw new ApiError(response.status, body.code, body.message || '요청을 처리하지 못했습니다.')
  }
  return body.data
}

async function reissue() {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/reissue`, {
      method: 'POST',
      credentials: 'include',
    })
    if (!response.ok) return false
    const token = await parseBody(response)
    if (!token) return false
    saveAccessToken(token)
    return true
  } catch {
    return false
  }
}

async function request(path, options = {}, retry = true) {
  const { auth = true, body, headers: customHeaders, ...fetchOptions } = options
  const headers = new Headers(customHeaders || {})

  if (body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const token = getAccessToken()
  if (auth && token) headers.set('Authorization', `Bearer ${token}`)

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...fetchOptions,
    body,
    headers,
    credentials: 'include',
  })

  if (response.status === 401 && auth && retry) {
    const renewed = await reissue()
    if (renewed) return request(path, options, false)
    clearAccessToken()
    window.dispatchEvent(new Event('auth:expired'))
  }

  return parseBody(response)
}

export const api = { request, reissue, getAccessToken, saveAccessToken, clearAccessToken }
