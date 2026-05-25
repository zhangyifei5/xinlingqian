<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { adminCustomerServiceApi } from '../api'

const conversations = ref([])
const selectedConversation = ref(null)
const messages = ref([])
const inputMessage = ref('')
const isSending = ref(false)
const messagesContainer = ref(null)
const loading = ref(false)

const loadConversations = async () => {
  try {
    loading.value = true
    const response = await adminCustomerServiceApi.getConversations()
    if (response.data.code === 200) {
      conversations.value = response.data.data
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMessages = async () => {
  if (!selectedConversation.value) return
  try {
    const response = await adminCustomerServiceApi.getHistory(selectedConversation.value.userId)
    if (response.data.code === 200) {
      messages.value = response.data.data
      selectedConversation.value.unreadCount = 0
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
  if (!inputMessage.value.trim() || isSending.value || !selectedConversation.value) return
  
  isSending.value = true
  try {
    const response = await adminCustomerServiceApi.reply(selectedConversation.value.userId, inputMessage.value)
    if (response.data.code === 200) {
      messages.value.push({
        id: response.data.data.id,
        content: response.data.data.content,
        senderType: 'ADMIN',
        createTime: response.data.data.createTime
      })
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

const selectConversation = (conversation) => {
  selectedConversation.value = conversation
  loadMessages()
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

const hasUnreadMessages = (conversation) => {
  return conversation.unreadCount > 0
}

const getTotalUnread = computed(() => {
  return conversations.value.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0)
})

onMounted(() => {
  loadConversations()
  setInterval(() => {
    loadConversations()
  }, 5000)
})
</script>

<template>
  <div class="admin-customer-service-page">
    <div class="page-header">
      <h1>在线客服管理</h1>
    </div>

    <div class="message-container">
      <div class="sidebar">
        <div class="sidebar-header">
          <h2>用户会话</h2>
          <div class="header-right">
            <span v-if="getTotalUnread > 0" class="total-unread">{{ getTotalUnread }}</span>
            <span class="count">{{ conversations.length }}</span>
          </div>
        </div>
        
        <div class="conversation-list">
          <div 
            v-for="conversation in conversations" 
            :key="conversation.userId"
            :class="['conversation-item', { active: selectedConversation?.userId === conversation.userId }]"
            @click="selectConversation(conversation)"
          >
            <div class="conversation-avatar">
              {{ conversation.nickname?.charAt(0) || '?' }}
            </div>
            <div class="conversation-info">
              <div class="conversation-header">
                <span class="user-name">{{ conversation.nickname }}</span>
                <span v-if="hasUnreadMessages(conversation)" class="unread-badge">
                  {{ conversation.unreadCount }}
                </span>
              </div>
              <div class="conversation-preview">
                <span v-if="conversation.lastMessage">{{ conversation.lastMessage }}</span>
                <span v-else class="no-message">暂无消息</span>
              </div>
              <div class="conversation-footer">
                <span class="time">{{ formatDate(conversation.lastMessageTime) }}</span>
              </div>
            </div>
          </div>
          
          <div v-if="conversations.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
            </div>
            <p>暂无用户咨询</p>
          </div>
        </div>
      </div>

      <div class="chat-area">
        <div v-if="selectedConversation" class="chat-content">
          <div class="chat-header">
            <div class="user-info">
              <div class="user-avatar">
                {{ selectedConversation.nickname?.charAt(0) || '?' }}
              </div>
              <div class="user-detail">
                <span class="user-name">{{ selectedConversation.nickname }}</span>
                <span class="user-id">用户ID: {{ selectedConversation.userId }}</span>
              </div>
            </div>
          </div>

          <div ref="messagesContainer" class="messages-container">
            <div class="system-message">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              您正在与用户交流，请注意保护用户隐私
            </div>
            
            <div 
              v-for="message in messages" 
              :key="message.id"
              :class="['message-item', message.senderType === 'ADMIN' ? 'admin' : 'user']"
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
              placeholder="输入回复消息..."
              @keyup.enter="sendMessage"
            />
            <button 
              class="send-btn" 
              @click="sendMessage"
              :disabled="!inputMessage.trim() || isSending"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
              </svg>
              发送
            </button>
          </div>
        </div>

        <div v-else class="empty-chat">
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
          </div>
          <p>选择一个用户会话开始回复</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-customer-service-page {
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

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.total-unread {
  background: #dc3545;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
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

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.conversation-item {
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

.conversation-item:hover {
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.conversation-item.active {
  background: #ffffff;
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}

.conversation-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFB74D 0%, #FF9800 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3);
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
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

.unread-badge {
  background: #dc3545;
  color: white;
  min-width: 20px;
  height: 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  padding: 0 6px;
}

.conversation-preview {
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

.conversation-footer {
  display: flex;
  justify-content: flex-end;
}

.time {
  color: #adb5bd;
  font-size: 12px;
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

.user-info {
  display: flex;
  gap: 14px;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFB74D 0%, #FF9800 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3);
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  color: #1a1a2e;
  font-weight: 600;
  font-size: 16px;
}

.user-id {
  font-size: 13px;
  color: #6c757d;
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

.message-item.admin {
  align-self: flex-end;
}

.message-content {
  background: white;
  border-radius: 16px;
  padding: 14px 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e9ecef;
}

.message-item.admin .message-content {
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

.send-btn {
  padding: 14px 24px;
  border: none;
  border-radius: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
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
