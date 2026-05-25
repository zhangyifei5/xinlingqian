<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { consultationApi } from '../api'
import BackButton from '../components/BackButton.vue'

const router = useRouter()

const consultations = ref([])
const selectedConsultation = ref(null)
const messages = ref([])
const inputMessage = ref('')
const isSending = ref(false)
const messagesContainer = ref(null)
const loading = ref(false)

const filteredConsultations = computed(() => {
  return consultations.value
})

const loadConsultations = async () => {
  try {
    loading.value = true
    const response = await consultationApi.getCounselorConsultations()
    if (response.data.code === 200) {
      consultations.value = response.data.data
      if (consultations.value.length > 0 && !selectedConsultation.value) {
        selectedConsultation.value = consultations.value[0]
        loadMessages()
      }
    }
  } catch (error) {
    console.error('获取咨询列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMessages = async () => {
  if (!selectedConsultation.value) return
  try {
    const response = await consultationApi.getMessages(selectedConsultation.value.id)
    if (response.data.code === 200) {
      messages.value = response.data.data
      await nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('获取消息失败:', error)
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isSending.value || !selectedConsultation.value) return
  
  isSending.value = true
  try {
    const response = await consultationApi.sendMessage(selectedConsultation.value.id, {
      content: inputMessage.value,
      senderType: 'COUNSELOR'
    })
    if (response.data.code === 200) {
      messages.value.push(response.data.data)
      inputMessage.value = ''
      await nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('发送消息失败:', error)
  } finally {
    isSending.value = false
  }
}

const selectConsultation = (consultation) => {
  selectedConsultation.value = consultation
  loadMessages()
}

const acceptConsultation = async () => {
  if (!selectedConsultation.value) return
  try {
    const response = await consultationApi.startConsultation(selectedConsultation.value.id)
    if (response.data.code === 200) {
      selectedConsultation.value.status = 'ACTIVE'
      alert('已接受咨询')
    }
  } catch (error) {
    console.error('接受咨询失败:', error)
    alert('操作失败')
  }
}

const completeConsultation = async () => {
  if (!selectedConsultation.value) return
  try {
    const response = await consultationApi.completeConsultation(selectedConsultation.value.id)
    if (response.data.code === 200) {
      selectedConsultation.value.status = 'COMPLETED'
      alert('咨询已结束')
    }
  } catch (error) {
    console.error('结束咨询失败:', error)
    alert('操作失败')
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

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待确认',
    'ACTIVE': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusClass = (status) => {
  const classMap = {
    'PENDING': 'status-pending',
    'ACTIVE': 'status-active',
    'COMPLETED': 'status-completed',
    'CANCELLED': 'status-cancelled'
  }
  return classMap[status] || ''
}

const hasUnreadMessages = (consultation) => {
  return consultation.unreadCount > 0
}

const getPaymentStatus = (consultation) => {
  if (consultation.paidAmount > 0) {
    return `¥${consultation.paidAmount.toFixed(2)}`
  }
  return '未支付'
}

onMounted(() => {
  loadConsultations()
  setInterval(() => {
    loadConsultations()
    if (selectedConsultation.value) {
      loadMessages()
    }
  }, 5000)
})
</script>

<template>
  <div class="counselor-message-page">
    <BackButton />
    
    <div class="page-header">
      <h1>咨询师消息中心</h1>
    </div>

    <div class="message-container">
      <div class="sidebar">
        <div class="sidebar-header">
          <h2>咨询会话</h2>
          <span class="count">{{ filteredConsultations.length }}</span>
        </div>
        
        <div class="consultation-list">
          <div 
            v-for="consultation in filteredConsultations" 
            :key="consultation.id"
            :class="['consultation-item', { active: selectedConsultation?.id === consultation.id }]"
            @click="selectConsultation(consultation)"
          >
            <div class="consultation-avatar">
              {{ consultation.userNickname?.charAt(0) || '?' }}
            </div>
            <div class="consultation-info">
              <div class="consultation-header">
                <span class="user-name">{{ consultation.userNickname || '匿名用户' }}</span>
                <span :class="['status', getStatusClass(consultation.status)]">
                  {{ getStatusText(consultation.status) }}
                </span>
              </div>
              <div class="consultation-preview">
                <span v-if="consultation.lastMessage">{{ consultation.lastMessage }}</span>
                <span v-else class="no-message">暂无消息</span>
              </div>
              <div class="consultation-footer">
                <span class="time">{{ formatDate(consultation.updateTime) }}</span>
                <span v-if="consultation.paidAmount > 0" class="payment-badge paid">
                  ¥{{ consultation.paidAmount.toFixed(2) }}
                </span>
                <span v-else class="payment-badge unpaid">
                  待支付
                </span>
              </div>
            </div>
          </div>
          
          <div v-if="filteredConsultations.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
            </div>
            <p>暂无咨询会话</p>
          </div>
        </div>
      </div>

      <div class="chat-area">
        <div v-if="selectedConsultation" class="chat-content">
          <div class="chat-header">
            <div class="counselor-info">
              <div class="client-avatar">
                {{ selectedConsultation.userNickname?.charAt(0) || '?' }}
              </div>
              <div class="client-detail">
                <span class="client-name">{{ selectedConsultation.userNickname || '匿名用户' }}</span>
                <div class="status-row">
                  <span :class="['status', getStatusClass(selectedConsultation.status)]">
                    {{ getStatusText(selectedConsultation.status) }}
                  </span>
                  <span class="payment-info">
                    支付金额: <strong>¥{{ selectedConsultation.paidAmount?.toFixed(2) || '0.00' }}</strong>
                  </span>
                </div>
              </div>
            </div>
            <div class="chat-actions">
              <button v-if="selectedConsultation.status === 'PENDING'" class="action-btn accept-btn" @click="acceptConsultation">
                接受咨询
              </button>
              <button v-if="selectedConsultation.status === 'ACTIVE'" class="action-btn complete-btn" @click="completeConsultation">
                结束咨询
              </button>
            </div>
          </div>

          <div ref="messagesContainer" class="messages-container">
            <div class="system-message">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              欢迎与客户交流，请注意保护客户隐私
            </div>
            
            <div 
              v-for="message in messages" 
              :key="message.id"
              :class="['message-item', message.senderType === 'COUNSELOR' ? 'counselor' : 'user']"
            >
              <div class="message-content">
                <span class="message-text">{{ message.content }}</span>
              </div>
              <span class="message-time">{{ formatTime(message.createTime) }}</span>
            </div>
          </div>

          <div class="input-area">
            <input 
              v-model="inputMessage" 
              type="text" 
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
              :disabled="selectedConsultation.status === 'COMPLETED' || selectedConsultation.status === 'CANCELLED'"
            />
            <button 
              class="send-btn" 
              @click="sendMessage"
              :disabled="!inputMessage.trim() || selectedConsultation.status === 'COMPLETED' || selectedConsultation.status === 'CANCELLED'"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
              </svg>
            </button>
          </div>
        </div>

        <div v-else class="empty-chat">
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
          </div>
          <p>选择一个会话开始交流</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.counselor-message-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.page-header h1 {
  color: white;
  font-size: 22px;
  margin: 0;
  font-weight: 600;
  letter-spacing: 1px;
}

.message-container {
  flex: 1;
  display: flex;
  gap: 0;
  overflow: hidden;
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.sidebar {
  width: 360px;
  background: white;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e9ecef;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.sidebar-header h2 {
  color: #1a1a2e;
  font-size: 18px;
  margin: 0;
  font-weight: 600;
}

.count {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.consultation-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.consultation-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.consultation-item:hover {
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.consultation-item.active {
  background: #ffffff;
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}

.consultation-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.consultation-info {
  flex: 1;
  min-width: 0;
}

.consultation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.user-name {
  color: #1a1a2e;
  font-weight: 600;
  font-size: 15px;
}

.status {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: 500;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-active {
  background: #d4edda;
  color: #155724;
}

.status-completed {
  background: #e9ecef;
  color: #6c757d;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
}

.consultation-preview {
  color: #6c757d;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
}

.no-message {
  color: #adb5bd;
  font-style: italic;
}

.consultation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time {
  color: #adb5bd;
  font-size: 12px;
}

.payment-badge {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 10px;
  font-weight: 500;
}

.payment-badge.paid {
  background: #d4edda;
  color: #155724;
}

.payment-badge.unpaid {
  background: #fff3cd;
  color: #856404;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #adb5bd;
}

.empty-icon {
  margin-bottom: 16px;
  color: #dee2e6;
}

.empty-icon svg {
  opacity: 0.5;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 16px;
  margin-left: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #adb5bd;
}

.empty-chat .empty-icon {
  color: #dee2e6;
  margin-bottom: 20px;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 1px solid #e9ecef;
}

.counselor-info {
  display: flex;
  gap: 14px;
}

.client-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.client-detail {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.client-name {
  color: #1a1a2e;
  font-weight: 600;
  font-size: 16px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.payment-info {
  font-size: 13px;
  color: #6c757d;
}

.payment-info strong {
  color: #28a745;
  font-weight: 600;
}

.chat-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.accept-btn {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(40, 167, 69, 0.3);
}

.accept-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.4);
}

.complete-btn {
  background: linear-gradient(135deg, #FF9800 0%, #f57c00 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3);
}

.complete-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.4);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8f9fa;
}

.system-message {
  text-align: center;
  color: #6c757d;
  font-size: 13px;
  padding: 10px 20px;
  background: white;
  border-radius: 20px;
  align-self: center;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.message-item {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-item.user {
  align-self: flex-start;
}

.message-item.counselor {
  align-self: flex-end;
}

.message-content {
  background: white;
  border-radius: 16px;
  padding: 14px 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e9ecef;
}

.message-item.counselor .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.message-text {
  font-size: 14px;
  line-height: 1.6;
}

.message-time {
  color: #adb5bd;
  font-size: 11px;
  margin-top: 6px;
  padding: 0 8px;
}

.input-area {
  display: flex;
  gap: 12px;
  padding: 20px 24px;
  background: white;
  border-top: 1px solid #e9ecef;
}

.input-area input {
  flex: 1;
  padding: 14px 20px;
  border: 2px solid #e9ecef;
  border-radius: 24px;
  background: #f8f9fa;
  color: #1a1a2e;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.input-area input:focus {
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-area input::placeholder {
  color: #adb5bd;
}

.input-area input:disabled {
  background: #e9ecef;
  cursor: not-allowed;
}

.send-btn {
  width: 52px;
  height: 52px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  background: #dee2e6;
  box-shadow: none;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .message-container {
    flex-direction: column;
    padding: 12px;
  }
  
  .sidebar {
    width: 100%;
    max-height: 300px;
    margin-bottom: 12px;
  }
  
  .chat-area {
    margin-left: 0;
    flex: 1;
  }
}
</style>
