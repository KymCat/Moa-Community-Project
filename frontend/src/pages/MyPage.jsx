import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { api } from '../api/client'
import { useAuth } from '../auth/context'
import { EmptyState, ErrorBanner, Pagination } from '../components/Feedback'
import { formatDate } from '../utils/date'

export function MyPage() {
  const { user, refreshUser, clearSession, isAdmin } = useAuth()
  const navigate = useNavigate()
  const [name, setName] = useState(user.name)
  const [passwords, setPasswords] = useState({ currentPassword: '', newPassword: '' })
  const [posts, setPosts] = useState(null)
  const [comments, setComments] = useState(null)
  const [postPage, setPostPage] = useState(0)
  const [commentPage, setCommentPage] = useState(0)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const loadActivity = useCallback(async () => {
    try {
      const [postResult, commentResult] = await Promise.all([
        api.request(`/posts/me?page=${postPage}`),
        api.request(`/comments/me?page=${commentPage}`),
      ])
      setPosts(postResult)
      setComments(commentResult)
    } catch (requestError) { setError(requestError.message) }
  }, [commentPage, postPage])

  useEffect(() => { loadActivity() }, [loadActivity])

  const updateName = async (event) => {
    event.preventDefault(); setError(''); setNotice('')
    try {
      await api.request('/users/me/name', { method: 'PATCH', body: JSON.stringify({ name }) })
      await refreshUser(); setNotice('닉네임을 변경했습니다.')
    } catch (requestError) { setError(requestError.message) }
  }

  const updatePassword = async (event) => {
    event.preventDefault(); setError(''); setNotice('')
    try {
      await api.request('/users/me/password', { method: 'PATCH', body: JSON.stringify(passwords) })
      setPasswords({ currentPassword: '', newPassword: '' }); setNotice('비밀번호를 변경했습니다.')
    } catch (requestError) { setError(requestError.message) }
  }

  const deleteAccount = async () => {
    if (!window.confirm('계정을 탈퇴하면 되돌릴 수 없습니다. 계속할까요?')) return
    try {
      await api.request('/users/me', { method: 'DELETE' })
      clearSession(); navigate('/signup', { replace: true })
    } catch (requestError) { setError(requestError.message) }
  }

  return (
    <section>
      <div className="profile-hero"><span className="avatar profile-avatar">{user.name.slice(0, 1).toUpperCase()}</span><div><p className="eyebrow">MY PROFILE</p><h1>{user.name}</h1><p>@{user.id} · {user.role}</p></div></div>
      <ErrorBanner message={error} />
      {notice && <p className="feedback success">{notice}</p>}

      <div className="settings-grid">
        <form className="card settings-card" onSubmit={updateName}><p className="eyebrow">PROFILE</p><h2>닉네임 변경</h2><label>새 닉네임<input minLength="2" maxLength="30" value={name} onChange={(event) => setName(event.target.value)} required /></label><button className="secondary-button">저장</button></form>
        <form className="card settings-card" onSubmit={updatePassword}><p className="eyebrow">SECURITY</p><h2>비밀번호 변경</h2><label>현재 비밀번호<input type="password" minLength="8" maxLength="15" value={passwords.currentPassword} onChange={(event) => setPasswords({ ...passwords, currentPassword: event.target.value })} required /></label><label>새 비밀번호<input type="password" minLength="8" maxLength="15" value={passwords.newPassword} onChange={(event) => setPasswords({ ...passwords, newPassword: event.target.value })} required /></label><button className="secondary-button">변경</button></form>
      </div>

      <div className="activity-grid">
        <section className="card activity-panel"><div className="section-heading"><div><p className="eyebrow">POSTS</p><h2>내가 쓴 이야기</h2></div><span>{posts?.page?.totalElements || 0}개</span></div>{posts?.content?.map((post) => <Link className="activity-item" to={`/posts/${post.id}`} key={post.id}><div><strong>{post.title}</strong><p>{post.content}</p></div><time>{formatDate(post.createdAt)}</time></Link>)}{posts?.content?.length === 0 && <EmptyState title="작성한 이야기가 없습니다." description="커뮤니티에 첫 이야기를 들려주세요." />}<Pagination page={posts?.page} onChange={setPostPage} /></section>
        <section className="card activity-panel"><div className="section-heading"><div><p className="eyebrow">COMMENTS</p><h2>내 댓글</h2></div><span>{comments?.page?.totalElements || 0}개</span></div>{comments?.content?.map((item) => <Link className="activity-item" to={`/posts/${item.post_id}`} key={item.id}><div><strong>게시글 #{item.post_id}</strong><p>{item.content}</p></div><time>{formatDate(item.createdAt)}</time></Link>)}{comments?.content?.length === 0 && <EmptyState title="작성한 댓글이 없습니다." description="게시글에 의견을 남겨보세요." />}<Pagination page={comments?.page} onChange={setCommentPage} /></section>
      </div>

      <section className="danger-zone"><div><h2>계정 탈퇴</h2><p>{isAdmin ? '관리자 계정은 API에서 탈퇴할 수 없습니다.' : '탈퇴한 계정은 복구할 수 없습니다.'}</p></div><button className="danger-button" type="button" disabled={isAdmin} onClick={deleteAccount}>계정 탈퇴</button></section>
    </section>
  )
}
