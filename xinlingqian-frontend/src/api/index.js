import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
});

const uploadApi = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

uploadApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

export const userApi = {
  login: (data) => api.post('/user/login', data),
  register: (data) => api.post('/user/register', data),
  sendCode: (data) => api.post('/user/send-code', data),
  resetPassword: (data) => api.post('/user/reset-password', data),
  getUserInfo: () => api.get('/user/info'),
  updateUser: (data) => api.post('/user/update', data),
  recharge: (amount) => api.post('/user/recharge', null, { params: { amount } }),
  checkVersion: () => api.get('/user/version'),
};

export const membershipApi = {
  subscribe: (paymentType) => api.post('/membership/subscribe', null, { params: { paymentType } }),
  checkMemberStatus: () => api.get('/membership/status'),
};

export const moodTestApi = {
  getRegisterQuestions: () => api.get('/mood/register-questions'),
  submitRegisterAnswers: (data) => api.post('/mood/submit-register', data),
  getDailyQuestions: () => api.get('/mood/daily-questions'),
  submitDailyAnswers: (data) => api.post('/mood/submit-daily', data),
  getTodayRecord: () => api.get('/mood/today-record'),
  checkBaselineStatus: () => api.get('/mood/baseline-status'),
  getBaselineRecord: () => api.get('/mood/baseline-record'),
  getHistoryRecords: (days = 7) => api.get(`/mood/history-records?days=${days}`),
  getRecordByDate: (date) => api.get(`/mood/record-by-date?date=${date}`),
  getHistoryRecord: (userId, date) => api.get(`/mood/history-record?userId=${userId}&date=${date}`),
};

export const communityApi = {
  getPosts: () => api.get('/community/posts'),
  createPost: (data) => api.post('/community/posts', data),
  deletePost: (postId) => api.delete(`/community/posts/${postId}`),
  likePost: (postId) => api.post(`/community/posts/${postId}/like`),
  getComments: (postId) => api.get(`/community/posts/${postId}/comments`),
  addComment: (postId, data) => api.post(`/community/posts/${postId}/comments`, data),
};

export const fileApi = {
  upload: (formData) => uploadApi.post('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }),
};

export const counselorApi = {
  getCounselors: (keyword) => api.get('/counselor/list' + (keyword ? `?keyword=${keyword}` : '')),
  getRandomCounselor: () => api.get('/counselor/random'),
  getCounselor: (id) => api.get(`/counselor/${id}`),
  getMyCounselorInfo: () => api.get('/counselor/me'),
  getMyStats: () => api.get('/counselor/me/stats'),
  getMyReviews: () => api.get('/counselor/me/reviews'),
  getMyProfile: () => api.get('/counselor/profile'),
  updateProfile: (data) => api.post('/counselor/profile', data),
  applyCounselor: (data) => api.post('/counselor/apply', data),
  updateCounselor: (id, data) => api.put(`/counselor/${id}`, data),
  approveCounselor: (id) => api.post(`/counselor/${id}/approve`),
  rejectCounselor: (id) => api.post(`/counselor/${id}/reject`),
  getApplications: () => api.get('/counselor/applications'),
};

export const consultationApi = {
  createConsultation: (counselorId) => api.post(`/consultation/create?counselorId=${counselorId}`),
  getConsultation: (id) => api.get(`/consultation/${id}`),
  getUserConsultations: () => api.get('/consultation/user/list'),
  getCounselorConsultations: () => api.get('/consultation/counselor/list'),
  startConsultation: (id) => api.post(`/consultation/${id}/start`),
  completeConsultation: (id) => api.post(`/consultation/${id}/complete`),
  sendMessage: (id, data) => api.post(`/consultation/${id}/message`, data),
  getMessages: (id) => api.get(`/consultation/${id}/messages`),
  createPayment: (data) => api.post('/consultation/payment', data),
  completePayment: (id) => api.post(`/consultation/payment/${id}/complete`),
  getConsultationPayments: (id) => api.get(`/consultation/payment/${id}`),
  getCounselorPayments: () => api.get('/consultation/counselor/payments'),
};

export const reviewApi = {
  addReview: (data) => api.post('/review/add', data),
  getReviews: (counselorId) => api.get(`/review/list/${counselorId}`),
  checkReviewStatus: (counselorId) => api.get(`/review/check/${counselorId}`),
  getRatingInfo: (counselorId) => api.get(`/review/rating/${counselorId}`),
};

export const levelApi = {
  getPendingRequests: () => api.get('/level/pending'),
  getAllRequests: () => api.get('/level/all'),
  approveRequest: (requestId, data) => api.post(`/level/approve/${requestId}`, data),
  rejectRequest: (requestId, data) => api.post(`/level/reject/${requestId}`, data),
  checkLevelUp: (counselorId) => api.get(`/level/check/${counselorId}`),
  getLevelInfo: (counselorId) => api.get(`/level/info/${counselorId}`),
};

export const adminApi = {
  searchUsers: (username) => api.get('/admin/users' + (username ? `?username=${username}` : '')),
  updateBalance: (userId, amount) => api.post(`/admin/users/${userId}/balance`, { amount }),
  deleteUser: (userId) => api.delete(`/admin/users/${userId}`),
  getPendingApplications: () => api.get('/admin/counselors/applications'),
  getApprovedCounselors: () => api.get('/admin/counselors/approved'),
  approveCounselor: (counselorId) => api.post(`/admin/counselors/${counselorId}/approve`),
  rejectCounselor: (counselorId) => api.post(`/admin/counselors/${counselorId}/reject`),
  deleteCounselor: (counselorId) => api.delete(`/admin/counselors/${counselorId}`),
  getAllPosts: () => api.get('/admin/posts'),
  deletePost: (postId) => api.delete(`/admin/posts/${postId}`),
  getPendingLevelUpRequests: () => api.get('/admin/level-up/pending'),
  approveLevelUp: (requestId) => api.post(`/admin/level-up/${requestId}/approve`),
  rejectLevelUp: (requestId) => api.post(`/admin/level-up/${requestId}/reject`),
  getAllReviews: () => api.get('/admin/reviews'),
  deleteReview: (reviewId) => api.delete(`/admin/reviews/${reviewId}`),
  getUnrepliedMessages: () => api.get('/admin/messages'),
  replyMessage: (messageId, content) => api.post(`/admin/messages/${messageId}/reply`, { content }),
  setMember: (userId, isMember) => api.post(`/admin/users/${userId}/member`, { isMember }),
};

export const customerServiceApi = {
  sendMessage: (content) => api.post('/customer-service/send', { content, senderType: 'USER' }),
  getHistory: () => api.get('/customer-service/history'),
};

export const adminCustomerServiceApi = {
  getConversations: () => api.get('/customer-service/admin/conversations'),
  getHistory: (userId) => api.get(`/customer-service/admin/history/${userId}`),
  reply: (userId, content) => api.post('/customer-service/admin/reply', { userId, content }),
};

export default api;
