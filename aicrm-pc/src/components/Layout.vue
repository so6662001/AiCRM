<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo" @click="$router.push('/')">
        <span v-if="!collapsed" class="logo-text">AiCRM</span>
        <span v-else class="logo-text-mini">Ai</span>
      </div>
      <el-menu :default-active="$route.path" :collapse="collapsed" router background-color="#1a1a2e" text-color="#a0a8b4" active-text-color="#fff" :collapse-transition="false">
        <template v-for="r in menuRoutes" :key="r.path">
          <el-menu-item :index="'/' + r.path">
            <el-icon><component :is="r.meta?.icon || 'Document'" /></el-icon>
            <template #title>{{ r.meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <el-icon class="toggle-btn" @click="collapsed = !collapsed"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="$route.meta?.title">{{ $route.meta.title }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="topbar-right">
          <el-badge :value="3" :max="99" class="notif-badge">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" style="background:#4F6EF6">李</el-avatar>
              <span class="user-name">李明</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人信息</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const collapsed = ref(false)
const router = useRouter()
const menuRoutes = computed(() =>
  (router.options.routes[0]?.children || []).filter((r: any) => !r.meta?.hidden)
)
</script>

<style scoped>
.layout { height: 100vh; }
.sidebar { background: #1a1a2e; transition: width .2s; overflow-x: hidden; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid rgba(255,255,255,.06); }
.logo-text { font-size: 22px; font-weight: 700; color: #fff; letter-spacing: 2px; }
.logo-text-mini { font-size: 20px; font-weight: 700; color: #4F6EF6; }
.el-menu { border-right: none !important; }
.el-menu-item.is-active { background: rgba(79,110,246,.15) !important; border-right: 3px solid #4F6EF6; }
.topbar { display: flex; align-items: center; gap: 16px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.04); padding: 0 24px; height: 60px !important; }
.toggle-btn { cursor: pointer; font-size: 20px; color: #636e7b; }
.breadcrumb { flex: 1; }
.topbar-right { display: flex; align-items: center; gap: 20px; }
.notif-badge { cursor: pointer; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.user-name { font-size: 14px; color: #1a1a2e; }
.main-content { background: #f5f6fa; padding: 20px; overflow-y: auto; }
</style>
