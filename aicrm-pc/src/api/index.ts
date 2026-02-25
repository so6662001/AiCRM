import http from './request'

export const leadApi = {
  page: (p: any) => http.get('/leads', { params: p }),
  get: (id: number) => http.get(`/leads/${id}`),
  create: (d: any) => http.post('/leads', d),
  update: (id: number, d: any) => http.put(`/leads/${id}`, d),
  delete: (id: number) => http.delete(`/leads/${id}`),
  assign: (d: any) => http.post('/leads/assign', d),
  returnToPool: (id: number, d: any) => http.post(`/leads/${id}/return`, d),
}

export const customerApi = {
  page: (p: any) => http.get('/customers', { params: p }),
  get: (id: number) => http.get(`/customers/${id}`),
  create: (d: any) => http.post('/customers', d),
  update: (id: number, d: any) => http.put(`/customers/${id}`, d),
  delete: (id: number) => http.delete(`/customers/${id}`),
  transfer: (d: any) => http.post('/customers/transfer', d),
  markInvalid: (id: number, d: any) => http.post(`/customers/${id}/mark-invalid`, d),
  reactivate: (id: number) => http.post(`/customers/${id}/reactivate`),
}

export const opportunityApi = {
  page: (p: any) => http.get('/opportunities', { params: p }),
  get: (id: number) => http.get(`/opportunities/${id}`),
  create: (d: any) => http.post('/opportunities', d),
  update: (id: number, d: any) => http.put(`/opportunities/${id}`, d),
  delete: (id: number) => http.delete(`/opportunities/${id}`),
  changeStage: (id: number, d: any) => http.put(`/opportunities/${id}/stage`, d),
  win: (id: number, d: any) => http.post(`/opportunities/${id}/win`, d),
  lose: (id: number, d: any) => http.post(`/opportunities/${id}/lose`, d),
}

export const followUpApi = {
  page: (p: any) => http.get('/follow-ups', { params: p }),
  get: (id: number) => http.get(`/follow-ups/${id}`),
  create: (d: any) => http.post('/follow-ups', d),
  delete: (id: number) => http.delete(`/follow-ups/${id}`),
}

export const visitApi = {
  page: (p: any) => http.get('/visits', { params: p }),
  get: (id: number) => http.get(`/visits/${id}`),
  create: (d: any) => http.post('/visits', d),
  complete: (id: number, d: any) => http.put(`/visits/${id}/complete`, d),
  cancel: (id: number) => http.delete(`/visits/${id}`),
}

export const taskApi = {
  page: (p: any) => http.get('/tasks', { params: p }),
  get: (id: number) => http.get(`/tasks/${id}`),
  create: (d: any) => http.post('/tasks', d),
  update: (id: number, d: any) => http.put(`/tasks/${id}`, d),
  complete: (id: number, d: any) => http.put(`/tasks/${id}/complete`, d),
  todaySummary: () => http.get('/tasks/today/summary'),
}

export const activityApi = {
  page: (p: any) => http.get('/activities', { params: p }),
  get: (id: number) => http.get(`/activities/${id}`),
  create: (d: any) => http.post('/activities', d),
  update: (id: number, d: any) => http.put(`/activities/${id}`, d),
  publish: (id: number) => http.put(`/activities/${id}/publish`),
  cancel: (id: number) => http.put(`/activities/${id}/cancel`),
  end: (id: number) => http.put(`/activities/${id}/end`),
  statistics: (id: number) => http.get(`/activities/${id}/statistics`),
  participants: (id: number, p?: any) => http.get(`/activities/${id}/participants`, { params: p }),
  addParticipant: (id: number, d: any) => http.post(`/activities/${id}/participants`, d),
  checkinParticipant: (aid: number, pid: number) => http.post(`/activities/${aid}/participants/${pid}/checkin`),
  approveParticipant: (aid: number, pid: number) => http.post(`/activities/${aid}/participants/${pid}/approve`),
  rejectParticipant: (aid: number, pid: number, d: any) => http.post(`/activities/${aid}/participants/${pid}/reject`, d),
}

export const friendApi = {
  page: (p: any) => http.get('/friends', { params: p }),
  get: (id: number) => http.get(`/friends/${id}`),
  create: (d: any) => http.post('/friends', d),
  update: (id: number, d: any) => http.put(`/friends/${id}`, d),
  delete: (id: number) => http.delete(`/friends/${id}`),
  linkCustomer: (id: number, d: any) => http.post(`/friends/${id}/link-customer`, d),
  convertToLead: (id: number) => http.post(`/friends/${id}/convert-to-lead`),
  statistics: () => http.get('/friends/statistics'),
}

export const checkinApi = {
  page: (p: any) => http.get('/checkins', { params: p }),
  today: () => http.get('/checkins/today'),
}

export const orgApi = {
  tree: () => http.get('/organizations/tree'),
}

export const tenantApi = {
  list: () => http.get('/admin/tenants'),
}
