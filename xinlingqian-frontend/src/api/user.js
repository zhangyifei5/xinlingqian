import axios from 'axios'

const baseURL = 'http://localhost:8080/api'

const instance = axios.create({
  baseURL,
  timeout: 10000,
  withCredentials: true
})

export const login = (data) => {
  return instance.post('/user/login', data)
}

export const register = (data) => {
  return instance.post('/user/register', data)
}

export const sendCode = (data) => {
  return instance.post('/user/send-code', data)
}

export const resetPassword = (data) => {
  return instance.post('/user/reset-password', data)
}

export const getCaptcha = () => {
  return instance.get('/captcha/generate')
}

export const verifyCaptcha = (data) => {
  return instance.post('/captcha/verify', data)
}
