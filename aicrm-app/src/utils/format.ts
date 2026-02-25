import dayjs from 'dayjs'

export function formatDate(val: string | null | undefined, fmt = 'YYYY-MM-DD'): string {
  return val ? dayjs(val).format(fmt) : '-'
}

export function formatDateTime(val: string | null | undefined): string {
  return val ? dayjs(val).format('MM-DD HH:mm') : '-'
}

export function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return '¥' + val.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

export const visitTypeMap: Record<number, string> = { 1: '现场拜访', 2: '电话拜访', 3: '微信拜访', 4: '企业微信', 5: '视频会议' }
export const visitStatusMap: Record<number, string> = { 0: '计划中', 1: '进行中', 2: '已完成', 3: '已取消' }
export const leadStatusMap: Record<number, string> = { 0: '待分配', 1: '已分配', 2: '跟进中', 3: '已转化', 4: '已退回', 5: '无效' }
export const leadIntentionMap: Record<string, string> = { A: 'A级', B: 'B级', C: 'C级', D: 'D级' }
export const followUpTypeMap: Record<number, string> = { 1: '现场', 2: '电话', 3: '微信', 4: '邮件', 5: '企微', 6: '其他' }
export const oppStageMap: Record<number, string> = { 1: '初步接触', 2: '需求确认', 3: '方案报价', 4: '商务谈判', 5: '赢单', 6: '输单' }
export const customerStageMap: Record<number, string> = { 1: '潜在', 2: '意向', 3: '成交', 4: '活跃', 5: 'VIP', 6: '流失', 7: '无效', 8: '黑名单' }
export const oppStatusMap: Record<number, string> = { 1: '进行中', 2: '赢单', 3: '输单', 4: '无效' }
export const taskStatusMap: Record<number, string> = { 0: '待开始', 1: '进行中', 2: '已完成', 3: '已取消', 4: '已逾期' }
export const priorityMap: Record<number, string> = { 1: '低', 2: '中', 3: '高', 4: '紧急' }
export const priorityColorMap: Record<number, string> = { 1: '#8E8E93', 2: '#4F6EF6', 3: '#FF9500', 4: '#FF3B30' }

// 活动
export const activityStatusMap: Record<number, string> = { 0: '未开始', 1: '报名中', 2: '进行中', 3: '已结束' }
export const activityNeedSignupMap: Record<number, string> = { 0: '不需要报名', 1: '需要报名' }

// 好友
export const friendTypeMap: Record<number, string> = { 1: '平台好友', 2: '企微好友', 3: '双渠道' }
