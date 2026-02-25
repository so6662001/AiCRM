import axios from 'axios';
import { ElMessage } from 'element-plus';
const http = axios.create({ baseURL: '/v1', timeout: 15000 });
http.interceptors.request.use((config) => {
    config.headers['X-Tenant-Id'] = localStorage.getItem('tenantId') || '1';
    config.headers['X-User-Id'] = localStorage.getItem('userId') || '100';
    return config;
});
http.interceptors.response.use((res) => {
    const body = res.data;
    if (body.code === 200)
        return body.data;
    ElMessage.error(body.message || '请求失败');
    return Promise.reject(new Error(body.message));
}, (err) => {
    ElMessage.error(err.message || '网络异常');
    return Promise.reject(err);
});
export default http;
