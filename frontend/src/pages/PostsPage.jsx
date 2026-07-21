import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router'
import { api } from '../api/client'
import { EmptyState, ErrorBanner, Pagination } from '../components/Feedback'
import { formatDate } from '../utils/date'

const EMPTY_POST = { title: '', content: '' }

export function PostsPage() {
  const [result, setResult] = useState(null)
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState({ type: 'TITLE', keyword: '', applied: '' })
  const [draft, setDraft] = useState(EMPTY_POST)
  const [composerOpen, setComposerOpen] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadPosts = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const path = search.applied
        ? `/posts/search?type=${search.type}&keyword=${encodeURIComponent(search.applied)}&page=${page}`
        : `/posts?page=${page}&size=6&sort=createdAt,desc`
      setResult(await api.request(path))
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setLoading(false)
    }
  }, [page, search.applied, search.type])

  useEffect(() => { loadPosts() }, [loadPosts])

  const submitSearch = (event) => {
    event.preventDefault()
    const keyword = search.keyword.trim()
    if (!keyword) return
    setPage(0)
    setSearch((current) => ({ ...current, applied: keyword }))
  }

  const clearSearch = () => {
    setPage(0)
    setSearch({ type: 'TITLE', keyword: '', applied: '' })
  }

  const createPost = async (event) => {
    event.preventDefault()
    setError('')
    try {
      await api.request('/posts', { method: 'POST', body: JSON.stringify(draft) })
      setDraft(EMPTY_POST)
      setComposerOpen(false)
      setPage(0)
      await loadPosts()
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  return (
    <section>
      <div className="hero-panel">
        <div><p className="eyebrow">OPEN COMMUNITY</p><h1>오늘, 무슨 이야기를 나눌까요?</h1><p>소소한 일상부터 취미, 고민, 궁금한 이야기까지 편하게 들려주세요.</p></div>
        <button className="primary-button" type="button" onClick={() => setComposerOpen((open) => !open)}>{composerOpen ? '작성 닫기' : '이야기 쓰기'}</button>
      </div>

      {composerOpen && (
        <form className="composer card" onSubmit={createPost}>
          <div className="section-heading"><div><p className="eyebrow">NEW STORY</p><h2>이야기 나누기</h2></div><span>{draft.title.length}/100</span></div>
          <input placeholder="어떤 이야기인가요?" maxLength="100" value={draft.title} onChange={(event) => setDraft({ ...draft, title: event.target.value })} required />
          <textarea placeholder="자유롭게 이야기를 들려주세요." maxLength="6000" rows="8" value={draft.content} onChange={(event) => setDraft({ ...draft, content: event.target.value })} required />
          <div className="form-actions"><button className="secondary-button" type="button" onClick={() => setComposerOpen(false)}>취소</button><button className="primary-button">공유하기</button></div>
        </form>
      )}

      <div className="content-toolbar">
        <div><p className="eyebrow">NOW TALKING</p><h2>{search.applied ? `“${search.applied}” 검색 결과` : '새로 올라온 이야기'}</h2></div>
        <form className="search-form" onSubmit={submitSearch}>
          <select value={search.type} onChange={(event) => setSearch({ ...search, type: event.target.value })}>
            <option value="TITLE">제목</option><option value="CONTENT">내용</option><option value="AUTHOR">작성자</option>
          </select>
          <input placeholder="검색어" value={search.keyword} onChange={(event) => setSearch({ ...search, keyword: event.target.value })} />
          <button className="secondary-button">검색</button>
          {search.applied && <button className="text-button" type="button" onClick={clearSearch}>초기화</button>}
        </form>
      </div>

      <ErrorBanner message={error} />
      {loading ? <div className="loading-grid">게시글을 불러오는 중입니다.</div> : (
        <div className="post-grid">
          {result?.content?.map((post) => (
            <Link className="post-card" to={`/posts/${post.id}`} key={post.id}>
              <div className="post-card-top"><span className="post-number">#{post.id}</span><span>{formatDate(post.createdAt)}</span></div>
              <h3>{post.title}</h3><p>{post.content}</p>
              <div className="post-card-footer"><span className="avatar">{post.name.slice(0, 1).toUpperCase()}</span><strong>{post.name}</strong><span className="arrow">→</span></div>
            </Link>
          ))}
        </div>
      )}
      {!loading && result?.content?.length === 0 && <EmptyState title="아직 이야기가 없습니다." description="가장 먼저 이야기를 시작해보세요." />}
      <Pagination page={result?.page} onChange={setPage} />
    </section>
  )
}
