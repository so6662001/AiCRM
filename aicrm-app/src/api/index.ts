import { api } from './request'

// ========== 线索 ==========
export const leadApi = {
  page: (params: any) => api.get('/v1/leads', params),
  get: (id: number) => api.get(`/v1/leads/${id}`),
  create: (data: any) => api.post('/v1/leads', data),
  update: (id: number, data: any) => api.put(`/v1/leads/${id}`, data),
  delete: (id: number) => api.del(`/v1/leads/${id}`),
  assign: (data: any) => api.post('/v1/leads/assign', data),
  returnToPool: (id: number, data: any) => api.post(`/v1/leads/${id}/return`, data),
}

// ========== 客户 ==========
export const customerApi = {
  page: (params: any) => api.get('/v1/customers', params),
  get: (id: number) => api.get(`/v1/customers/${id}`),
  create: (data: any) => api.post('/v1/customers', data),
  update: (id: number, data: any) => api.put(`/v1/customers/${id}`, data),
  delete: (id: number) => api.del(`/v1/customers/${id}`),
  transfer: (data: any) => api.post('/v1/customers/transfer', data),
  markInvalid: (id: number, data: any) => api.post(`/v1/customers/${id}/mark-invalid`, data),
  reactivate: (id: number) => api.post(`/v1/customers/${id}/reactivate`),
}

// ========== 商机 ==========
export const opportunityApi = {
  page: (params: any) => api.get('/v1/opportunities', params),
  get: (id: number) => api.get(`/v1/opportunities/${id}`),
  create: (data: any) => api.post('/v1/opportunities', data),
  update: (id: number, data: any) => api.put(`/v1/opportunities/${id}`, data),
  delete: (id: number) => api.del(`/v1/opportunities/${id}`),
  changeStage: (id: number, data: any) => api.put(`/v1/opportunities/${id}/stage`, data),
  win: (id: number, data: any) => api.post(`/v1/opportunities/${id}/win`, data),
  lose: (id: number, data: any) => api.post(`/v1/opportunities/${id}/lose`, data),
}

// ========== 跟进 ==========
export const followUpApi = {
  page: (params: any) => api.get('/v1/follow-ups', params),
  get: (id: number) => api.get(`/v1/follow-ups/${id}`),
  create: (data: any) => api.post('/v1/follow-ups', data),
  update: (id: number, data: any) => api.put(`/v1/follow-ups/${id}`, data),
  delete: (id: number) => api.del(`/v1/follow-ups/${id}`),
}

// ========== 拜访 ==========
export const visitApi = {
  page: (params: any) => api.get('/v1/visits', params),
  get: (id: number) => api.get(`/v1/visits/${id}`),
  create: (data: any) => api.post('/v1/visits', data),
  complete: (id: number, data: any) => api.put(`/v1/visits/${id}/complete`, data),
  cancel: (id: number) => api.del(`/v1/visits/${id}`),
}

// ========== 签到 ==========
export const checkinApi = {
  page: (params: any) => api.get('/v1/checkins', params),
  today: () => api.get('/v1/checkins/today'),
  create: (data: any) => api.post('/v1/checkins', data),
}

// ========== 任务 ==========
export const taskApi = {
  page: (params: any) => api.get('/v1/tasks', params),
  get: (id: number) => api.get(`/v1/tasks/${id}`),
  create: (data: any) => api.post('/v1/tasks', data),
  update: (id: number, data: any) => api.put(`/v1/tasks/${id}`, data),
  complete: (id: number, data: any) => api.put(`/v1/tasks/${id}/complete`, data),
  todaySummary: () => api.get('/v1/tasks/today/summary'),
}

// ========== 活动 ==========
export const activityApi = {
  page: (params: any) => api.get('/v1/activities', params),
  get: (id: number) => api.get(`/v1/activities/${id}`),
  create: (data: any) => api.post('/v1/activities', data),
  update: (id: number, data: any) => api.put(`/v1/activities/${id}`, data),
  publish: (id: number) => api.put(`/v1/activities/${id}/publish`),
  cancel: (id: number) => api.put(`/v1/activities/${id}/cancel`),
  statistics: (id: number) => api.get(`/v1/activities/${id}/statistics`),
  participants: (id: number, params?: any) => api.get(`/v1/activities/${id}/participants`, params),
  addParticipant: (id: number, data: any) => api.post(`/v1/activities/${id}/participants`, data),
  checkinParticipant: (actId: number, pid: number) => api.post(`/v1/activities/${actId}/participants/${pid}/checkin`),
}

// ========== 好友 ==========
export const friendApi = {
  page: (params: any) => api.get('/v1/friends', params),
  get: (id: number) => api.get(`/v1/friends/${id}`),
  create: (data: any) => api.post('/v1/friends', data),
  update: (id: number, data: any) => api.put(`/v1/friends/${id}`, data),
  delete: (id: number) => api.del(`/v1/friends/${id}`),
  linkCustomer: (id: number, data: any) => api.post(`/v1/friends/${id}/link-customer`, data),
  convertToLead: (id: number) => api.post(`/v1/friends/${id}/convert-to-lead`),
  statistics: () => api.get('/v1/friends/statistics'),
}

// ========== 轨迹 ==========
export const trajectoryApi = {
  report: (data: any) => api.post('/v1/trajectories/report', data),
  daily: (userId: number, date: string) => api.get(`/v1/trajectories/${userId}/daily`, { date }),
}
