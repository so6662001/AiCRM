const BASE_URL = 'http://localhost:8080'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  params?: Record<string, any>
}

interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}

function buildQuery(params?: Record<string, any>): string {
  if (!params) return ''
  const parts = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
  return parts.length ? '?' + parts.join('&') : ''
}

export function request<T = any>(options: RequestOptions): Promise<T> {
  const tenantId = uni.getStorageSync('tenantId') || '1'
  const userId = uni.getStorageSync('userId') || '100'
  const url = BASE_URL + options.url + buildQuery(options.params)

  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'X-Tenant-Id': tenantId,
        'X-User-Id': userId,
      },
      success: (res: any) => {
        const body = res.data as ApiResult<T>
        if (body.code === 200) {
          resolve(body.data)
        } else {
          uni.showToast({ title: body.message || '请求失败', icon: 'none' })
          reject(new Error(body.message))
        }
      },
      fail: (err: any) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      },
    })
  })
}

export const api = {
  get: <T = any>(url: string, params?: Record<string, any>) =>
    request<T>({ url, method: 'GET', params }),
  post: <T = any>(url: string, data?: any) =>
    request<T>({ url, method: 'POST', data }),
  put: <T = any>(url: string, data?: any) =>
    request<T>({ url, method: 'PUT', data }),
  del: <T = any>(url: string) =>
    request<T>({ url, method: 'DELETE' }),
}
