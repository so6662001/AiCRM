import http from './request';
export const leadApi = {
    page: (p) => http.get('/leads', { params: p }),
    get: (id) => http.get(`/leads/${id}`),
    create: (d) => http.post('/leads', d),
    update: (id, d) => http.put(`/leads/${id}`, d),
    delete: (id) => http.delete(`/leads/${id}`),
    assign: (d) => http.post('/leads/assign', d),
    returnToPool: (id, d) => http.post(`/leads/${id}/return`, d),
};
export const customerApi = {
    page: (p) => http.get('/customers', { params: p }),
    get: (id) => http.get(`/customers/${id}`),
    create: (d) => http.post('/customers', d),
    update: (id, d) => http.put(`/customers/${id}`, d),
    delete: (id) => http.delete(`/customers/${id}`),
    transfer: (d) => http.post('/customers/transfer', d),
    markInvalid: (id, d) => http.post(`/customers/${id}/mark-invalid`, d),
    reactivate: (id) => http.post(`/customers/${id}/reactivate`),
};
export const opportunityApi = {
    page: (p) => http.get('/opportunities', { params: p }),
    get: (id) => http.get(`/opportunities/${id}`),
    create: (d) => http.post('/opportunities', d),
    update: (id, d) => http.put(`/opportunities/${id}`, d),
    delete: (id) => http.delete(`/opportunities/${id}`),
    changeStage: (id, d) => http.put(`/opportunities/${id}/stage`, d),
    win: (id, d) => http.post(`/opportunities/${id}/win`, d),
    lose: (id, d) => http.post(`/opportunities/${id}/lose`, d),
};
export const followUpApi = {
    page: (p) => http.get('/follow-ups', { params: p }),
    get: (id) => http.get(`/follow-ups/${id}`),
    create: (d) => http.post('/follow-ups', d),
    delete: (id) => http.delete(`/follow-ups/${id}`),
};
export const visitApi = {
    page: (p) => http.get('/visits', { params: p }),
    get: (id) => http.get(`/visits/${id}`),
    create: (d) => http.post('/visits', d),
    complete: (id, d) => http.put(`/visits/${id}/complete`, d),
    cancel: (id) => http.delete(`/visits/${id}`),
};
export const taskApi = {
    page: (p) => http.get('/tasks', { params: p }),
    get: (id) => http.get(`/tasks/${id}`),
    create: (d) => http.post('/tasks', d),
    update: (id, d) => http.put(`/tasks/${id}`, d),
    complete: (id, d) => http.put(`/tasks/${id}/complete`, d),
    todaySummary: () => http.get('/tasks/today/summary'),
};
export const activityApi = {
    page: (p) => http.get('/activities', { params: p }),
    get: (id) => http.get(`/activities/${id}`),
    create: (d) => http.post('/activities', d),
    update: (id, d) => http.put(`/activities/${id}`, d),
    publish: (id) => http.put(`/activities/${id}/publish`),
    cancel: (id) => http.put(`/activities/${id}/cancel`),
    end: (id) => http.put(`/activities/${id}/end`),
    statistics: (id) => http.get(`/activities/${id}/statistics`),
    participants: (id, p) => http.get(`/activities/${id}/participants`, { params: p }),
    addParticipant: (id, d) => http.post(`/activities/${id}/participants`, d),
    checkinParticipant: (aid, pid) => http.post(`/activities/${aid}/participants/${pid}/checkin`),
    approveParticipant: (aid, pid) => http.post(`/activities/${aid}/participants/${pid}/approve`),
    rejectParticipant: (aid, pid, d) => http.post(`/activities/${aid}/participants/${pid}/reject`, d),
};
export const friendApi = {
    page: (p) => http.get('/friends', { params: p }),
    get: (id) => http.get(`/friends/${id}`),
    create: (d) => http.post('/friends', d),
    update: (id, d) => http.put(`/friends/${id}`, d),
    delete: (id) => http.delete(`/friends/${id}`),
    linkCustomer: (id, d) => http.post(`/friends/${id}/link-customer`, d),
    convertToLead: (id) => http.post(`/friends/${id}/convert-to-lead`),
    statistics: () => http.get('/friends/statistics'),
};
export const checkinApi = {
    page: (p) => http.get('/checkins', { params: p }),
    today: () => http.get('/checkins/today'),
};
export const orgApi = {
    tree: () => http.get('/organizations/tree'),
};
export const tenantApi = {
    list: () => http.get('/admin/tenants'),
};
export const blacklistApi = {
    page: (p) => http.get('/customers/blacklist', { params: p }),
    add: (d) => http.post('/customers/blacklist', d),
    release: (id, d) => http.post(`/customers/blacklist/${id}/release`, d),
    check: (p) => http.get('/customers/blacklist/check', { params: p }),
};
export const enterpriseApi = {
    search: (keyword) => http.get('/enterprise/search', { params: { keyword } }),
    queryContacts: (creditCode) => http.get(`/enterprise/${creditCode}/contacts`),
    importContact: (creditCode, contactId, d) => http.post(`/enterprise/${creditCode}/contacts/${contactId}/import`, d),
    getQuota: () => http.get('/enterprise/contact-quota'),
};
export const reportApi = {
    personal: (period) => http.get('/reports/sales/personal', { params: { period } }),
    team: (period) => http.get('/reports/sales/team', { params: { period } }),
};
export const erpFinanceApi = {
    receivables: (customerId) => http.get(`/customers/${customerId}/erp-receivables`),
    transactions: (customerId, p) => http.get(`/customers/${customerId}/erp-transactions`, { params: p }),
};
export const wechatWorkApi = {
    getConfig: () => http.get('/wechat-work/config'),
    saveConfig: (d) => http.post('/wechat-work/config', d),
    updateConfig: (id, d) => http.put(`/wechat-work/config/${id}`, d),
    testConnection: (id) => http.post(`/wechat-work/config/${id}/test`),
};
