<template>
  <div class="page">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <div class="card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="组织架构" name="org">
          <el-tree
            :data="orgTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            default-expand-all
            v-loading="orgLoading"
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <span>{{ data.name }}</span>
                <el-tag v-if="data.type" size="small" type="info">{{ data.type }}</el-tag>
              </span>
            </template>
          </el-tree>
        </el-tab-pane>
        <el-tab-pane label="基本设置" name="basic">
          <el-form label-width="120px" class="settings-form">
            <el-form-item label="通知设置">
              <el-switch v-model="basicSettings.notify" />
            </el-form-item>
            <el-form-item label="自动同步">
              <el-switch v-model="basicSettings.autoSync" />
            </el-form-item>
            <el-form-item label="系统版本">
              <span class="version-text">v1.0.0</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { orgApi } from '@/api'

const activeTab = ref('org')
const orgLoading = ref(false)
const orgTree = ref<any[]>([])
const basicSettings = reactive({ notify: true, autoSync: false })

const defaultOrgTree = [
  {
    id: 1,
    name: '总公司',
    type: '公司',
    children: [
      {
        id: 2,
        name: '销售部',
        type: '部门',
        children: [
          { id: 3, name: '华东区', type: '部门', children: [] },
          { id: 4, name: '华南区', type: '部门', children: [] },
        ],
      },
      { id: 5, name: '市场部', type: '部门', children: [] },
    ],
  },
]

async function loadOrgTree() {
  orgLoading.value = true
  try {
    const res = await orgApi.tree()
    if (Array.isArray(res) && res.length) {
      orgTree.value = res
    } else if (res && (res as any).children?.length) {
      orgTree.value = [res as any]
    } else {
      orgTree.value = defaultOrgTree
    }
  } catch {
    orgTree.value = defaultOrgTree
  } finally {
    orgLoading.value = false
  }
}

onMounted(loadOrgTree)
</script>

<style scoped>
.tree-node { display: flex; align-items: center; gap: 8px; }
.settings-form { max-width: 400px; }
.version-text { color: var(--text-secondary); font-size: 14px; }
</style>
