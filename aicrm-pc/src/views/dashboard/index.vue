<template>
  <div class="dashboard">
    <!-- 顶部欢迎语 -->
    <div class="welcome-bar">
      <div class="welcome-left">
        <h1 class="greeting">早上好，李明</h1>
        <span class="todo-count">今日待办 {{ todoCount }} 项</span>
      </div>
      <div class="welcome-right">{{ currentDate }}</div>
    </div>

    <!-- 数据概览 -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-value">156</div>
        <div class="stat-label">新增线索(本月)</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">892</div>
        <div class="stat-label">活跃客户</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">45</div>
        <div class="stat-label">进行中商机</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">¥2,580,000</div>
        <div class="stat-label">本月成交额</div>
      </div>
    </div>

    <!-- 两列布局 -->
    <el-row :gutter="20">
      <el-col :span="14">
        <!-- 今日任务 -->
        <el-card class="panel-card" shadow="hover">
          <template #header>
            <span>今日任务</span>
          </template>
          <el-table :data="tasks" size="small" stripe>
            <el-table-column prop="title" label="任务标题" />
            <el-table-column prop="priority" label="优先级" width="90">
              <template #default="{ row }">
                <el-tag :type="row.priorityType" size="small">{{ row.priority }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.statusType" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="时间" width="100" />
          </el-table>
        </el-card>

        <!-- 最近跟进 -->
        <el-card class="panel-card" shadow="hover">
          <template #header>
            <span>最近跟进</span>
          </template>
          <el-timeline>
            <el-timeline-item v-for="(f, i) in followUps" :key="i" :timestamp="f.time" placement="top">
              <div class="follow-content">{{ f.content }}</div>
              <div class="follow-meta">
                <span class="customer">{{ f.customer }}</span>
                <el-tag size="small" type="info">{{ f.method }}</el-tag>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :span="10">
        <!-- 商机阶段分布 -->
        <el-card class="panel-card" shadow="hover">
          <template #header>
            <span>商机阶段分布</span>
          </template>
          <div class="stage-chart">
            <div v-for="s in stages" :key="s.name" class="stage-row">
              <span class="stage-name">{{ s.name }}</span>
              <div class="stage-bar-wrap">
                <div class="stage-bar" :style="{ width: s.percent + '%', background: s.color }"></div>
              </div>
              <span class="stage-value">{{ s.value }}</span>
            </div>
          </div>
        </el-card>

        <!-- 快捷操作 -->
        <el-card class="panel-card" shadow="hover">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/leads')">
              <el-icon><Plus /></el-icon>
              新建线索
            </el-button>
            <el-button type="primary" @click="$router.push('/customers')">
              <el-icon><User /></el-icon>
              新建客户
            </el-button>
            <el-button type="primary" @click="$router.push('/visits')">
              <el-icon><Location /></el-icon>
              新建拜访
            </el-button>
            <el-button type="primary" @click="$router.push('/tasks')">
              <el-icon><List /></el-icon>
              新建任务
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const todoCount = ref(5)
const currentDate = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${['日', '一', '二', '三', '四', '五', '六'][d.getDay()]}`
})

const tasks = ref([
  { title: '跟进客户A的商机', priority: '高', priorityType: 'danger', status: '进行中', statusType: 'primary', time: '09:00' },
  { title: '完成周报提交', priority: '中', priorityType: 'warning', status: '待办', statusType: 'info', time: '14:00' },
  { title: '拜访客户B', priority: '低', priorityType: 'info', status: '已完成', statusType: 'success', time: '16:00' },
])

const followUps = ref([
  { content: '电话沟通产品方案，客户表示下周考虑', customer: '某某科技', time: '10:30', method: '电话' },
  { content: '发送报价单，等待客户反馈', customer: 'ABC公司', time: '09:15', method: '邮件' },
  { content: '现场演示产品，客户兴趣较高', customer: 'XYZ集团', time: '昨天 15:00', method: '拜访' },
])

const stages = ref([
  { name: '初步接触', value: 12, percent: 24, color: '#4F6EF6' },
  { name: '需求确认', value: 18, percent: 36, color: '#67C23A' },
  { name: '方案报价', value: 10, percent: 20, color: '#E6A23C' },
  { name: '商务谈判', value: 6, percent: 12, color: '#F56C6C' },
  { name: '赢单', value: 4, percent: 8, color: '#909399' },
])
</script>

<style scoped>
.dashboard { padding: 0; }
.welcome-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,.04);
}
.greeting { font-size: 24px; font-weight: 600; color: #1a1a2e; margin: 0 0 4px 0; }
.todo-count { font-size: 14px; color: #636e7b; }
.welcome-right { font-size: 14px; color: #636e7b; }
.stat-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.stat-card {
  flex: 1;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,.04);
  text-align: center;
}
.stat-value { font-size: 28px; font-weight: 700; color: #4F6EF6; margin-bottom: 8px; }
.stat-label { font-size: 13px; color: #636e7b; }
.panel-card { margin-bottom: 20px; border-radius: 12px; }
.follow-content { font-size: 14px; color: #1a1a2e; margin-bottom: 4px; }
.follow-meta { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #636e7b; }
.stage-chart { padding: 8px 0; }
.stage-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 13px;
}
.stage-name { width: 80px; color: #636e7b; }
.stage-bar-wrap {
  flex: 1;
  height: 20px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}
.stage-bar { height: 100%; border-radius: 4px; transition: width .3s; }
.stage-value { width: 30px; text-align: right; font-weight: 600; color: #1a1a2e; }
.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.quick-actions .el-button { flex: 1; min-width: 120px; }
</style>
