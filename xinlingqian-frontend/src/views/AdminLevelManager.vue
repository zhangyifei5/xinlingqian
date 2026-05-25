<script setup>import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { levelApi, counselorApi } from '../api';
import BackButton from '../components/BackButton.vue';
const router = useRouter();
const requests = ref([]);
const isLoading = ref(true);
const showModal = ref(false);
const currentRequest = ref(null);
const adminComment = ref('');
const actionType = ref('');
const counselors = ref({});
const loadRequests = async () => {
 isLoading.value = true;
 try {
 const response = await levelApi.getPendingRequests();
 if (response.data.code === 200) {
 requests.value = response.data.data;
 await loadCounselorNames();
 }
 }
 catch (error) {
 console.error('获取升级请求失败:', error);
 }
 finally {
 isLoading.value = false;
 }
};
const loadCounselorNames = async () => {
 for (const request of requests.value) {
 if (!counselors.value[request.counselorId]) {
 try {
 const response = await counselorApi.getCounselor(request.counselorId);
 if (response.data.code === 200) {
 counselors.value[request.counselorId] = response.data.data.name;
 }
 }
 catch (error) {
 counselors.value[request.counselorId] = '未知咨询师';
 }
 }
 }
};
const openModal = (request, type) => {
 currentRequest.value = request;
 actionType.value = type;
 adminComment.value = '';
 showModal.value = true;
};
const closeModal = () => {
 showModal.value = false;
 currentRequest.value = null;
 adminComment.value = '';
};
const confirmAction = async () => {
 if (!currentRequest.value)
 return;
 try {
 let response;
 if (actionType.value === 'approve') {
 response = await levelApi.approveRequest(currentRequest.value.id, { comment: adminComment.value });
 }
 else {
 response = await levelApi.rejectRequest(currentRequest.value.id, { comment: adminComment.value });
 }
 if (response.data.code === 200) {
 ElMessage.success(actionType.value === 'approve' ? '升级成功！' : '已拒绝申请');
 loadRequests();
 closeModal();
 }
 }
 catch (error) {
 console.error('操作失败:', error);
 ElMessage.error(error.response?.data?.message || '操作失败');
 }
};
const getStatusText = (status) => {
 const statusMap = {
 'PENDING': '待审核',
 'APPROVED': '已通过',
 'REJECTED': '已拒绝'
 };
 return statusMap[status] || status;
};
const getStatusColor = (status) => {
 const colorMap = {
 'PENDING': '#FFB74D',
 'APPROVED': '#66BB6A',
 'REJECTED': '#EF5350'
 };
 return colorMap[status] || '#9E9E9E';
};
onMounted(() => {
 loadRequests();
});
</script>

<template>
  <div class="admin-level-manager">
    <div class="page-header">
      <BackButton />
      <h1>🏆 咨询师等级管理</h1>
    </div>

    <div v-if="isLoading" class="loading-container">
      <div class="loading-animation">
        <span class="loading-dot">●</span>
        <span class="loading-dot">●</span>
        <span class="loading-dot">●</span>
      </div>
      <p>加载中...</p>
    </div>

    <div v-else class="content">
      <div v-if="requests.length === 0" class="empty-state">
        <div class="empty-icon">🎉</div>
        <p>暂无待审核的升级请求</p>
      </div>

      <div v-else class="requests-list">
        <div 
          v-for="request in requests" 
          :key="request.id" 
          class="request-card"
        >
          <div class="request-header">
            <div class="counselor-info">
              <span class="counselor-name">👤 {{ counselors[request.counselorId] || '未知咨询师' }}</span>
              <span 
                class="status-badge" 
                :style="{ background: getStatusColor(request.status) }"
              >
                {{ getStatusText(request.status) }}
              </span>
            </div>
          </div>

          <div class="request-details">
            <div class="level-info">
              <span class="level-change">
                <span class="level-badge-old">Lv.{{ request.currentLevel }}</span>
                <span class="arrow">→</span>
                <span class="level-badge-new">Lv.{{ request.targetLevel }}</span>
              </span>
            </div>

            <div class="stats-row">
              <div class="stat">
                <span class="stat-icon">⭐</span>
                <span class="stat-value">{{ request.excellentReviewCount }} 条好评</span>
              </div>
            </div>

            <div class="request-time">
              📅 {{ request.createTime }}
            </div>
          </div>

          <div class="request-actions" v-if="request.status === 'PENDING'">
            <button 
              class="action-btn approve-btn"
              @click="openModal(request, 'approve')"
            >
              ✅ 通过升级
            </button>
            <button 
              class="action-btn reject-btn"
              @click="openModal(request, 'reject')"
            >
              ❌ 拒绝申请
            </button>
          </div>

          <div v-if="request.adminComment" class="admin-comment">
            <span class="comment-label">管理员备注：</span>
            <span class="comment-text">{{ request.adminComment }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ actionType === 'approve' ? '✅ 通过升级申请' : '❌ 拒绝升级申请' }}</h3>
          <span class="modal-close" @click="closeModal">×</span>
        </div>
        <div class="modal-body">
          <div class="modal-info">
            <p><strong>咨询师：</strong>{{ counselors[currentRequest?.counselorId] || '未知' }}</p>
            <p><strong>等级变化：</strong>Lv.{{ currentRequest?.currentLevel }} → Lv.{{ currentRequest?.targetLevel }}</p>
            <p><strong>好评数量：</strong>{{ currentRequest?.excellentReviewCount }} 条</p>
          </div>
          <div class="comment-section">
            <label class="comment-label">备注（选填）</label>
            <textarea 
              v-model="adminComment" 
              class="comment-input" 
              placeholder="请输入备注信息..."
              maxlength="200"
            ></textarea>
            <span class="comment-count">{{ adminComment.length }}/200</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button 
            class="btn-confirm"
            :class="{ approve: actionType === 'approve', reject: actionType === 'reject' }"
            @click="confirmAction"
          >
            {{ actionType === 'approve' ? '确认通过' : '确认拒绝' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-level-manager {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF5F0 0%, #FFE4D4 30%, #FFDCC9 60%, #FFF0EB 100%);
  padding-bottom: 60px;
}

.page-header {
  padding: 20px;
  padding-top: 30px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.back-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}

.page-header h1 {
  font-size: 24px;
  color: #E57373;
  margin: 0;
}

.loading-container {
  text-align: center;
  padding: 100px 20px;
}

.loading-animation {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 15px;
}

.loading-dot {
  font-size: 24px;
  color: #FF8A65;
  animation: dotBounce 1.4s ease-in-out infinite;
}

.loading-dot:nth-child(2) { animation-delay: 0.2s; }
.loading-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.content {
  padding: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state p {
  color: #BDBDBD;
  font-size: 16px;
}

.requests-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.request-card {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(229, 115, 115, 0.08);
}

.request-header {
  margin-bottom: 15px;
}

.counselor-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.counselor-name {
  font-size: 18px;
  font-weight: 600;
  color: #E57373;
}

.status-badge {
  padding: 6px 15px;
  border-radius: 20px;
  font-size: 13px;
  color: white;
  font-weight: 500;
}

.request-details {
  background: #FFF8F5;
  border-radius: 15px;
  padding: 15px;
  margin-bottom: 15px;
}

.level-info {
  margin-bottom: 12px;
}

.level-change {
  display: flex;
  align-items: center;
  gap: 15px;
}

.level-badge-old,
.level-badge-new {
  padding: 8px 16px;
  border-radius: 15px;
  font-weight: 600;
  font-size: 16px;
}

.level-badge-old {
  background: #E0E0E0;
  color: #666;
}

.level-badge-new {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 215, 0, 0.3);
}

.arrow {
  font-size: 20px;
  color: #FF8A65;
}

.stats-row {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 5px;
}

.stat-icon {
  font-size: 18px;
}

.stat-value {
  font-size: 14px;
  color: #666;
}

.request-time {
  font-size: 12px;
  color: #BDBDBD;
}

.request-actions {
  display: flex;
  gap: 15px;
}

.action-btn {
  flex: 1;
  padding: 14px;
  border: none;
  border-radius: 15px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.approve-btn {
  background: linear-gradient(135deg, #66BB6A 0%, #4CAF50 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3);
}

.approve-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4);
}

.reject-btn {
  background: linear-gradient(135deg, #EF5350 0%, #E53935 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(239, 83, 80, 0.3);
}

.reject-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(239, 83, 80, 0.4);
}

.admin-comment {
  margin-top: 15px;
  padding: 12px;
  background: #FFF3E0;
  border-radius: 10px;
}

.comment-label {
  font-size: 13px;
  color: #FF8A65;
  font-weight: 500;
}

.comment-text {
  font-size: 14px;
  color: #666;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  max-width: 500px;
  background: white;
  border-radius: 25px 25px 0 0;
  padding: 25px;
  padding-bottom: calc(25px + env(safe-area-inset-bottom));
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h3 {
  font-size: 20px;
  color: #E57373;
  margin: 0;
}

.modal-close {
  font-size: 30px;
  color: #BDBDBD;
  cursor: pointer;
}

.modal-body {
  padding: 10px 0;
}

.modal-info {
  background: #FFF8F5;
  border-radius: 15px;
  padding: 15px;
  margin-bottom: 20px;
}

.modal-info p {
  margin: 8px 0;
  font-size: 14px;
  color: #666;
}

.comment-section {
  margin-top: 10px;
}

.comment-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  display: block;
}

.comment-input {
  width: 100%;
  height: 100px;
  padding: 15px;
  border: 2px solid #FFE4D4;
  border-radius: 15px;
  font-size: 14px;
  resize: none;
  box-sizing: border-box;
}

.comment-input:focus {
  outline: none;
  border-color: #FF8A65;
}

.comment-count {
  font-size: 12px;
  color: #BDBDBD;
  float: right;
  margin-top: 5px;
}

.modal-footer {
  display: flex;
  gap: 15px;
  margin-top: 25px;
}

.btn-cancel {
  flex: 1;
  padding: 15px;
  background: #F5F5F5;
  border: none;
  border-radius: 15px;
  font-size: 16px;
  color: #666;
  cursor: pointer;
}

.btn-confirm {
  flex: 2;
  padding: 15px;
  border: none;
  border-radius: 15px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  cursor: pointer;
}

.btn-confirm.approve {
  background: linear-gradient(135deg, #66BB6A 0%, #4CAF50 100%);
}

.btn-confirm.reject {
  background: linear-gradient(135deg, #EF5350 0%, #E53935 100%);
}
</style>