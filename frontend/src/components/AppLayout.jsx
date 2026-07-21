import { NavLink, Outlet, useNavigate } from 'react-router'
import { useAuth } from '../auth/context'

export function AppLayout() {
  const { user, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-shell">
      <header className="site-header">
        <NavLink className="brand" to="/" aria-label="모여 홈">
          <span className="brand-mark">M</span><span>모여</span>
        </NavLink>
        <nav className="main-nav" aria-label="주 메뉴">
          <NavLink to="/" end>커뮤니티</NavLink>
          <NavLink to="/me">내 활동</NavLink>
          {isAdmin && <NavLink to="/admin">관리자</NavLink>}
        </nav>
        <div className="header-account">
          <div><strong>{user.name}</strong><span className={`role-badge ${isAdmin ? 'admin' : ''}`}>{user.role}</span></div>
          <button className="text-button" type="button" onClick={handleLogout}>로그아웃</button>
        </div>
      </header>
      <main className="page-container"><Outlet /></main>
    </div>
  )
}
