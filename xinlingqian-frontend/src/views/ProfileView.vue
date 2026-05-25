<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { userApi, fileApi, customerServiceApi } from '../api'
import BackButton from '../components/BackButton.vue'
const router = useRouter()

const userInfo = ref({
  nickname: '',
  avatar: '',
  signature: '',
  balance: 0,
  phone: '',
  createTime: '',
  role: ''
})

const isCounselor = ref(false)
const isMember = ref(false)
const memberExpireDate = ref('')

const showEditModal = ref(false)
const showRechargeModal = ref(false)
const showServiceModal = ref(false)
const showVersionModal = ref(false)

const editForm = ref({
  nickname: '',
  avatar: '',
  signature: ''
})

const tempAvatar = ref('')
const uploading = ref(false)

const rechargeAmount = ref(0)
const versionInfo = ref({
  version: '',
  latestVersion: '',
  updateAvailable: false,
  updateContent: ''
})

const showChatModal = ref(false)
const chatMessages = ref([
  { type: 'system', content: '您好！我是心灵签的智能客服，很高兴为您服务。请问有什么可以帮助您的？', time: '刚刚' }
])
const chatInput = ref('')
const isSending = ref(false)
const chatMessagesRef = ref(null)

const faqItems = [
  { question: '如何修改个人资料？', answer: '您可以在个人中心点击「个人资料」进入编辑页面，修改昵称、头像和个性签名。' },
  { question: '充值后余额什么时候到账？', answer: '充值成功后余额会立即到账，您可以在「我的钱包」中查看实时余额。' },
  { question: '测试报告可以保存多久？', answer: '测试报告将永久保存，您可以随时在「心情档案」中查看历史记录。' },
  { question: '如何联系人工客服？', answer: '您可以发送消息给我，人工客服会尽快回复您。' },
  { question: '忘记密码怎么办？', answer: '在登录页面点击「忘记密码」，通过手机号获取验证码即可重置密码。' },
]

const handleServiceClick = (title) => {
  if (title === '在线客服') {
    showChatModal.value = true
    loadChatHistory()
  } else if (title === '客服电话') {
    window.location.href = 'tel:400-888-8888'
  } else if (title === '邮件反馈') {
    window.location.href = 'mailto:support@xinlingqian.com'
  } else if (title === '使用帮助') {
    alert('常见问题解答页面开发中~')
  }
}

const loadChatHistory = async () => {
  try {
    const response = await customerServiceApi.getHistory()
    if (response.data.code === 200) {
      chatMessages.value = [
        { type: 'system', content: '您好！我是心灵签的智能客服，很高兴为您服务。请问有什么可以帮助您的？', time: '刚刚' }
      ]
      response.data.data.forEach(msg => {
        chatMessages.value.push({
          type: msg.senderType === 'USER' ? 'user' : 'system',
          content: msg.content,
          time: new Date(msg.createTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
        })
      })
      scrollToBottom()
    }
  } catch (error) {
    console.error('获取聊天记录失败:', error)
  }
}

const sendMessage = async () => {
  const message = chatInput.value.trim()
  if (!message || isSending.value) return
  
  isSending.value = true
  chatMessages.value.push({
    type: 'user',
    content: message,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  scrollToBottom()
  
  const sentMessage = chatInput.value
  chatInput.value = ''
  
  try {
    await customerServiceApi.sendMessage(sentMessage)
  } catch (error) {
    console.error('发送消息失败:', error)
  } finally {
    isSending.value = false
  }
}

const selectFaq = (faq) => {
  chatMessages.value.push({
    type: 'user',
    content: faq.question,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  scrollToBottom()
  
  setTimeout(() => {
    chatMessages.value.push({
      type: 'system',
      content: faq.answer,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
    scrollToBottom()
  }, 500)
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

const avatarOptions = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
  '🙂', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗',
  '😚', '😋', '😛', '😝', '🤑', '🤗', '🤭', '🤫',
  '🤔', '😐', '😑', '😶', '😏', '😒', '🙄', '😬'
]

const rechargeOptions = [10, 20, 50, 100, 200, 500]

const menuItems = [
  { icon: '🎨', title: '个人资料', action: 'edit' },
  { icon: '💰', title: '我的钱包', action: 'recharge' },
  { icon: '📜', title: '心情档案', action: 'records' },
  { icon: '🧘', title: '咨询师资料', action: 'counselor-profile', counselorOnly: true },
  { icon: '⚙️', title: '设置', action: 'settings' },
]

const messageCenterItem = { icon: '💬', title: '消息中心', action: 'messages' }

const serviceItems = [
  { icon: '💬', title: '在线客服', desc: '7x24小时在线服务' },
  { icon: '📞', title: '客服电话', desc: '400-888-8888' },
  { icon: '✉️', title: '邮件反馈', desc: 'support@xinlingqian.com' },
  { icon: '📄', title: '使用帮助', desc: '常见问题解答' },
]

const getUserInfo = async () => {
  try {
    const response = await userApi.getUserInfo()
    if (response.data.code === 200) {
      userInfo.value = response.data.data
      isCounselor.value = response.data.data.role === 'COUNSELOR'
      isMember.value = response.data.data.isMember === true
      if (response.data.data.memberExpireDate) {
        memberExpireDate.value = new Date(response.data.data.memberExpireDate).toLocaleDateString('zh-CN')
      }
      console.log('用户信息:', userInfo.value)
      console.log('是否为咨询师:', isCounselor.value)
      console.log('是否为会员:', isMember.value)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const openEditModal = () => {
  editForm.value = {
    nickname: userInfo.value.nickname || '',
    avatar: userInfo.value.avatar || '',
    signature: userInfo.value.signature || ''
  }
  tempAvatar.value = userInfo.value.avatar || ''
  showEditModal.value = true
}

const handleMenuClick = (action) => {
  switch (action) {
    case 'edit':
      openEditModal()
      break
    case 'recharge':
      showRechargeModal.value = true
      break
    case 'records':
      window.location.href = '/mood-calendar'
      break
    case 'counselor':
      window.location.href = '/my-counselor'
      break
    case 'counselor-profile':
      window.location.href = '/counselor/profile'
      break
    case 'messages':
      window.location.href = '/counselor-messages'
      break
    case 'membership':
      window.location.href = '/membership'
      break
    case 'meihua-yijing':
      if (!isMember.value) {
        alert('此功能仅对会员开放，请先开通会员')
        window.location.href = '/membership'
      } else {
        window.location.href = '/meihua-yijing'
      }
      break
    case 'settings':
      alert('设置功能开发中~')
      break
  }
}

const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const response = await fileApi.upload(formData)
    if (response.data.code === 200) {
      tempAvatar.value = response.data.data
      editForm.value.avatar = response.data.data
      alert('上传成功')
    } else {
      alert('上传失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('上传失败:', error)
    alert('上传失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}

const selectAvatarEmoji = (avatar) => {
  tempAvatar.value = avatar
  editForm.value.avatar = avatar
}

const saveProfile = async () => {
  console.log('保存数据:', editForm.value)
  try {
    const response = await userApi.updateUser(editForm.value)
    console.log('保存响应:', response)
    console.log('响应数据:', response.data)
    if (response.data.code === 200) {
      userInfo.value = { ...userInfo.value, ...response.data.data }
      showEditModal.value = false
      alert('保存成功')
    } else {
      console.log('保存失败，code:', response.data.code, 'message:', response.data.message)
      alert('保存失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('保存失败 - 错误对象:', error)
    if (error.response) {
      console.error('HTTP状态码:', error.response.status)
      console.error('响应数据:', error.response.data)
      alert('保存失败，HTTP错误: ' + error.response.status + ' - ' + (error.response.data?.message || '未知错误'))
    } else if (error.request) {
      console.error('请求已发出但无响应:', error.request)
      alert('保存失败，服务器无响应，请检查网络连接')
    } else {
      console.error('请求设置错误:', error.message)
      alert('保存失败，请求错误: ' + error.message)
    }
  }
}

const handleRecharge = async () => {
  if (rechargeAmount.value <= 0) {
    alert('请输入充值金额')
    return
  }
  try {
    const response = await userApi.recharge(rechargeAmount.value)
    if (response.data.code === 200) {
      userInfo.value.balance = response.data.data.balance
      showRechargeModal.value = false
      rechargeAmount.value = 0
      alert('充值成功')
    } else {
      alert('充值失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('充值失败:', error)
    alert('充值失败，请稍后重试')
  }
}

const checkVersion = async () => {
  try {
    const response = await userApi.checkVersion()
    if (response.data.code === 200) {
      versionInfo.value = response.data.data
      showVersionModal.value = true
    }
  } catch (error) {
    console.error('检查版本失败:', error)
  }
}

const handleLogout = () => {
  localStorage.clear()
  window.location.href = '/login'
}

onMounted(() => {
  getUserInfo()
})
</script>

<template>
  <div class="profile-container">
    <div class="profile-bg"></div>
    
    <div class="profile-header">
      <BackButton />
      <div class="header-content">
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="openEditModal">
            <img v-if="userInfo.avatar && userInfo.avatar.startsWith('http') || userInfo.avatar?.startsWith('/uploads')" 
                 :src="userInfo.avatar?.startsWith('/uploads') ? 'http://localhost:8080' + userInfo.avatar : userInfo.avatar" 
                 class="avatar-img" alt="头像" />
            <span v-else class="avatar-emoji">{{ userInfo.avatar || '👤' }}</span>
            <span v-if="isCounselor" class="counselor-badge">🧘</span>
            <span v-if="isMember" class="member-badge">👑</span>
          </div>
          <span class="edit-badge">✏️</span>
        </div>
        <div class="user-info">
          <h2 class="nickname">{{ userInfo.nickname || '未设置昵称' }}</h2>
          <p class="signature">{{ userInfo.signature || '暂无个性签名' }}</p>
        </div>
        <button class="logout-btn" @click="handleLogout">
          <span>退出登录</span>
        </button>
      </div>
      
      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-value">¥{{ userInfo.balance?.toFixed(2) || '0.00' }}</span>
          <span class="stat-label">账户余额</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">🎯</span>
          <span class="stat-label">完成测试</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">📅</span>
          <span class="stat-label">注册时间</span>
        </div>
      </div>
    </div>

    <div class="menu-section">
      <div 
        v-for="item in menuItems" 
        :key="item.action" 
        class="menu-item"
        v-show="!item.counselorOnly || isCounselor"
        @click="handleMenuClick(item.action)"
      >
        <span class="menu-icon">{{ item.icon }}</span>
        <span class="menu-title">{{ item.title }}</span>
        <span class="menu-arrow">→</span>
      </div>
      <div 
        class="menu-item member-item"
        @click="handleMenuClick('membership')"
      >
        <span class="menu-icon">👑</span>
        <span class="menu-title">{{ isMember ? '会员中心' : '开通会员' }}</span>
        <span v-if="isMember" class="member-expire">到期: {{ memberExpireDate }}</span>
        <span v-else class="menu-arrow">→</span>
      </div>
      <div v-if="isMember" class="menu-item member-feature"
        @click="handleMenuClick('meihua-yijing')">
        <span class="menu-icon">🌸</span>
        <span class="menu-title">梅花易数</span>
        <span class="menu-desc">今日运势测试</span>
        <span class="menu-arrow">→</span>
      </div>
      <div 
        v-if="isCounselor" 
        class="menu-item message-item"
        @click="handleMenuClick(messageCenterItem.action)"
      >
        <span class="menu-icon">{{ messageCenterItem.icon }}</span>
        <span class="menu-title">{{ messageCenterItem.title }}</span>
        <span class="menu-arrow">→</span>
      </div>
    </div>

    <div class="service-section">
      <div class="section-header">
        <h3 class="section-title">
          <span class="title-icon">💁</span>
          <span>客服帮助</span>
        </h3>
      </div>
      <div class="service-grid">
        <div 
          v-for="item in serviceItems" 
          :key="item.title" 
          class="service-item"
          @click="handleServiceClick(item.title)"
        >
          <span class="service-icon">{{ item.icon }}</span>
          <div class="service-info">
            <span class="service-title">{{ item.title }}</span>
            <span class="service-desc">{{ item.desc }}</span>
          </div>
          <span class="service-arrow">→</span>
        </div>
      </div>
    </div>

    <div class="footer-section">
      <button class="footer-btn" @click="checkVersion">
        <span>检查版本更新</span>
      </button>
      <p class="copyright">© 2026 心灵签 - 用心倾听你的心声</p>
    </div>

    <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal-content edit-modal">
        <div class="modal-header">
          <h3>编辑个人资料</h3>
          <span class="modal-close" @click="showEditModal = false">×</span>
        </div>
        
        <div class="modal-body">
          <div class="avatar-upload-section">
            <label class="form-label">头像</label>
            <div class="avatar-preview-wrapper">
              <div class="avatar-preview">
                <img v-if="tempAvatar && (tempAvatar.startsWith('http') || tempAvatar.startsWith('/uploads'))" 
                     :src="tempAvatar.startsWith('/uploads') ? 'http://localhost:8080' + tempAvatar : tempAvatar" 
                     class="avatar-preview-img" alt="头像" />
                <span v-else class="avatar-preview-emoji">{{ tempAvatar || '👤' }}</span>
              </div>
              <label class="upload-btn">
                <input type="file" accept="image/*" @change="handleAvatarUpload" style="display: none" />
                <span v-if="uploading">上传中...</span>
                <span v-else>📷 上传图片</span>
              </label>
            </div>
          </div>
          
          <div class="avatar-select-section">
            <label class="form-label">或选择表情</label>
            <div class="avatar-grid">
              <span 
                v-for="avatar in avatarOptions" 
                :key="avatar"
                :class="['avatar-option', { active: tempAvatar === avatar }]"
                @click="selectAvatarEmoji(avatar)"
              >{{ avatar }}</span>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label">昵称</label>
            <input 
              v-model="editForm.nickname" 
              type="text" 
              class="form-input"
              placeholder="请输入昵称"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">个性签名</label>
            <textarea 
              v-model="editForm.signature" 
              class="form-textarea"
              placeholder="说说你的心里话..."
              rows="3"
            ></textarea>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-cancel" @click="showEditModal = false">取消</button>
          <button class="btn-save" @click="saveProfile">保存</button>
        </div>
      </div>
    </div>

    <div v-if="showRechargeModal" class="modal-overlay" @click.self="showRechargeModal = false">
      <div class="modal-content recharge-modal">
        <div class="modal-header">
          <h3>充值中心</h3>
          <span class="modal-close" @click="showRechargeModal = false">×</span>
        </div>
        
        <div class="modal-body">
          <div class="balance-info">
            <span class="balance-label">当前余额</span>
            <span class="balance-value">¥{{ userInfo.balance?.toFixed(2) || '0.00' }}</span>
          </div>
          
          <div class="form-group">
            <label class="form-label">选择金额</label>
            <div class="amount-grid">
              <button 
                v-for="amount in rechargeOptions" 
                :key="amount"
                :class="['amount-btn', { active: rechargeAmount === amount }]"
                @click="rechargeAmount = amount"
              >¥{{ amount }}</button>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label">自定义金额</label>
            <div class="custom-amount">
              <span class="amount-prefix">¥</span>
              <input 
                v-model.number="rechargeAmount" 
                type="number" 
                class="form-input amount-input"
                placeholder="输入金额"
              />
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-cancel" @click="showRechargeModal = false">取消</button>
          <button class="btn-recharge" @click="handleRecharge">立即充值</button>
        </div>
      </div>
    </div>

    <div v-if="showVersionModal" class="modal-overlay" @click.self="showVersionModal = false">
      <div class="modal-content version-modal">
        <div class="modal-header">
          <h3>版本信息</h3>
          <span class="modal-close" @click="showVersionModal = false">×</span>
        </div>
        
        <div class="modal-body">
          <div class="version-info">
            <div class="version-row">
              <span class="version-label">当前版本</span>
              <span class="version-value">{{ versionInfo.version }}</span>
            </div>
            <div class="version-row">
              <span class="version-label">最新版本</span>
              <span class="version-value">{{ versionInfo.latestVersion }}</span>
            </div>
            <div v-if="versionInfo.updateAvailable" class="update-info">
              <span class="update-icon">🔔</span>
              <span class="update-text">有新版本可用！</span>
            </div>
            <div class="update-content">
              <h4>更新内容</h4>
              <p>{{ versionInfo.updateContent }}</p>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-confirm" @click="showVersionModal = false">确定</button>
        </div>
      </div>
    </div>

    <div v-if="showChatModal" class="modal-overlay" @click.self="showChatModal = false">
      <div class="modal-content chat-modal">
        <div class="modal-header">
          <h3>在线客服</h3>
          <span class="modal-close" @click="showChatModal = false">×</span>
        </div>
        
        <div class="modal-body">
          <div class="faq-section">
            <h4 class="faq-title">常见问题</h4>
            <div class="faq-list">
              <div 
                v-for="(faq, index) in faqItems" 
                :key="index" 
                class="faq-item"
                @click="selectFaq(faq)"
              >
                <span class="faq-dot">•</span>
                <span class="faq-question">{{ faq.question }}</span>
              </div>
            </div>
          </div>
          
          <div class="chat-section">
            <div class="chat-messages" ref="chatMessagesRef">
              <div 
                v-for="(message, index) in chatMessages" 
                :key="index"
                :class="['chat-message', message.type]"
              >
                <div class="message-content">
                  <span class="message-text">{{ message.content }}</span>
                </div>
                <span class="message-time">{{ message.time }}</span>
              </div>
            </div>
            
            <div class="chat-input-area">
              <input 
                v-model="chatInput" 
                type="text" 
                class="chat-input"
                placeholder="输入您的问题..."
                @keyup.enter="sendMessage"
              />
              <button class="chat-send-btn" @click="sendMessage">发送</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF9F0 0%, #FFFBF5 50%, #FFFFFF 100%);
  padding-bottom: 100px;
  position: relative;
}

.profile-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 280px;
  background: linear-gradient(135deg, #FFF5E6 0%, #FFE4C4 50%, #FFDAB9 100%);
  border-radius: 0 0 40px 40px;
  pointer-events: none;
}

.profile-header {
  position: relative;
  z-index: 1;
  padding: 40px 20px 20px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.avatar-section {
  position: relative;
}

.avatar-wrapper {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFE4E1 0%, #FFB6C1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 25px rgba(255, 183, 77, 0.3);
  transition: all 0.3s ease;
  cursor: pointer;
  overflow: hidden;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 35px rgba(255, 143, 0, 0.4);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-emoji {
  font-size: 45px;
}

.edit-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  box-shadow: 0 2px 10px rgba(255, 143, 0, 0.3);
}

.counselor-badge {
  position: absolute;
  top: -2px;
  left: -2px;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.4);
  border: 2px solid white;
}

.member-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  box-shadow: 0 2px 10px rgba(255, 215, 0, 0.4);
  border: 2px solid white;
}

.user-info {
  flex: 1;
}

.nickname {
  font-size: 24px;
  font-weight: 700;
  color: #5D4037;
  margin: 0 0 8px 0;
}

.signature {
  font-size: 14px;
  color: #9E9E9E;
  margin: 0;
}

.logout-btn {
  padding: 8px 16px;
  background: rgba(255, 87, 34, 0.1);
  border: none;
  border-radius: 20px;
  color: #FF5722;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(255, 87, 34, 0.2);
}

.stats-row {
  display: flex;
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(255, 183, 77, 0.15);
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #FF8F00;
  display: block;
}

.stat-label {
  font-size: 12px;
  color: #9E9E9E;
}

.stat-divider {
  width: 1px;
  background: #E0E0E0;
  margin: 0 10px;
}

.menu-section {
  margin: 20px;
  background: white;
  border-radius: 20px;
  padding: 8px 0;
  box-shadow: 0 4px 20px rgba(255, 183, 77, 0.1);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.menu-item:hover {
  background: #FFF8E7;
}

.member-item {
  background: linear-gradient(135deg, #d4a574 0%, #c9956c 100%);
  color: white;
  border-radius: 12px;
  margin-top: 8px;
}

.member-item:hover {
  background: linear-gradient(135deg, #c9956c 0%, #b8845c 100%);
}

.member-expire {
  font-size: 12px;
  opacity: 0.9;
  margin-left: auto;
  margin-right: 8px;
}

.member-feature {
  background: linear-gradient(135deg, #f8f6f2 0%, #fff8f0 100%);
  border: 2px solid #d4a574;
  border-radius: 12px;
  margin-top: 8px;
}

.member-feature:hover {
  background: linear-gradient(135deg, #f5ebe0 0%, #faf5f0 100%);
}

.menu-desc {
  font-size: 12px;
  color: #a0856c;
  margin-left: 8px;
}

.message-item {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  margin-top: 8px;
}

.message-item:hover {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
}

.menu-icon {
  font-size: 24px;
}

.menu-title {
  flex: 1;
  font-size: 15px;
  color: #5D4037;
}

.menu-arrow {
  font-size: 16px;
  color: #BDBDBD;
}

.service-section {
  margin: 20px;
}

.section-header {
  margin-bottom: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #5D4037;
}

.title-icon {
  font-size: 18px;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 15px rgba(255, 183, 77, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.service-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 143, 0, 0.15);
}

.service-icon {
  font-size: 22px;
}

.service-info {
  display: flex;
  flex-direction: column;
}

.service-title {
  font-size: 13px;
  font-weight: 600;
  color: #5D4037;
}

.service-desc {
  font-size: 11px;
  color: #9E9E9E;
}

.footer-section {
  margin: 20px;
  text-align: center;
}

.footer-btn {
  padding: 12px 30px;
  background: white;
  border: 1px solid #FFE0B2;
  border-radius: 30px;
  color: #FF8F00;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.footer-btn:hover {
  background: #FFF8E7;
}

.copyright {
  font-size: 12px;
  color: #BDBDBD;
  margin-top: 16px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: white;
  border-radius: 24px;
  width: 90%;
  max-width: 400px;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #F5F5F5;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #5D4037;
}

.modal-close {
  font-size: 28px;
  color: #BDBDBD;
  cursor: pointer;
  line-height: 1;
}

.modal-body {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #5D4037;
  display: block;
  margin-bottom: 8px;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #E0E0E0;
  border-radius: 12px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #FF8F00;
}

.form-textarea {
  resize: none;
}

.form-group {
  margin-bottom: 20px;
}

.avatar-upload-section {
  margin-bottom: 20px;
  text-align: center;
}

.avatar-preview-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFE4E1 0%, #FFB6C1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-preview-emoji {
  font-size: 50px;
}

.upload-btn {
  display: inline-block;
  padding: 10px 24px;
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  color: white;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.upload-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 143, 0, 0.4);
}

.avatar-select-section {
  margin-bottom: 20px;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
}

.avatar-option {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #FFF8E7;
}

.avatar-option:hover {
  transform: scale(1.1);
}

.avatar-option.active {
  background: linear-gradient(135deg, #FFE0B2, #FFD54F);
  box-shadow: 0 0 0 3px #FF8F00;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #F5F5F5;
}

.btn-cancel, .btn-save, .btn-recharge, .btn-confirm {
  flex: 1;
  padding: 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.btn-cancel {
  background: #F5F5F5;
  color: #616161;
}

.btn-cancel:hover {
  background: #E0E0E0;
}

.btn-save, .btn-recharge, .btn-confirm {
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  color: white;
}

.btn-save:hover, .btn-recharge:hover, .btn-confirm:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(255, 143, 0, 0.3);
}

.balance-info {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #FFF8E7, #FFF3E0);
  border-radius: 16px;
  margin-bottom: 20px;
}

.balance-label {
  font-size: 14px;
  color: #9E9E9E;
  display: block;
}

.balance-value {
  font-size: 32px;
  font-weight: 800;
  color: #FF8F00;
}

.amount-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.amount-btn {
  padding: 12px;
  border: 2px solid #E0E0E0;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #5D4037;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.amount-btn:hover {
  border-color: #FFE0B2;
}

.amount-btn.active {
  border-color: #FF8F00;
  background: #FFF8E7;
  color: #FF8F00;
}

.custom-amount {
  display: flex;
  align-items: center;
  border: 1px solid #E0E0E0;
  border-radius: 12px;
  padding: 0 12px;
}

.amount-prefix {
  font-size: 18px;
  font-weight: 600;
  color: #FF8F00;
}

.amount-input {
  border: none;
  padding: 12px;
  font-size: 16px;
}

.version-info {
  padding: 10px;
}

.version-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #F5F5F5;
}

.version-label {
  color: #9E9E9E;
}

.version-value {
  font-weight: 600;
  color: #5D4037;
}

.update-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #E8F5E9;
  border-radius: 12px;
  margin: 12px 0;
}

.update-icon {
  font-size: 20px;
}

.update-text {
  color: #4CAF50;
  font-weight: 600;
}

.update-content h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #5D4037;
}

.update-content p {
  margin: 0;
  font-size: 13px;
  color: #9E9E9E;
  line-height: 1.6;
}

.service-arrow {
  font-size: 16px;
  color: #BDBDBD;
  margin-left: auto;
}

.chat-modal {
  max-width: 450px;
  max-height: 80vh;
  overflow: hidden;
}

.chat-modal .modal-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: calc(80vh - 80px);
}

.faq-section {
  padding-bottom: 16px;
  border-bottom: 1px solid #F5F5F5;
}

.faq-title {
  font-size: 14px;
  font-weight: 600;
  color: #5D4037;
  margin: 0 0 12px 0;
}

.faq-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.faq-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #FFF8E7;
  border-radius: 20px;
  font-size: 12px;
  color: #5D4037;
  cursor: pointer;
  transition: all 0.3s ease;
}

.faq-item:hover {
  background: #FFE0B2;
  transform: translateY(-2px);
}

.faq-dot {
  color: #FF8F00;
}

.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 200px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-right: 8px;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #F5F5F5;
  border-radius: 2px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #FFD54F;
  border-radius: 2px;
}

.chat-message {
  display: flex;
  flex-direction: column;
  max-width: 75%;
}

.chat-message.user {
  align-items: flex-end;
  margin-left: auto;
}

.chat-message.system {
  align-items: flex-start;
}

.message-content {
  padding: 10px 14px;
  border-radius: 18px;
  word-break: break-word;
}

.chat-message.user .message-content {
  background: linear-gradient(135deg, #FF8F00, #FFC107);
}

.chat-message.system .message-content {
  background: #F5F5F5;
}

.message-text {
  font-size: 13px;
  line-height: 1.5;
}

.chat-message.user .message-text {
  color: white;
}

.chat-message.system .message-text {
  color: #5D4037;
}

.message-time {
  font-size: 10px;
  color: #BDBDBD;
  margin-top: 4px;
}

.chat-message.user .message-time {
  text-align: right;
}

.chat-message.system .message-time {
  text-align: left;
}

.chat-input-area {
  display: flex;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid #F5F5F5;
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #E0E0E0;
  border-radius: 25px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s ease;
}

.chat-input:focus {
  border-color: #FF8F00;
}

.chat-send-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #FF8F00, #FFC107);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chat-send-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(255, 143, 0, 0.3);
}
</style>
