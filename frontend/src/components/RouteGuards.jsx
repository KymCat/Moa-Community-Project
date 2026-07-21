import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../auth/context'

export function ProtectedRoute() {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) return <div className="center-screen"><div className="spinner" /><p>로그인 정보를 확인하고 있습니다.</p></div>
  if (!user) return <Navigate to="/login" state={{ from: location }} replace />
  return <Outlet />
}

export function AdminRoute() {
  const { isAdmin } = useAuth()
  return isAdmin ? <Outlet /> : <Navigate to="/" replace />
}
