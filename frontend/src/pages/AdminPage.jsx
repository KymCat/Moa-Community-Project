import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client'
import { useAuth } from '../auth/context'
import { EmptyState, ErrorBanner, Pagination } from '../components/Feedback'

export function AdminPage() {
  const { user } = useAuth()
  const [users, setUsers] = useState([])
  const [totalUsers, setTotalUsers] = useState(0)
  const [page, setPage] = useState(0)
  const [pageInfo, setPageInfo] = useState(null)
  const [editingId, setEditingId] = useState(null)
  const [name, setName] = useState('')
  const [error, setError] = useState('')

  const loadUsers = useCallback(async () => {
    try {
      const response = await api.request(`/admin/users?page=${page}`)
      setUsers(response.content)
      setTotalUsers(response.page.totalElements)
      setPageInfo(response.page)
    }
    catch (requestError) { setError(requestError.message) }
  }, [page])

  useEffect(() => { loadUsers() }, [loadUsers])

  const updateName = async (id) => {
    try {
      await api.request(`/admin/users/${id}/name`, { method: 'PATCH', body: JSON.stringify({ name }) })
      setEditingId(null); await loadUsers()
    } catch (requestError) { setError(requestError.message) }
  }

  const deleteUser = async (id) => {
    if (!window.confirm(`${id} 계정을 삭제할까요?`)) return
    try { await api.request(`/admin/users/${id}`, { method: 'DELETE' }); await loadUsers() }
    catch (requestError) { setError(requestError.message) }
  }

  return (
    <section>
      <div className="hero-panel admin-hero"><div><p className="eyebrow">ADMIN CONSOLE</p><h1>사용자 관리</h1><p>일반 사용자의 닉네임을 수정하거나 계정을 삭제할 수 있습니다.</p></div><span className="metric"><strong>{totalUsers}</strong> USERS</span></div>
      <ErrorBanner message={error} />
      <div className="card table-card">
        <div className="user-table header"><span>사용자</span><span>닉네임</span><span>관리</span></div>
        {users.map((item) => (
          <div className="user-table" key={item.id}>
            <span><span className="avatar">{item.name.slice(0, 1).toUpperCase()}</span><strong>{item.id}</strong>{item.id === user.id && <em>내 계정</em>}</span>
            {editingId === item.id ? <input value={name} onChange={(event) => setName(event.target.value)} /> : <span>{item.name}</span>}
            <span className="table-actions">
              {editingId === item.id ? <><button className="text-button" onClick={() => setEditingId(null)}>취소</button><button className="secondary-button" onClick={() => updateName(item.id)}>저장</button></> : <button className="secondary-button" onClick={() => { setEditingId(item.id); setName(item.name) }}>닉네임 수정</button>}
              <button className="danger-button" type="button" onClick={() => deleteUser(item.id)}>삭제</button>
            </span>
          </div>
        ))}
        {users.length === 0 && <EmptyState title="사용자가 없습니다." description="회원가입한 사용자가 여기에 표시됩니다." />}
      </div>
      <Pagination page={pageInfo} onChange={setPage} />
    </section>
  )
}
