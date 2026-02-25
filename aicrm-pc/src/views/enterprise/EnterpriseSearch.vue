<template>
  <div class="enterprise-search">
    <div class="page-header">
      <h2 class="page-title">企业信息查询</h2>
      <div v-if="quota" class="quota-info">
        <span>今日剩余：{{ quota.dailyRemaining ?? '-' }}</span>
        <span class="quota-divider">|</span>
        <span>本月剩余：{{ quota.monthlyRemaining ?? '-' }}</span>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="输入企业名称或统一社会信用代码搜索"
        clearable
        style="width: 400px"
        @keyup.enter="onSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="onSearch">搜索</el-button>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>搜索中...</span>
    </div>

    <div v-else-if="searched && resultList.length === 0" class="empty-wrap">
      暂无搜索结果
    </div>

    <div v-else-if="resultList.length > 0" class="result-cards">
      <el-card v-for="ent in resultList" :key="ent.creditCode || ent.companyName" class="enterprise-card" shadow="hover">
        <div class="card-header">
          <h3 class="company-name">{{ ent.companyName || '-' }}</h3>
          <el-tag v-if="ent.isPlatformCustomer" type="success" size="small">已是平台客户</el-tag>
          <el-tag v-else type="info" size="small">非平台客户</el-tag>
        </div>
        <div class="card-body">
          <div class="info-row"><span class="label">信用代码：</span>{{ ent.creditCode || '-' }}</div>
          <div class="info-row"><span class="label">法人：</span>{{ ent.legalPerson || '-' }}</div>
          <div class="info-row"><span class="label">注册资本：</span>{{ ent.registeredCapital || '-' }}</div>
          <div class="info-row"><span class="label">成立日期：</span>{{ formatDate(ent.establishedDate) }}</div>
          <div class="info-row"><span class="label">经营状态：</span>{{ ent.companyStatus || '-' }}</div>
          <div class="info-row"><span class="label">行业：</span>{{ ent.industry || '-' }}</div>
          <div class="info-row"><span class="label">已缓存联系人数：</span>{{ ent.contactCacheCount ?? 0 }}</div>
        </div>
        <div class="card-footer">
          <el-button type="primary" size="small" @click="toggleContacts(ent)">查询联系方式</el-button>
          <el-button size="small" @click="importAsCustomer(ent)">导入为客户</el-button>
        </div>

        <!-- 联系方式面板 -->
        <div v-if="expandedCreditCode === ent.creditCode" class="contacts-panel">
          <el-table v-loading="contactsLoading" :data="contactsList" size="small" stripe>
            <el-table-column prop="contactName" label="姓名" width="100" />
            <el-table-column prop="position" label="职位" width="120" />
            <el-table-column prop="phone" label="手机" width="140" />
            <el-table-column prop="email" label="邮箱" width="160" />
            <el-table-column prop="sourceTypeLabel" label="来源类型" width="100" />
            <el-table-column label="可靠度" width="90">
              <template #default="{ row }">
                <el-tag
                  :type="reliabilityTagType(row.reliability)"
                  size="small"
                >
                  {{ row.reliabilityLabel || reliabilityLabel(row.reliability) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="importContact(ent, row)">导入</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>

    <!-- 导入联系人弹窗 -->
    <el-dialog v-model="showImportDialog" title="导入联系人" width="400px">
      <el-form v-if="importTarget" :model="importForm" label-width="90px">
        <el-form-item label="客户">
          <el-select v-model="importForm.customerId" placeholder="请选择客户" filterable style="width: 100%">
            <el-option
              v-for="c in customerOptions"
              :key="c.id"
              :label="c.name || c.customerName"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="主联系人">
          <el-switch v-model="importForm.isPrimary" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="confirmImportContact">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { enterpriseApi, customerApi } from '@/api/index'
import dayjs from 'dayjs'

const keyword = ref('')
const loading = ref(false)
const searched = ref(false)
const resultList = ref<any[]>([])
const quota = ref<any>(null)
const expandedCreditCode = ref<string | null>(null)
const contactsList = ref<any[]>([])
const contactsLoading = ref(false)
const showImportDialog = ref(false)
const importing = ref(false)
const importTarget = ref<{ creditCode: string; contact: any } | null>(null)
const customerOptions = ref<any[]>([])

const importForm = reactive({
  customerId: undefined as number | undefined,
  isPrimary: false,
})

function formatDate(val: string | null | undefined): string {
  return val ? dayjs(val).format('YYYY-MM-DD') : '-'
}

function reliabilityLabel(r: number) {
  const map: Record<number, string> = { 1: '高', 2: '中', 3: '低' }
  return map[r] ?? '-'
}

function reliabilityTagType(r: number) {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'info' }
  return map[r] ?? 'info'
}

async function loadQuota() {
  try {
    quota.value = await enterpriseApi.getQuota()
  } catch {
    quota.value = null
  }
}

async function onSearch() {
  if (!keyword.value?.trim()) return
  loading.value = true
  searched.value = true
  try {
    resultList.value = (await enterpriseApi.search(keyword.value.trim())) as unknown as any[]
    expandedCreditCode.value = null
    loadQuota()
  } catch {
    resultList.value = []
  } finally {
    loading.value = false
  }
}

async function toggleContacts(ent: any) {
  const code = ent.creditCode
  if (!code) return
  if (expandedCreditCode.value === code) {
    expandedCreditCode.value = null
    contactsList.value = []
    return
  }
  expandedCreditCode.value = code
  contactsLoading.value = true
  try {
    contactsList.value = (await enterpriseApi.queryContacts(code)) as unknown as any[]
  } catch {
    contactsList.value = []
  } finally {
    contactsLoading.value = false
  }
}

function importAsCustomer(_ent: any) {
  // TODO: 调用导入为企业/客户的接口，如后端提供
  console.log('导入为客户', _ent)
}

async function loadCustomerOptions() {
  try {
    const res: any = await customerApi.page({ pageNum: 1, pageSize: 100 })
    customerOptions.value = res?.records ?? res?.list ?? []
  } catch {
    customerOptions.value = []
  }
}

function importContact(ent: any, contact: any) {
  importTarget.value = { creditCode: ent.creditCode, contact }
  importForm.customerId = undefined
  importForm.isPrimary = false
  loadCustomerOptions()
  showImportDialog.value = true
}

async function confirmImportContact() {
  if (!importTarget.value || !importForm.customerId) return
  importing.value = true
  try {
    await enterpriseApi.importContact(
      importTarget.value.creditCode,
      importTarget.value.contact.id,
      { customerId: importForm.customerId, isPrimary: importForm.isPrimary }
    )
    showImportDialog.value = false
    importTarget.value = null
    if (expandedCreditCode.value) {
      const code = expandedCreditCode.value
      contactsList.value = (await enterpriseApi.queryContacts(code)) as unknown as any[]
    }
  } finally {
    importing.value = false
  }
}

onMounted(() => loadQuota())
</script>

<style scoped>
.enterprise-search { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; margin: 0; }
.quota-info { font-size: 14px; color: #636e7b; }
.quota-divider { margin: 0 8px; color: #ddd; }
.search-bar { display: flex; gap: 12px; margin-bottom: 24px; }
.loading-wrap, .empty-wrap { text-align: center; padding: 60px 0; color: #636e7b; }
.result-cards { display: flex; flex-direction: column; gap: 20px; }
.enterprise-card { margin-bottom: 0; }
.enterprise-card :deep(.el-card__body) { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.company-name { font-size: 18px; font-weight: 600; color: #1a1a2e; margin: 0; }
.card-body { margin-bottom: 16px; }
.info-row { font-size: 14px; color: #636e7b; margin-bottom: 8px; }
.info-row .label { color: #a0a8b4; margin-right: 4px; }
.card-footer { display: flex; gap: 12px; padding-top: 12px; border-top: 1px solid #eee; }
.contacts-panel { margin-top: 16px; padding-top: 16px; border-top: 1px solid #eee; }
</style>
