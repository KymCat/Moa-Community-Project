import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { useAuth } from '../auth/context'
import { ErrorBanner } from '../components/Feedback'

export function LoginPage() {
  const { user, login } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [form, setForm] = useState({ id: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to="/" replace />

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(form)
      navigate(location.state?.from?.pathname || '/', { replace: true })
    } catch (requestError) {
      setError(requestError.message || '로그인에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-intro">
        <Link className="brand light" to="/login"><span className="brand-mark">M</span>모여</Link>
        <p className="eyebrow">EVERYONE'S COMMUNITY</p>
        <h1>우리의 이야기가<br />모이는 곳.</h1>
        <p>오늘 있었던 일, 좋아하는 것, 궁금한 이야기를 이웃들과 편하게 나눠보세요.</p>
      </section>
      <section className="auth-card">
        <div><p className="eyebrow">WELCOME BACK</p><h2>다시 만나 반가워요</h2><p className="muted">로그인하고 새로운 이야기를 만나보세요.</p></div>
        <form onSubmit={handleSubmit}>
          <label>아이디<input value={form.id} onChange={(event) => setForm({ ...form, id: event.target.value })} required /></label>
          <label>비밀번호<input type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} required /></label>
          <ErrorBanner message={error} />
          <button className="primary-button wide" disabled={submitting}>{submitting ? '로그인 중…' : '로그인'}</button>
        </form>
        <p className="auth-link">아직 계정이 없나요? <Link to="/signup">회원가입</Link></p>
      </section>
    </main>
  )
}
