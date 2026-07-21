import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { useAuth } from '../auth/context'
import { ErrorBanner } from '../components/Feedback'

export function SignupPage() {
  const { signup } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ id: '', password: '', name: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await signup(form)
      navigate('/login', { replace: true })
    } catch (requestError) {
      setError(requestError.message || '회원가입에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="auth-page signup">
      <section className="auth-intro">
        <Link className="brand light" to="/login"><span className="brand-mark">M</span>모여</Link>
        <p className="eyebrow">JOIN THE COMMUNITY</p>
        <h1>함께 이야기하면<br />일상이 더 즐거워져요.</h1>
        <p>간단한 가입으로 다양한 사람들과 관심사와 일상을 나눠보세요.</p>
      </section>
      <section className="auth-card">
        <div><p className="eyebrow">CREATE ACCOUNT</p><h2>모여에 가입하기</h2></div>
        <form onSubmit={handleSubmit}>
          <label>아이디<input minLength="6" maxLength="30" value={form.id} onChange={(event) => setForm({ ...form, id: event.target.value })} required /></label>
          <label>닉네임<input minLength="2" maxLength="30" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} required /></label>
          <label>비밀번호<input type="password" minLength="8" maxLength="15" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} required /></label>
          <ErrorBanner message={error} />
          <button className="primary-button wide" disabled={submitting}>{submitting ? '가입 중…' : '계정 만들기'}</button>
        </form>
        <p className="auth-link">이미 계정이 있나요? <Link to="/login">로그인</Link></p>
      </section>
    </main>
  )
}
