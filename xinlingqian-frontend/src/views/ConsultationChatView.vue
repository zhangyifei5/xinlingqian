<script setup>import { ref, onMounted, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { consultationApi, counselorApi } from '../api';
import BackButton from '../components/BackButton.vue';
const route = useRoute();
const consultationId = ref(null);
const counselor = ref(null);
const messages = ref([]);
const inputMessage = ref('');
const isSending = ref(false);
const messagesContainer = ref(null);
const currentUserId = ref(null);
const loadMessages = async () => {
 try {
 const response = await consultationApi.getMessages(consultationId.value);
 if (response.data.code === 200) {
 messages.value = response.data.data;
 await nextTick(() => {
 scrollToBottom();
 });
 }
 }
 catch (error) {
 console.error('获取消息失败:', error);
 }
};
const scrollToBottom = () => {
 if (messagesContainer.value) {
 messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
 }
};
const sendMessage = async () => {
 if (!inputMessage.value.trim() || isSending.value)
 return;
 isSending.value = true;
 try {
 const response = await consultationApi.sendMessage(consultationId.value, {
 content: inputMessage.value,
 senderType: 'USER'
 });
 if (response.data.code === 200) {
 messages.value.push(response.data.data);
 inputMessage.value = '';
 await nextTick(() => {
 scrollToBottom();
 });
 }
 }
 catch (error) {
 console.error('发送消息失败:', error);
 }
 finally {
 isSending.value = false;
 }
};
const formatTime = (dateStr) => {
 if (!dateStr)
 return '';
 const date = new Date(dateStr);
 return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};
onMounted(() => {
 consultationId.value = route.params.id;
 currentUserId.value = localStorage.getItem('userId');
 loadMessages();
 setInterval(() => {
 loadMessages();
 }, 3000);
});
</script>

<template>
  <div class="chat-page">
    <div class="chat-header">
      <BackButton />
      <div class="chat-title">
        <span class="chat-icon">💬</span>
        <span>咨询交流</span>
      </div>
    </div>

    <div ref="messagesContainer" class="messages-container">
      <div class="system-message">
        🎉 欢迎开始咨询，您可以与咨询师交流咨询价格等事宜
      </div>
      
      <div 
        v-for="message in messages" 
        :key="message.id"
        :class="['message-item', message.senderType === 'USER' ? 'user' : 'counselor']"
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
        class="message-input"
        placeholder="输入消息..."
        @keyup.enter="sendMessage"
      />
      <button class="send-btn" :disabled="isSending" @click="sendMessage">
        <span v-if="isSending">发送中...</span>
        <span v-else>发送</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #FFF5F0 0%, #FFE4D4 30%, #FFDCC9 60%, #FFF0EB 100%);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: white;
  box-shadow: 0 2px 15px rgba(229, 115, 115, 0.08);
}

.back-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #E57373;
  font-weight: 500;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-icon {
  font-size: 20px;
}

.chat-title span:last-child {
  font-size: 18px;
  font-weight: 600;
  color: #E57373;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.system-message {
  text-align: center;
  padding: 12px 20px;
  background: white;
  border-radius: 20px;
  font-size: 13px;
  color: #666;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(229, 115, 115, 0.08);
}

.message-item {
  display: flex;
  flex-direction: column;
  max-width: 75%;
  margin-bottom: 15px;
}

.message-item.user {
  align-items: flex-end;
  margin-left: auto;
}

.message-item.counselor {
  align-items: flex-start;
}

.message-content {
  padding: 14px 18px;
  border-radius: 20px;
  word-break: break-word;
}

.message-item.user .message-content {
  background: linear-gradient(135deg, #FFAB91 0%, #FF8A65 100%);
  border-radius: 20px 20px 5px 20px;
  box-shadow: 0 4px 15px rgba(255, 171, 145, 0.3);
}

.message-item.counselor .message-content {
  background: white;
  border-radius: 20px 20px 20px 5px;
  box-shadow: 0 4px 15px rgba(229, 115, 115, 0.08);
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
}

.message-item.user .message-text {
  color: white;
}

.message-item.counselor .message-text {
  color: #5D4037;
}

.message-time {
  font-size: 11px;
  color: #BDBDBD;
  margin-top: 6px;
}

.message-item.user .message-time {
  text-align: right;
}

.message-item.counselor .message-time {
  text-align: left;
}

.input-area {
  display: flex;
  gap: 12px;
  padding: 15px 20px;
  background: white;
  box-shadow: 0 -4px 15px rgba(229, 115, 115, 0.08);
  padding-bottom: calc(15px + env(safe-area-inset-bottom));
}

.message-input {
  flex: 1;
  padding: 15px 20px;
  border: 2px solid #FFE4D4;
  border-radius: 30px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
  background: #FFF5F0;
}

.message-input:focus {
  border-color: #FFAB91;
  background: white;
}

.message-input::placeholder {
  color: #BDBDBD;
}

.send-btn {
  padding: 15px 28px;
  background: linear-gradient(135deg, #FFAB91 0%, #FF8A65 100%);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 171, 145, 0.3);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 171, 145, 0.4);
}

.send-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>