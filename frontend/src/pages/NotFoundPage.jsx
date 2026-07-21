import { Link } from 'react-router'

export function NotFoundPage() {
  return <main className="center-screen"><p className="eyebrow">404</p><h1>페이지를 찾을 수 없습니다.</h1><Link className="primary-button" to="/">홈으로 이동</Link></main>
}
