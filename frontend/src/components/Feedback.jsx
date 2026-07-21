export function ErrorBanner({ message }) {
  if (!message) return null
  return <p className="feedback error" role="alert">{message}</p>
}

export function EmptyState({ title, description }) {
  return <div className="empty-state"><strong>{title}</strong><p>{description}</p></div>
}

export function Pagination({ page, onChange }) {
  if (!page || page.totalPages <= 1) return null
  return (
    <nav className="pagination" aria-label="페이지 이동">
      <button type="button" disabled={page.number === 0} onClick={() => onChange(page.number - 1)}>이전</button>
      <span>{page.number + 1} / {page.totalPages}</span>
      <button type="button" disabled={page.number + 1 >= page.totalPages} onClick={() => onChange(page.number + 1)}>다음</button>
    </nav>
  )
}
