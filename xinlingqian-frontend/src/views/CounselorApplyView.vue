<script setup>import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { counselorApi } from '../api';
import BackButton from '../components/BackButton.vue';
const router = useRouter();
const form = ref({
 name: '',
 title: '',
 specialty: '',
 experience: '',
 education: '',
 certificate: '',
 description: '',
 hourlyRate: 0
});
const isSubmitting = ref(false);
const submitApplication = async () => {
 if (!form.value.name.trim()) {
 ElMessage.warning('请输入姓名');
 return;
 }
 if (!form.value.title.trim()) {
 ElMessage.warning('请输入头衔');
 return;
 }
 if (!form.value.specialty.trim()) {
 ElMessage.warning('请输入专长领域');
 return;
 }
 if (!form.value.experience.trim()) {
 ElMessage.warning('请输入从业经历');
 return;
 }
 if (!form.value.education.trim()) {
 ElMessage.warning('请输入学历背景');
 return;
 }
 if (!form.value.certificate.trim()) {
 ElMessage.warning('请输入资质证书');
 return;
 }
 if (!form.value.description.trim()) {
 ElMessage.warning('请输入个人简介');
 return;
 }
 if (!form.value.hourlyRate || form.value.hourlyRate <= 0) {
 ElMessage.warning('请输入合理的小时费率');
 return;
 }
 isSubmitting.value = true;
 try {
 const response = await counselorApi.applyCounselor(form.value);
 if (response.data.code === 200) {
 ElMessage.success('申请提交成功！等待管理员审核');
 setTimeout(() => {
 router.push('/counselor');
 }, 2000);
 }
 }
 catch (error) {
 console.error('申请失败:', error);
 ElMessage.error(error.response?.data?.message || '申请失败，请稍后重试');
 }
 finally {
 isSubmitting.value = false;
 }
};
</script>

<template>
  <div class="apply-page">
    <div class="page-header">
      <BackButton />
      <h1>💡 成为心理咨询师</h1>
      <p>填写以下信息，等待审核通过后即可开始服务</p>
    </div>

    <div class="form-container">
      <div class="form-section">
        <label class="form-label">
          <span class="label-icon">👤</span>
          <span>姓名 *</span>
        </label>
        <input 
          v-model="form.name" 
          type="text" 
          class="form-input"
          placeholder="请输入您的姓名"
        />
      </div>

      <div class="form-section">
        <label class="form-label">
          <span class="label-icon">🎖️</span>
          <span>头衔 *</span>
        </label>
        <input 
          v-model="form.title" 
          type="text" 
          class="form-input"
          placeholder="如：高级心理咨询师、心理治疗师等"
        />
      </div>

      <div class="form-section">
        <label class="form-label">
          <span class="label-icon">🏷️</span>
          <span>专长领域 *</span>
        </label>
        <input 
          v-model="form.specialty" 
          type="text" 
          class="form-input"
          placeholder="如：焦虑症、抑郁症、人际关系等"
        />
      </div>

      <div class="form-section">
        <label class="form-label">
          <span class="label-icon">📚</span>
          <span>从业经历 *</span>
        </label>
        <textarea 
          v-model="form.experience" 
          class="form-textarea"
          rows="3"
          placeholder="请描述您的从业经历和相关经验"
        ></textarea>
      </div>

      <div class="form-section">
        <label class="form-label">
          <span class="label-icon">🎓</span>
          <span>学历背景 *</span>
        </label>
        <input 
          v-model="form.education" 
          type="text" 
          class="form-input"
          placeholder="如：心理学硕士、应用心理学博士等"
        />
      </div>

      <div class="form-section">
        <label class="form-label">
          <span class="label-icon">📜</span>
          <span>资质证书 *</span>
        </label>
        <textarea 
          v-model="form.certificate" 
          class="form-textarea"
          rows="3"
          placeholder="请列出您的专业资质证书"
        ></textarea>
      </div>

      <div class="form-section">
        <label class="form-label">
          <span class="label-icon">💬</span>
          <span>个人简介 *</span>
        </label>
        <textarea 
          v-model="form.description" 
          class="form-textarea"
          rows="4"
          placeholder="请介绍一下您自己和您的咨询理念"
        ></textarea>
      </div>

      <div class="form-section">
        <label class="form-label">
          <span class="label-icon">💰</span>
          <span>小时费率（元）*</span>
        </label>
        <div class="rate-input-wrapper">
          <span class="rate-prefix">¥</span>
          <input 
            v-model.number="form.hourlyRate" 
            type="number" 
            class="form-input rate-input"
            placeholder="请输入咨询费用"
          />
          <span class="rate-suffix">/小时</span>
        </div>
      </div>
    </div>

    <button class="submit-btn" :disabled="isSubmitting" @click="submitApplication">
      <span v-if="isSubmitting">提交中...</span>
      <span v-else>提交申请</span>
    </button>
  </div>
</template>

<style scoped>
.apply-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF5F0 0%, #FFE4D4 30%, #FFDCC9 60%, #FFF0EB 100%);
  padding: 20px;
  padding-bottom: 120px;
}

.page-header {
  padding: 20px 0;
}

.back-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  margin-bottom: 20px;
  color: #E57373;
  font-weight: 500;
}

.page-header h1 {
  font-size: 28px;
  color: #E57373;
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 14px;
  color: #FFAB91;
  margin: 0;
}

.form-container {
  padding: 10px;
}

.form-section {
  margin-bottom: 20px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #E57373;
  margin-bottom: 8px;
}

.label-icon {
  font-size: 16px;
}

.form-input {
  width: 100%;
  padding: 15px 18px;
  border: 2px solid #FFE4D4;
  border-radius: 15px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
  background: white;
}

.form-input:focus {
  border-color: #FFAB91;
  box-shadow: 0 0 0 3px rgba(255, 171, 145, 0.1);
}

.form-input::placeholder {
  color: #BDBDBD;
}

.form-textarea {
  width: 100%;
  padding: 15px 18px;
  border: 2px solid #FFE4D4;
  border-radius: 15px;
  font-size: 14px;
  outline: none;
  resize: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
  background: white;
}

.form-textarea:focus {
  border-color: #FFAB91;
  box-shadow: 0 0 0 3px rgba(255, 171, 145, 0.1);
}

.form-textarea::placeholder {
  color: #BDBDBD;
}

.rate-input-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rate-prefix {
  font-size: 18px;
  color: #E57373;
  font-weight: 600;
}

.rate-input {
  flex: 1;
  max-width: 200px;
}

.rate-suffix {
  font-size: 14px;
  color: #FFAB91;
}

.submit-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  margin: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #FFAB91 0%, #FF8A65 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(255, 171, 145, 0.4);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(255, 171, 145, 0.5);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>