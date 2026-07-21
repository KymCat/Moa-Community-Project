import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { api } from '../api/client'
import { useAuth } from '../auth/context'
import { EmptyState, ErrorBanner, Pagination } from '../components/Feedback'
import { formatDate } from '../utils/date'

export function PostDetailPage() {
  const { postId } = useParams()
  const navigate = useNavigate()
  const { user, isAdmin } = useAuth()
  const [post, setPost] = useState(null)
  const [comments, setComments] = useState(null)
  const [commentPage, setCommentPage] = useState(0)
  const [comment, setComment] = useState('')
  const [liked, setLiked] = useState(false)
  const [editingPost, setEditingPost] = useState(false)
  const [postDraft, setPostDraft] = useState({ title: '', content: '' })
  const [editingCommentId, setEditingCommentId] = useState(null)
  const [commentDraft, setCommentDraft] = useState('')
  const [error, setError] = useState('')

  const loadPost = useCallback(async () => {
    const data = await api.request(`/posts/${postId}`)
    setPost(data)
    setPostDraft({ title: data.title, content: data.content })
  }, [postId])

  const loadComments = useCallback(async () => {
    setComments(await api.request(`/posts/${postId}/comments?page=${commentPage}`))
  }, [commentPage, postId])

  useEffect(() => {
    Promise.all([loadPost(), loadComments(), api.request(`/posts/${postId}/likes`).then(setLiked)])
      .catch((requestError) => setError(requestError.message))
  }, [loadComments, loadPost, postId])

  const canManagePost = post && (isAdmin || post.userId === user.id)

  const updatePost = async (event) => {
    event.preventDefault()
    try {
      await api.request(`/posts/${postId}`, { method: 'PATCH', body: JSON.stringify(postDraft) })
      await loadPost()
      setEditingPost(false)
    } catch (requestError) { setError(requestError.message) }
  }

  const deletePost = async () => {
    if (!window.confirm('게시글을 삭제할까요?')) return
    try { await api.request(`/posts/${postId}`, { method: 'DELETE' }); navigate('/') }
    catch (requestError) { setError(requestError.message) }
  }

  const addLike = async () => {
    try {
      await api.request(`/posts/${postId}/likes`, { method: 'POST' })
      setLiked(true)
      setPost((current) => ({ ...current, likeCount: current.likeCount + 1 }))
    } catch (requestError) { setError(requestError.message) }
  }

  const addComment = async (event) => {
    event.preventDefault()
    try {
      await api.request(`/posts/${postId}/comments`, { method: 'POST', body: JSON.stringify({ content: comment }) })
      setComment('')
      setCommentPage(0)
      await loadComments()
    } catch (requestError) { setError(requestError.message) }
  }

  const updateComment = async (id) => {
    try {
      await api.request(`/comments/${id}`, { method: 'PATCH', body: JSON.stringify({ content: commentDraft }) })
      setEditingCommentId(null)
      await loadComments()
    } catch (requestError) { setError(requestError.message) }
  }

  const deleteComment = async (id) => {
    if (!window.confirm('댓글을 삭제할까요?')) return
    try { await api.request(`/comments/${id}`, { method: 'DELETE' }); await loadComments() }
    catch (requestError) { setError(requestError.message) }
  }

  if (!post && !error) return <div className="loading-grid">게시글을 불러오는 중입니다.</div>

  return (
    <section className="detail-page">
      <Link className="back-link" to="/">← 커뮤니티로 돌아가기</Link>
      <ErrorBanner message={error} />
      {post && (
        <article className="article-card">
          {editingPost ? (
            <form className="composer" onSubmit={updatePost}>
              <input maxLength="100" value={postDraft.title} onChange={(event) => setPostDraft({ ...postDraft, title: event.target.value })} required />
              <textarea rows="12" maxLength="6000" value={postDraft.content} onChange={(event) => setPostDraft({ ...postDraft, content: event.target.value })} required />
              <div className="form-actions"><button className="secondary-button" type="button" onClick={() => setEditingPost(false)}>취소</button><button className="primary-button">수정 완료</button></div>
            </form>
          ) : (
            <>
              <div className="article-meta"><span className="avatar large">{post.name.slice(0, 1).toUpperCase()}</span><div><strong>{post.name}</strong><span>{formatDate(post.createdAt)}</span></div></div>
              <h1>{post.title}</h1><p className="article-content">{post.content}</p>
              <div className="article-actions">
                <button className={`like-button ${liked ? 'liked' : ''}`} type="button" disabled={liked} onClick={addLike}>♥ {post.likeCount} {liked ? '좋아요 완료' : '좋아요'}</button>
                {canManagePost && <div><button className="secondary-button" type="button" onClick={() => setEditingPost(true)}>수정</button><button className="danger-button" type="button" onClick={deletePost}>삭제</button></div>}
              </div>
            </>
          )}
        </article>
      )}

      <section className="comments-section">
        <div className="section-heading"><div><p className="eyebrow">JOIN THE TALK</p><h2>함께 나눈 이야기</h2></div><span>{comments?.page?.totalElements || 0}개</span></div>
        <form className="comment-form" onSubmit={addComment}><textarea rows="3" maxLength="6000" placeholder="따뜻한 댓글로 대화에 참여해보세요." value={comment} onChange={(event) => setComment(event.target.value)} required /><button className="primary-button">댓글 남기기</button></form>
        <div className="comment-list">
          {comments?.content?.map((item) => {
            const canManage = isAdmin || item.user_id === user.id
            return (
              <article className="comment-card" key={item.id}>
                <div className="comment-meta"><span className="avatar">{item.name.slice(0, 1).toUpperCase()}</span><div><strong>{item.name}</strong><span>{formatDate(item.createdAt)}</span></div></div>
                {editingCommentId === item.id ? (
                  <div className="inline-edit"><textarea value={commentDraft} onChange={(event) => setCommentDraft(event.target.value)} /><div className="form-actions"><button className="text-button" type="button" onClick={() => setEditingCommentId(null)}>취소</button><button className="secondary-button" type="button" onClick={() => updateComment(item.id)}>저장</button></div></div>
                ) : <p>{item.content}</p>}
                {canManage && editingCommentId !== item.id && <div className="comment-actions"><button className="text-button" type="button" onClick={() => { setEditingCommentId(item.id); setCommentDraft(item.content) }}>수정</button><button className="text-button danger-text" type="button" onClick={() => deleteComment(item.id)}>삭제</button></div>}
              </article>
            )
          })}
        </div>
        {comments?.content?.length === 0 && <EmptyState title="아직 댓글이 없습니다." description="첫 번째 대화를 시작해보세요." />}
        <Pagination page={comments?.page} onChange={setCommentPage} />
      </section>
    </section>
  )
}
