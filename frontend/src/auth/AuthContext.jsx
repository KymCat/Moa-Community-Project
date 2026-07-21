import { useEffect, useMemo, useState } from 'react'
import { api } from '../api/client'
import { AuthContext } from './context'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    const expireSession = () => setUser(null)
    window.addEventListener('auth:expired', expireSession)

    api.request('/users/me')
      .then((currentUser) => active && setUser(currentUser))
      .catch(() => {
        api.clearAccessToken()
        if (active) setUser(null)
      })
      .finally(() => active && setLoading(false))
    return () => {
      active = false
      window.removeEventListener('auth:expired', expireSession)
    }
  }, [])

  const value = useMemo(() => ({
    user,
    loading,
    isAdmin: user?.role === 'ADMIN',
    async login(credentials) {
      try {
        const token = await api.request('/auth/login', {
          method: 'POST', auth: false, body: JSON.stringify(credentials),
        })
        api.saveAccessToken(token)
        const currentUser = await api.request('/users/me')
        setUser(currentUser)
        return currentUser
      } catch (error) {
        api.clearAccessToken()
        throw error
      }
    },
    signup(payload) {
      return api.request('/users', { method: 'POST', auth: false, body: JSON.stringify(payload) })
    },
    async logout() {
      try { await api.request('/auth/logout', { method: 'POST' }) }
      catch { /* 로컬 세션은 항상 종료한다. */ }
      finally { api.clearAccessToken(); setUser(null) }
    },
    async refreshUser() {
      const currentUser = await api.request('/users/me')
      setUser(currentUser)
      return currentUser
    },
    clearSession() {
      api.clearAccessToken()
      setUser(null)
    },
  }), [loading, user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
