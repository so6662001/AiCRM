import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userId = ref<number>(100)
  const userName = ref('李明')
  const tenantId = ref<number>(1)
  const orgName = ref('华南销售团队')
  const roleName = ref('销售员')
  const avatar = ref('')

  function setUser(data: { userId: number; userName: string; tenantId: number }) {
    userId.value = data.userId
    userName.value = data.userName
    tenantId.value = data.tenantId
    uni.setStorageSync('userId', String(data.userId))
    uni.setStorageSync('tenantId', String(data.tenantId))
  }

  return { userId, userName, tenantId, orgName, roleName, avatar, setUser }
})
