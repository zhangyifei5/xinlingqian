<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, userApi, adminCustomerServiceApi } from '../api'

const router = useRouter()
const currentUser = ref(null)
const activeTab = ref('users')

const menuItems = [
  { id: 'users', icon: '👥', label: '用户管理' },
  { id: 'counselors', icon: '🧘', label: '咨询师管理' },
  { id: 'posts', icon: '📝', label: '社区管理' },
  { id: 'levelUp', icon: '⬆️', label: '升级审核' },
  { id: 'reviews', icon: '⭐', label: '评论管理' },
  { id: 'messages', icon: '💬', label: '客服消息' },
]

const users = ref([])
const searchKeyword = ref('')
const selectedUser = ref(null)
const showBalanceModal = ref(false)
const balanceAmount = ref(0)
const balanceOperation = ref('add')

const counselors = ref([])
const counselorApplications = ref([])

const posts = ref([])

const levelUpRequests = ref([])

const reviews = ref([])

const customerServiceConversations = ref([])
const selectedConversation = ref(null)
const customerMessages = ref([])
const replyContent = ref('')
const isSending = ref(false)

const loadUsers = async () => {
  try {
    const response = await adminApi.searchUsers(searchKeyword.value)
    if (response.data.code === 200) {
      users.value = response.data.data
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

const searchUsers = () => {
  loadUsers()
}

const openBalanceModal = (user) => {
  selectedUser.value = user
  showBalanceModal.value = true
  balanceAmount.value = 0
  balanceOperation.value = 'add'
}

const updateBalance = async () => {
  if (!selectedUser.value || balanceAmount.value === 0) {
    ElMessage.warning('请输入金额')
    return
  }
  try {
    const amount = balanceOperation.value === 'add' ? balanceAmount.value : -balanceAmount.value
    const response = await adminApi.updateBalance(selectedUser.value.id, amount)
    if (response.data.code === 200) {
      ElMessage.success('操作成功')
      showBalanceModal.value = false
      loadUsers()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const toggleMember = async (user) => {
  try {
    const response = await adminApi.setMember(user.id, !user.isMember)
    if (response.data.code === 200) {
      user.isMember = !user.isMember
      ElMessage.success(user.isMember ? '已设置为会员' : '已取消会员')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要注销用户 "${user.username}" 吗？`, '提示', {
      type: 'warning'
    })
    const response = await adminApi.deleteUser(user.id)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const loadCounselorApplications = async () => {
  try {
    const response = await adminApi.getPendingApplications()
    if (response.data.code === 200) {
      counselorApplications.value = response.data.data
    }
  } catch (error) {
    console.error('获取申请列表失败:', error)
  }
}

const approveCounselor = async (counselor) => {
  try {
    const response = await adminApi.approveCounselor(counselor.id)
    if (response.data.code === 200) {
      ElMessage.success('审批通过')
      loadCounselorApplications()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const rejectCounselor = async (counselor) => {
  try {
    const response = await adminApi.rejectCounselor(counselor.id)
    if (response.data.code === 200) {
      ElMessage.success('已拒绝')
      loadCounselorApplications()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const loadApprovedCounselors = async () => {
  try {
    const response = await adminApi.getApprovedCounselors()
    if (response.data.code === 200) {
      counselors.value = response.data.data
    }
  } catch (error) {
    console.error('获取咨询师列表失败:', error)
  }
}

const dismissCounselor = async (counselor) => {
  try {
    await ElMessageBox.confirm(`确定要卸任咨询师 "${counselor.name}" 吗？`, '提示', {
      type: 'warning'
    })
    const response = await adminApi.deleteCounselor(counselor.id)
    if (response.data.code === 200) {
      ElMessage.success('卸任成功')
      loadApprovedCounselors()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '卸任失败')
    }
  }
}

const loadPosts = async () => {
  try {
    const response = await adminApi.getAllPosts()
    if (response.data.code === 200) {
      posts.value = response.data.data
    }
  } catch (error) {
    console.error('获取帖子失败:', error)
  }
}

const deletePost = async (post) => {
  try {
    await ElMessageBox.confirm('确定要删除这条帖子吗？', '提示', {
      type: 'warning'
    })
    const response = await adminApi.deletePost(post.id)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadPosts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const loadLevelUpRequests = async () => {
  try {
    const response = await adminApi.getPendingLevelUpRequests()
    if (response.data.code === 200) {
      levelUpRequests.value = response.data.data
    }
  } catch (error) {
    console.error('获取升级申请失败:', error)
  }
}

const approveLevelUp = async (request) => {
  try {
    const response = await adminApi.approveLevelUp(request.id)
    if (response.data.code === 200) {
      ElMessage.success('批准升级')
      loadLevelUpRequests()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const rejectLevelUp = async (request) => {
  try {
    const response = await adminApi.rejectLevelUp(request.id)
    if (response.data.code === 200) {
      ElMessage.success('已拒绝')
      loadLevelUpRequests()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const loadReviews = async () => {
  try {
    const response = await adminApi.getAllReviews()
    if (response.data.code === 200) {
      reviews.value = response.data.data
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const deleteReview = async (review) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      type: 'warning'
    })
    const response = await adminApi.deleteReview(review.id)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadReviews()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const loadMessages = async () => {
  try {
    const response = await adminCustomerServiceApi.getConversations()
    if (response.data.code === 200) {
      customerServiceConversations.value = response.data.data
    }
  } catch (error) {
    console.error('获取客服会话失败:', error)
  }
}

const selectConversation = async (conversation) => {
  selectedConversation.value = conversation
  replyContent.value = ''
  try {
    const response = await adminCustomerServiceApi.getHistory(conversation.userId)
    if (response.data.code === 200) {
      customerMessages.value = response.data.data
      selectedConversation.value.unreadCount = 0
    }
  } catch (error) {
    console.error('获取消息历史失败:', error)
  }
}

const sendReply = async () => {
  if (!selectedConversation.value || !replyContent.value.trim() || isSending.value) {
    ElMessage.warning('请输入回复内容')
    return
  }
  isSending.value = true
  try {
    const response = await adminCustomerServiceApi.reply(selectedConversation.value.userId, replyContent.value)
    if (response.data.code === 200) {
      customerMessages.value.push({
        id: response.data.data.id,
        content: response.data.data.content,
        senderType: 'ADMIN',
        createTime: response.data.data.createTime
      })
      replyContent.value = ''
      ElMessage.success('回复成功')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '回复失败')
  } finally {
    isSending.value = false
  }
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const handleTabChange = (tab) => {
  activeTab.value = tab
  switch(tab) {
    case 'users':
      loadUsers()
      break
    case 'counselors':
      loadCounselorApplications()
      loadApprovedCounselors()
      break
    case 'posts':
      loadPosts()
      break
    case 'levelUp':
      loadLevelUpRequests()
      break
    case 'reviews':
      loadReviews()
      break
    case 'messages':
      loadMessages()
      break
  }
}

const goHome = () => {
  router.push('/')
}

const logout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    localStorage.clear()
    window.location.href = '/login'
  } catch (e) {
    ElMessage.info('已取消退出')
  }
}

onMounted(async () => {
  handleTabChange('users')
})
</script>

<template>
  <div class="admin-page">
    <div class="admin-header">
      <div class="header-left">
        <button class="home-btn" @click="goHome">🏠 返回首页</button>
      </div>
      <div class="header-center">
        <h1>🔧 管理员后台</h1>
      </div>
      <div class="header-right">
        <button class="logout-btn" @click="logout">退出登录</button>
      </div>
    </div>

    <div class="admin-content">
      <div class="sidebar">
        <div 
          v-for="item in menuItems" 
          :key="item.id"
          :class="['sidebar-item', { active: activeTab === item.id }]"
          @click="handleTabChange(item.id)"
        >
          <span class="item-icon">{{ item.icon }}</span>
          <span class="item-label">{{ item.label }}</span>
        </div>
      </div>

      <div class="main-content">
        <div v-if="activeTab === 'users'" class="content-section">
          <div class="section-header">
            <h2>👥 用户管理</h2>
            <div class="search-box">
              <input 
                v-model="searchKeyword" 
                type="text" 
                placeholder="搜索用户名..."
                @keyup.enter="searchUsers"
              />
              <button class="search-btn" @click="searchUsers">🔍</button>
            </div>
          </div>
          <div class="users-table">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>昵称</th>
                  <th>手机号</th>
                  <th>余额</th>
                  <th>角色</th>
                  <th>会员状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id">
                  <td>{{ user.id }}</td>
                  <td>{{ user.username }}</td>
                  <td>{{ user.nickname || '-' }}</td>
                  <td>{{ user.phone || '-' }}</td>
                  <td>¥{{ user.balance?.toFixed(2) || '0.00' }}</td>
                  <td>{{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}</td>
                  <td :class="['member-status', { member: user.isMember }]">
                    {{ user.isMember ? '👑 会员' : '普通用户' }}
                  </td>
                  <td>
                    <button class="action-btn add-btn" @click="openBalanceModal(user)">加钱</button>
                    <button class="action-btn minus-btn" @click="() => { openBalanceModal(user); balanceOperation = 'minus'; }">减钱</button>
                    <button 
                      :class="['action-btn', user.isMember ? 'reject-btn' : 'approve-btn']" 
                      @click="toggleMember(user)"
                    >
                      {{ user.isMember ? '取消会员' : '设为会员' }}
                    </button>
                    <button class="action-btn delete-btn" @click="deleteUser(user)">注销</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-if="activeTab === 'counselors'" class="content-section">
          <div class="section-header">
            <h2>🧘 咨询师申请审核</h2>
          </div>
          <div class="applications-list">
            <div v-for="counselor in counselorApplications" :key="counselor.id" class="application-card">
              <div class="app-header">
                <span class="app-name">{{ counselor.name }}</span>
                <span class="app-title">{{ counselor.title }}</span>
              </div>
              <p class="app-specialty">🏷️ {{ counselor.specialty }}</p>
              <p class="app-desc">{{ counselor.description }}</p>
              <div class="app-actions">
                <button class="action-btn approve-btn" @click="approveCounselor(counselor)">通过</button>
                <button class="action-btn reject-btn" @click="rejectCounselor(counselor)">拒绝</button>
              </div>
            </div>
            <div v-if="counselorApplications.length === 0" class="empty-state">
              <p>暂无待审核的申请</p>
            </div>
          </div>

          <div class="section-header" style="margin-top: 30px;">
            <h2>👩⚕️ 已通过咨询师管理</h2>
          </div>
          <div class="applications-list">
            <div v-for="counselor in counselors" :key="counselor.id" class="application-card">
              <div class="app-header">
                <span class="app-name">{{ counselor.name }}</span>
                <span class="app-title">Lv.{{ counselor.level }} {{ counselor.title }}</span>
              </div>
              <p class="app-specialty">🏷️ {{ counselor.specialty }} | 用户ID: {{ counselor.userId }}</p>
              <p class="app-desc">{{ counselor.description }}</p>
              <div class="app-actions">
                <button class="action-btn delete-btn" @click="dismissCounselor(counselor)">卸任咨询师</button>
              </div>
            </div>
            <div v-if="counselors.length === 0" class="empty-state">
              <p>暂无已通过的咨询师</p>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'posts'" class="content-section">
          <div class="section-header">
            <h2>📝 社区帖子管理</h2>
          </div>
          <div class="posts-list">
            <div v-for="post in posts" :key="post.id" class="post-card">
              <div class="post-header">
                <div class="post-author-info">
                  <span class="post-author">{{ post.nickname }}</span>
                  <span class="post-user-id">用户ID: {{ post.userId }}</span>
                </div>
                <span class="post-time">{{ post.createTime }}</span>
              </div>
              <div class="post-content-wrapper">
                <span class="post-content-label">帖子内容:</span>
                <p class="post-content">{{ post.content }}</p>
              </div>
              <div class="post-footer">
                <span class="post-stats">❤️ {{ post.likes }} 💬 {{ post.commentsCount }} | 帖子ID: {{ post.id }}</span>
                <button class="action-btn delete-btn" @click="deletePost(post)">删除</button>
              </div>
            </div>
            <div v-if="posts.length === 0" class="empty-state">
              <p>暂无社区帖子</p>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'levelUp'" class="content-section">
          <div class="section-header">
            <h2>⬆️ 咨询师升级审核</h2>
          </div>
          <div class="levelup-list">
            <div v-for="request in levelUpRequests" :key="request.id" class="levelup-card">
              <div class="levelup-header">
                <span class="levelup-counselor">咨询师: {{ request.counselorName }}</span>
                <span class="levelup-target">目标等级: {{ request.targetLevel }}级</span>
              </div>
              <p class="levelup-reason">申请理由: {{ request.reason }}</p>
              <div class="levelup-actions">
                <button class="action-btn approve-btn" @click="approveLevelUp(request)">批准升级</button>
                <button class="action-btn reject-btn" @click="rejectLevelUp(request)">拒绝</button>
              </div>
            </div>
            <div v-if="levelUpRequests.length === 0" class="empty-state">
              <p>暂无升级申请</p>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'reviews'" class="content-section">
          <div class="section-header">
            <h2>⭐ 评论管理</h2>
          </div>
          <div class="reviews-list">
            <div v-for="review in reviews" :key="review.id" class="review-card">
              <div class="review-header">
                <span class="review-counselor">咨询师ID: {{ review.counselorId }}</span>
                <span class="review-rating">
                  <span v-for="i in 5" :key="i">{{ i <= review.rating ? '★' : '☆' }}</span>
                </span>
              </div>
              <p class="review-comment">{{ review.comment || '暂无评论内容' }}</p>
              <div class="review-footer">
                <span class="review-time">{{ review.createTime }}</span>
                <button class="action-btn delete-btn" @click="deleteReview(review)">删除</button>
              </div>
            </div>
            <div v-if="reviews.length === 0" class="empty-state">
              <p>暂无评论</p>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'messages'" class="content-section customer-service-section">
          <div class="section-header">
            <h2>💬 客服消息</h2>
          </div>
          <div class="customer-service-container">
            <div class="conversation-list">
              <div 
                v-for="conv in customerServiceConversations" 
                :key="conv.userId"
                :class="['conversation-item', { active: selectedConversation?.userId === conv.userId }]"
                @click="selectConversation(conv)"
              >
                <div class="conv-avatar">{{ conv.nickname?.charAt(0) || '?' }}</div>
                <div class="conv-info">
                  <div class="conv-header">
                    <span class="conv-name">{{ conv.nickname }}</span>
                    <span v-if="conv.unreadCount > 0" class="unread-badge">{{ conv.unreadCount }}</span>
                  </div>
                  <div class="conv-preview">{{ conv.lastMessage || '暂无消息' }}</div>
                  <div class="conv-time">{{ formatDate(conv.lastMessageTime) }}</div>
                </div>
              </div>
              <div v-if="customerServiceConversations.length === 0" class="empty-state">
                <p>暂无用户咨询</p>
              </div>
            </div>
            
            <div class="chat-panel">
              <div v-if="selectedConversation" class="chat-content">
                <div class="chat-header">
                  <div class="chat-user-info">
                    <div class="chat-avatar">{{ selectedConversation.nickname?.charAt(0) || '?' }}</div>
                    <div class="chat-user-detail">
                      <span class="chat-user-name">{{ selectedConversation.nickname }}</span>
                      <span class="chat-user-id">用户ID: {{ selectedConversation.userId }}</span>
                    </div>
                  </div>
                </div>
                
                <div class="chat-messages">
                  <div class="system-message-hint">您正在与用户交流</div>
                  <div 
                    v-for="msg in customerMessages" 
                    :key="msg.id"
                    :class="['chat-message', msg.senderType === 'ADMIN' ? 'admin' : 'user']"
                  >
                    <div class="message-bubble">{{ msg.content }}</div>
                    <div class="message-time">{{ formatTime(msg.createTime) }}</div>
                  </div>
                </div>
                
                <div class="chat-input-area">
                  <input 
                    v-model="replyContent" 
                    type="text" 
                    placeholder="输入回复消息..."
                    @keyup.enter="sendReply"
                    :disabled="isSending"
                  />
                  <button class="send-btn" @click="sendReply" :disabled="!replyContent.trim() || isSending">
                    发送
                  </button>
                </div>
              </div>
              
              <div v-else class="empty-chat">
                <p>选择一个会话开始回复</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showBalanceModal" class="modal-overlay" @click.self="showBalanceModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ balanceOperation === 'add' ? '增加余额' : '减少余额' }}</h3>
          <span class="modal-close" @click="showBalanceModal = false">×</span>
        </div>
        <div class="modal-body">
          <p>用户: {{ selectedUser?.username }}</p>
          <p>当前余额: ¥{{ selectedUser?.balance?.toFixed(2) || '0.00' }}</p>
          <div class="amount-input">
            <span>金额:</span>
            <input v-model.number="balanceAmount" type="number" placeholder="输入金额" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showBalanceModal = false">取消</button>
          <button class="btn-confirm" @click="updateBalance">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: white;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.home-btn, .logout-btn {
  padding: 10px 20px;
  border-radius: 20px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.home-btn {
  background: rgba(255, 183, 77, 0.2);
  color: #FFB74D;
}

.home-btn:hover {
  background: rgba(255, 183, 77, 0.3);
}

.logout-btn {
  background: rgba(239, 83, 80, 0.2);
  color: #EF5350;
}

.logout-btn:hover {
  background: rgba(239, 83, 80, 0.3);
}

.header-center h1 {
  margin: 0;
  font-size: 24px;
}

.admin-content {
  display: flex;
  min-height: calc(100vh - 70px);
}

.sidebar {
  width: 200px;
  background: rgba(0, 0, 0, 0.2);
  padding: 20px 0;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.sidebar-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.sidebar-item.active {
  background: rgba(255, 183, 77, 0.2);
  border-left: 3px solid #FFB74D;
}

.item-icon {
  font-size: 18px;
}

.item-label {
  font-size: 14px;
}

.main-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.content-section {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 25px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
}

.search-box {
  display: flex;
  gap: 10px;
}

.search-box input {
  padding: 10px 15px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  outline: none;
}

.search-box input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.search-btn {
  padding: 10px 15px;
  background: rgba(255, 183, 77, 0.3);
  border: none;
  border-radius: 20px;
  cursor: pointer;
  color: #FFB74D;
}

.users-table {
  overflow-x: auto;
}

.users-table table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th, .users-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.users-table th {
  background: rgba(255, 255, 255, 0.05);
}

.member-status {
  font-weight: 500;
}

.member-status.member {
  color: #FFD700;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 10px;
  font-size: 12px;
  cursor: pointer;
  margin-right: 5px;
  transition: all 0.3s;
}

.add-btn {
  background: rgba(76, 175, 80, 0.3);
  color: #4CAF50;
}

.add-btn:hover {
  background: rgba(76, 175, 80, 0.5);
}

.minus-btn {
  background: rgba(255, 152, 0, 0.3);
  color: #FF9800;
}

.minus-btn:hover {
  background: rgba(255, 152, 0, 0.5);
}

.delete-btn {
  background: rgba(239, 83, 80, 0.3);
  color: #EF5350;
}

.delete-btn:hover {
  background: rgba(239, 83, 80, 0.5);
}

.approve-btn {
  background: rgba(76, 175, 80, 0.3);
  color: #4CAF50;
}

.approve-btn:hover {
  background: rgba(76, 175, 80, 0.5);
}

.reject-btn {
  background: rgba(239, 83, 80, 0.3);
  color: #EF5350;
}

.reject-btn:hover {
  background: rgba(239, 83, 80, 0.5);
}

.applications-list, .posts-list, .levelup-list, .reviews-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.application-card, .post-card, .levelup-card, .review-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
}

.app-header, .post-header, .levelup-header, .review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.app-name, .post-author, .levelup-counselor, .review-counselor {
  font-weight: bold;
}

.post-author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.post-user-id {
  font-size: 12px;
  color: rgba(255, 183, 77, 0.8);
}

.post-content-wrapper {
  margin-bottom: 15px;
}

.post-content-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 5px;
  display: block;
}

.app-title {
  padding: 4px 12px;
  background: rgba(255, 183, 77, 0.2);
  border-radius: 10px;
  font-size: 12px;
}

.app-specialty {
  margin: 0 0 10px 0;
  color: rgba(255, 255, 255, 0.7);
}

.app-desc, .post-content, .levelup-reason, .review-comment {
  margin: 0 0 15px 0;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.5;
}

.app-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.post-time, .review-time {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-stats {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
}

.review-rating {
  color: #FFB74D;
}

.empty-state {
  text-align: center;
  padding: 50px;
  color: rgba(255, 255, 255, 0.5);
}

.messages-container {
  display: flex;
  gap: 20px;
  height: 500px;
}

.messages-list {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 15px;
  overflow-y: auto;
}

.message-item {
  padding: 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.message-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.message-item.selected {
  background: rgba(255, 183, 77, 0.2);
}

.msg-content {
  margin: 0 0 5px 0;
  font-size: 14px;
}

.msg-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.reply-panel {
  width: 400px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
}

.selected-message {
  margin-bottom: 20px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
}

.selected-message p {
  margin: 0 0 10px 0;
  color: rgba(255, 255, 255, 0.6);
}

.msg-text {
  color: white !important;
  font-size: 14px;
}

.reply-input {
  width: 100%;
  height: 150px;
  padding: 15px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  resize: none;
  margin-bottom: 15px;
  box-sizing: border-box;
}

.reply-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.reply-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #FFB74D, #FF9800);
  border: none;
  border-radius: 12px;
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.reply-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 183, 77, 0.4);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #1a1a2e;
  border-radius: 20px;
  width: 400px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-header h3 {
  margin: 0;
}

.modal-close {
  font-size: 28px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.5);
}

.modal-body {
  padding: 20px;
}

.modal-body p {
  margin: 0 0 10px 0;
}

.amount-input {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 15px;
}

.amount-input input {
  flex: 1;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  outline: none;
}

.modal-footer {
  display: flex;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.btn-cancel, .btn-confirm {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.btn-confirm {
  background: linear-gradient(135deg, #FFB74D, #FF9800);
  color: white;
}

.customer-service-section {
  padding: 20px;
}

.customer-service-container {
  display: flex;
  gap: 20px;
  height: 600px;
}

.conversation-list {
  width: 300px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 15px;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.conversation-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.conversation-item.active {
  background: rgba(255, 183, 77, 0.2);
  border-color: #FFB74D;
}

.conv-avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  flex-shrink: 0;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conv-name {
  font-weight: bold;
  font-size: 14px;
}

.unread-badge {
  background: #dc3545;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  min-width: 20px;
  text-align: center;
}

.conv-preview {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 4px;
}

.chat-panel {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
}

.chat-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  padding: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-user-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
}

.chat-user-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chat-user-name {
  font-weight: bold;
  font-size: 14px;
}

.chat-user-id {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.system-message-hint {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  margin-bottom: 10px;
}

.chat-message {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.chat-message.user {
  align-self: flex-start;
}

.chat-message.admin {
  align-self: flex-end;
  align-items: flex-end;
}

.message-bubble {
  background: rgba(255, 255, 255, 0.1);
  padding: 10px 15px;
  border-radius: 15px;
  font-size: 14px;
}

.chat-message.admin .message-bubble {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.message-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 4px;
  padding: 0 5px;
}

.chat-input-area {
  display: flex;
  gap: 10px;
  padding: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-input-area input {
  flex: 1;
  padding: 12px 15px;
  border: none;
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  font-size: 14px;
  outline: none;
}

.chat-input-area input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.chat-input-area input:focus {
  background: rgba(255, 255, 255, 0.15);
}

.send-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 25px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.5);
}
</style>