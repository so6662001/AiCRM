# AiCRM API 接口设计文档

## 1. API 设计规范

### 1.1 URL 规范

```
格式：/{api-version}/{module}/{resource}/{action}
示例：/v1/leads                    -- 线索列表
      /v1/leads/{id}               -- 线索详情
      /v1/leads/{id}/assign        -- 线索分配
      /v1/customers/{id}/follow-ups -- 客户跟进记录
```

### 1.2 请求/响应规范

**统一响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1708848000000,
  "traceId": "abc123def456"
}
```

**分页请求参数**：
```
?pageNum=1&pageSize=20&sortField=createdTime&sortOrder=desc
```

**分页响应格式**：
```json
{
  "code": 200,
  "data": {
    "records": [],
    "total": 1000,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 50
  }
}
```

### 1.3 鉴权规范

- 所有 API 请求 Header 携带 Token：`Authorization: Bearer {token}`
- Gateway 层调用现有鉴权系统验证 Token
- 多租户标识从 Token 中解析或通过 Header 传递：`X-Tenant-Id: {tenantId}`
- CRM 内部数据权限在业务层控制

### 1.4 错误码规范

| 错误码 | 说明 |
|---|---|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 数据冲突（如客户查重） |
| 422 | 业务校验失败 |
| 429 | 请求限流 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

---

## 2. 线索管理 API

### 2.1 线索 CRUD

```
POST   /v1/leads                      -- 创建线索
GET    /v1/leads                      -- 线索列表（分页、筛选）
GET    /v1/leads/{id}                 -- 线索详情
PUT    /v1/leads/{id}                 -- 更新线索
DELETE /v1/leads/{id}                 -- 删除线索（逻辑删除）
POST   /v1/leads/import               -- 批量导入线索（Excel）
GET    /v1/leads/export               -- 导出线索
```

**创建线索请求**：
```json
POST /v1/leads
{
  "contactName": "张三",
  "contactPhone": "13800138000",
  "contactEmail": "zhangsan@company.com",
  "companyName": "示例科技有限公司",
  "position": "采购总监",
  "source": "manual",
  "sourceDetail": "运营手动录入",
  "intentionLevel": "B",
  "province": "广东",
  "city": "深圳",
  "industry": "互联网",
  "remark": "从行业展会获取"
}
```

**线索列表查询参数**：
```
GET /v1/leads?pageNum=1&pageSize=20
    &status=0,1,2                    -- 状态筛选（多选）
    &intentionLevel=A,B              -- 意向等级筛选
    &source=manual                   -- 来源筛选
    &ownerUserId=123                 -- 负责人筛选
    &keyword=张三                     -- 关键词搜索
    &createdTimeStart=2026-01-01     -- 创建时间范围
    &createdTimeEnd=2026-02-25
    &inPool=false                    -- 是否公海池
    &sortField=createdTime
    &sortOrder=desc
```

### 2.2 线索分配

```
POST   /v1/leads/assign                    -- 分配线索（单条/批量）
POST   /v1/leads/{id}/reassign             -- 经理再分配
POST   /v1/leads/{id}/return               -- 退回线索到公海池
POST   /v1/leads/{id}/pick                 -- 从公海池领取线索
GET    /v1/leads/pool                      -- 公海池线索列表
GET    /v1/leads/{id}/assign-logs          -- 分配历史记录
```

**分配线索请求**：
```json
POST /v1/leads/assign
{
  "leadIds": [1001, 1002, 1003],
  "targetUserId": 2001,
  "remark": "分配给华南区销售经理"
}
```

**退回线索请求**：
```json
POST /v1/leads/{id}/return
{
  "returnReason": "客户联系方式无效"
}
```

### 2.3 线索转化

```
POST   /v1/leads/{id}/convert             -- 线索转化为客户
```

**线索转化请求**：
```json
POST /v1/leads/{id}/convert
{
  "customerName": "示例科技有限公司",
  "lifecycleStage": 1,
  "createOpportunity": true,
  "opportunity": {
    "opportunityName": "示例科技ERP项目",
    "expectedAmount": 500000,
    "expectedCloseDate": "2026-06-30"
  }
}
```

### 2.4 公海池配置

```
GET    /v1/lead-pool-configs               -- 公海池配置列表
POST   /v1/lead-pool-configs               -- 创建公海池配置
PUT    /v1/lead-pool-configs/{id}          -- 更新公海池配置
```

---

## 3. 客户管理 API

### 3.1 客户 CRUD

```
POST   /v1/customers                       -- 创建客户
GET    /v1/customers                       -- 客户列表
GET    /v1/customers/{id}                  -- 客户详情（360°视图）
PUT    /v1/customers/{id}                  -- 更新客户
DELETE /v1/customers/{id}                  -- 删除客户
POST   /v1/customers/import                -- 批量导入
GET    /v1/customers/export                -- 导出客户
POST   /v1/customers/duplicate-check       -- 客户查重
POST   /v1/customers/transfer              -- 批量转移客户
```

**客户360°详情响应**：
```json
GET /v1/customers/{id}
{
  "code": 200,
  "data": {
    "customer": { ... },
    "contacts": [ ... ],
    "recentFollowUps": [ ... ],
    "opportunities": [ ... ],
    "visitRecords": [ ... ],
    "erpSyncInfo": {
      "erpCustomerId": "ERP-C001",
      "syncStatus": "synced",
      "lastSyncTime": "2026-02-20T10:00:00"
    },
    "statistics": {
      "totalFollowUps": 15,
      "totalVisits": 5,
      "totalDealAmount": 1500000,
      "daysSinceLastFollow": 3
    }
  }
}
```

### 3.2 客户联系人

```
POST   /v1/customers/{id}/contacts         -- 添加联系人
GET    /v1/customers/{id}/contacts         -- 联系人列表
PUT    /v1/customers/{id}/contacts/{cid}   -- 更新联系人
DELETE /v1/customers/{id}/contacts/{cid}   -- 删除联系人
```

### 3.3 客户审核

```
POST   /v1/customers/{id}/submit-approval  -- 提交审核
GET    /v1/customer-approvals              -- 审核列表
POST   /v1/customer-approvals/{id}/approve -- 审核通过
POST   /v1/customer-approvals/{id}/reject  -- 审核拒绝
```

---

## 4. 商机管理 API

### 4.1 商机 CRUD

```
POST   /v1/opportunities                   -- 创建商机
GET    /v1/opportunities                   -- 商机列表
GET    /v1/opportunities/{id}              -- 商机详情
PUT    /v1/opportunities/{id}              -- 更新商机
DELETE /v1/opportunities/{id}              -- 删除商机
```

### 4.2 商机阶段

```
PUT    /v1/opportunities/{id}/stage        -- 变更商机阶段
GET    /v1/opportunities/{id}/stage-logs   -- 阶段变更历史
POST   /v1/opportunities/{id}/win          -- 标记赢单
POST   /v1/opportunities/{id}/lose         -- 标记输单
```

**变更商机阶段请求**：
```json
PUT /v1/opportunities/{id}/stage
{
  "stageId": 3,
  "remark": "客户已确认需求，进入方案提案阶段"
}
```

### 4.3 商机阶段配置

```
GET    /v1/opportunity-stage-configs       -- 阶段配置列表
POST   /v1/opportunity-stage-configs       -- 创建阶段
PUT    /v1/opportunity-stage-configs/{id}  -- 更新阶段
DELETE /v1/opportunity-stage-configs/{id}  -- 删除阶段
PUT    /v1/opportunity-stage-configs/sort  -- 阶段排序
```

### 4.4 销售漏斗

```
GET    /v1/opportunities/funnel            -- 销售漏斗数据
GET    /v1/opportunities/forecast          -- 销售预测
```

**销售漏斗响应**：
```json
GET /v1/opportunities/funnel?period=2026-Q1
{
  "code": 200,
  "data": {
    "stages": [
      { "stageId": 1, "stageName": "初步接触", "count": 50, "amount": 5000000 },
      { "stageId": 2, "stageName": "需求确认", "count": 30, "amount": 3500000 },
      { "stageId": 3, "stageName": "方案提案", "count": 15, "amount": 2000000 },
      { "stageId": 4, "stageName": "报价阶段", "count": 8, "amount": 1200000 },
      { "stageId": 5, "stageName": "谈判阶段", "count": 5, "amount": 800000 },
      { "stageId": 6, "stageName": "赢单", "count": 3, "amount": 500000 }
    ],
    "wonCount": 3,
    "wonAmount": 500000,
    "lostCount": 5,
    "lostAmount": 600000,
    "conversionRate": 6.0
  }
}
```

---

## 5. 跟进管理 API

### 5.1 跟进记录

```
POST   /v1/follow-ups                      -- 创建跟进记录
GET    /v1/follow-ups                      -- 跟进记录列表（支持按客户/线索/商机筛选）
GET    /v1/follow-ups/{id}                 -- 跟进详情
PUT    /v1/follow-ups/{id}                 -- 更新跟进记录
DELETE /v1/follow-ups/{id}                 -- 删除跟进记录
```

**创建跟进记录请求**：
```json
POST /v1/follow-ups
{
  "bizType": 2,
  "bizId": 5001,
  "customerId": 5001,
  "followType": 2,
  "content": "与客户沟通了项目需求，客户对产品功能较满意，计划下周安排演示",
  "nextFollowTime": "2026-03-01T10:00:00",
  "nextFollowNote": "安排产品演示",
  "attachmentIds": [8001, 8002]
}
```

### 5.2 跟进时间线

```
GET    /v1/customers/{id}/timeline         -- 客户跟进时间线（聚合所有跟进记录、拜访、阶段变更等）
```

**时间线响应**：
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "type": "follow_up",
        "time": "2026-02-25T14:30:00",
        "user": { "id": 101, "name": "李明" },
        "content": "电话沟通需求",
        "hasRecording": true,
        "aiSummary": "客户对XX产品感兴趣..."
      },
      {
        "type": "visit",
        "time": "2026-02-22T10:00:00",
        "user": { "id": 101, "name": "李明" },
        "visitType": "onsite",
        "content": "现场拜访"
      },
      {
        "type": "stage_change",
        "time": "2026-02-20T09:00:00",
        "opportunityName": "XX项目",
        "fromStage": "需求确认",
        "toStage": "方案提案"
      }
    ]
  }
}
```

---

## 6. 外勤管理 API

### 6.1 拜访管理

```
POST   /v1/visits                          -- 创建拜访计划/记录
GET    /v1/visits                          -- 拜访记录列表
GET    /v1/visits/{id}                     -- 拜访详情
PUT    /v1/visits/{id}                     -- 更新拜访
PUT    /v1/visits/{id}/complete            -- 完成拜访
DELETE /v1/visits/{id}                     -- 取消拜访
GET    /v1/visits/calendar                 -- 拜访日历视图
GET    /v1/visits/daily-report             -- 每日拜访汇总
```

**创建现场拜访请求**：
```json
POST /v1/visits
{
  "customerId": 5001,
  "contactId": 6001,
  "visitType": 1,
  "visitPurpose": "产品演示",
  "visitTime": "2026-02-26T10:00:00",
  "remark": "携带演示设备"
}
```

**创建电话拜访请求**：
```json
POST /v1/visits
{
  "customerId": 5001,
  "contactId": 6001,
  "visitType": 2,
  "visitPurpose": "需求确认",
  "callPhone": "13800138000",
  "visitTime": "2026-02-25T14:00:00"
}
```

**创建微信拜访请求**：
```json
POST /v1/visits
{
  "customerId": 5001,
  "contactId": 6001,
  "visitType": 3,
  "visitPurpose": "报价沟通",
  "visitTime": "2026-02-25T15:00:00",
  "chatScreenshotIds": [9001, 9002]
}
```

### 6.2 签到打卡

```
POST   /v1/checkins                        -- 签到/打卡
GET    /v1/checkins                        -- 打卡记录列表
GET    /v1/checkins/today                  -- 今日打卡记录
GET    /v1/checkins/statistics             -- 打卡统计
```

**签到请求**：
```json
POST /v1/checkins
{
  "checkinType": 2,
  "address": "深圳市南山区科技园XX大厦",
  "longitude": 113.9306,
  "latitude": 22.5429,
  "photoUrl": "https://oss.example.com/checkin/20260225_1.jpg",
  "deviceInfo": "iPhone 15 Pro, iOS 19",
  "relatedVisitId": 7001,
  "remark": "客户拜访签到"
}
```

### 6.3 电话录音

```
POST   /v1/call-recordings                 -- 上传电话录音
GET    /v1/call-recordings                 -- 录音列表
GET    /v1/call-recordings/{id}            -- 录音详情（含转写文本、AI分析）
POST   /v1/call-recordings/{id}/transcribe -- 触发AI转写
POST   /v1/call-recordings/{id}/analyze    -- 触发AI分析
GET    /v1/call-recordings/violations      -- 违规录音列表
```

**上传录音请求**：
```json
POST /v1/call-recordings
{
  "userId": 101,
  "customerId": 5001,
  "contactId": 6001,
  "visitId": 7001,
  "callType": 1,
  "callerNumber": "13800001111",
  "calleeNumber": "13800138000",
  "callStartTime": "2026-02-25T14:00:00",
  "callEndTime": "2026-02-25T14:15:32",
  "callDuration": 932,
  "recordingFileUrl": "https://oss.example.com/recordings/20260225_101_001.mp3",
  "recordingFileSize": 4562340,
  "recordingFormat": "mp3",
  "source": "app_auto"
}
```

**录音详情响应（含 AI 分析）**：
```json
GET /v1/call-recordings/{id}
{
  "code": 200,
  "data": {
    "id": 10001,
    "callDuration": 932,
    "recordingFileUrl": "...",
    "transcriptionStatus": "completed",
    "transcriptionText": "销售：您好，我是XX公司的李明...\n客户：你好...",
    "aiSummary": "本次通话主要讨论了客户对ERP系统的需求。客户关注点包括：1)库存管理模块 2)财务对接 3)价格方案。客户表示下周可以安排演示。",
    "aiKeywords": ["ERP", "库存管理", "财务对接", "演示"],
    "aiSentiment": "positive",
    "aiActionItems": [
      { "action": "安排产品演示", "deadline": "2026-03-04" },
      { "action": "准备报价方案", "deadline": "2026-03-01" }
    ],
    "hasViolation": true,
    "violationDetail": [
      {
        "word": "绝对不会出问题",
        "category": "虚假承诺",
        "level": 2,
        "position": "03:25",
        "context": "...我们的系统绝对不会出问题，您放心..."
      }
    ]
  }
}
```

### 6.4 违规词管理

```
GET    /v1/violation-words                  -- 违规词列表
POST   /v1/violation-words                  -- 添加违规词
PUT    /v1/violation-words/{id}             -- 更新违规词
DELETE /v1/violation-words/{id}             -- 删除违规词
POST   /v1/violation-words/import           -- 批量导入违规词
```

---

## 7. 任务管理 API

### 7.1 工作任务

```
POST   /v1/tasks                            -- 创建任务
GET    /v1/tasks                            -- 任务列表
GET    /v1/tasks/{id}                       -- 任务详情
PUT    /v1/tasks/{id}                       -- 更新任务
DELETE /v1/tasks/{id}                       -- 删除任务
PUT    /v1/tasks/{id}/status                -- 更新任务状态
PUT    /v1/tasks/{id}/complete              -- 完成任务
```

**创建任务请求（自主创建）**：
```json
POST /v1/tasks
{
  "taskTitle": "拜访示例科技有限公司",
  "taskContent": "与采购总监沟通ERP采购事宜",
  "taskType": 1,
  "priority": 3,
  "planStartTime": "2026-02-26T09:00:00",
  "planEndTime": "2026-02-26T11:00:00",
  "assignType": 1,
  "assigneeUserId": 101,
  "relatedBizType": 2,
  "relatedBizId": 5001,
  "remindTime": "2026-02-26T08:30:00"
}
```

**创建任务请求（上级安排）**：
```json
POST /v1/tasks
{
  "taskTitle": "跟进XX客户的ERP项目报价",
  "taskContent": "根据客户需求准备详细报价方案",
  "taskType": 3,
  "priority": 4,
  "planStartTime": "2026-02-25T14:00:00",
  "planEndTime": "2026-02-25T18:00:00",
  "assignType": 2,
  "assigneeUserId": 102,
  "assignerUserId": 101,
  "relatedBizType": 3,
  "relatedBizId": 4001
}
```

### 7.2 今日工作台

```
GET    /v1/tasks/today                      -- 今日任务列表
GET    /v1/tasks/today/summary              -- 今日工作概览
GET    /v1/tasks/overdue                    -- 逾期任务
```

**今日工作概览响应**：
```json
GET /v1/tasks/today/summary
{
  "code": 200,
  "data": {
    "date": "2026-02-25",
    "totalTasks": 8,
    "completedTasks": 3,
    "inProgressTasks": 2,
    "pendingTasks": 3,
    "overdueTasks": 1,
    "todayVisits": [
      {
        "visitId": 7001,
        "customerName": "示例科技",
        "visitType": "现场拜访",
        "visitTime": "10:00",
        "status": "已完成"
      }
    ],
    "todayFollowUps": 5,
    "assignedByManager": [
      {
        "taskId": 3001,
        "taskTitle": "跟进XX客户报价",
        "assignerName": "王经理",
        "priority": "紧急"
      }
    ]
  }
}
```

---

## 8. ERP 集成 API

### 8.1 ERP 配置管理

```
GET    /v1/erp-configs                      -- ERP配置列表
POST   /v1/erp-configs                      -- 创建ERP配置
PUT    /v1/erp-configs/{id}                 -- 更新ERP配置
DELETE /v1/erp-configs/{id}                 -- 删除ERP配置
POST   /v1/erp-configs/{id}/test            -- 测试ERP连接
```

### 8.2 数据同步

```
POST   /v1/erp/sync/customers/pull          -- 从ERP拉取客户（期初/增量）
POST   /v1/erp/sync/customers/push          -- 推送客户到ERP（审核通过后）
POST   /v1/erp/sync/customers/{id}/push     -- 推送单个客户到ERP
GET    /v1/erp/sync/logs                    -- 同步日志列表
GET    /v1/erp/sync/logs/{id}              -- 同步日志详情（含明细）
POST   /v1/erp/sync/retry/{logId}          -- 重试失败的同步
```

**从 ERP 拉取客户请求**：
```json
POST /v1/erp/sync/customers/pull
{
  "erpConfigId": 1,
  "syncMode": "full",
  "filters": {
    "modifiedAfter": "2026-01-01T00:00:00"
  }
}
```

**推送客户到 ERP 请求**：
```json
POST /v1/erp/sync/customers/push
{
  "erpConfigId": 1,
  "customerIds": [5001, 5002, 5003]
}
```

### 8.3 CRM 对外提供的开放 API（供 ERP 回调）

```
POST   /v1/open/customers/webhook          -- ERP 客户变更回调
POST   /v1/open/orders/webhook             -- ERP 订单同步回调
```

---

## 9. 报表与统计 API

```
GET    /v1/reports/sales-performance        -- 销售业绩报表
GET    /v1/reports/lead-conversion          -- 线索转化分析
GET    /v1/reports/follow-up-statistics     -- 跟进统计
GET    /v1/reports/visit-statistics         -- 拜访统计
GET    /v1/reports/violation-statistics     -- 违规统计
GET    /v1/reports/ranking                  -- 销售排行榜
GET    /v1/reports/dashboard                -- 仪表盘数据
```

**仪表盘数据响应**：
```json
GET /v1/reports/dashboard
{
  "code": 200,
  "data": {
    "overview": {
      "totalLeads": 1500,
      "newLeadsToday": 25,
      "totalCustomers": 800,
      "activeOpportunities": 120,
      "expectedRevenue": 15000000,
      "wonThisMonth": 2500000
    },
    "leadConversion": {
      "thisMonth": 12.5,
      "lastMonth": 10.8,
      "trend": "up"
    },
    "topSales": [
      { "userId": 101, "userName": "李明", "dealAmount": 850000 }
    ],
    "todayActivities": {
      "visits": 15,
      "calls": 45,
      "followUps": 68
    }
  }
}
```

---

## 10. 通知 API

```
GET    /v1/notifications                    -- 通知列表
GET    /v1/notifications/unread-count       -- 未读数量
PUT    /v1/notifications/{id}/read          -- 标记已读
PUT    /v1/notifications/read-all           -- 全部标记已读
```

---

## 11. 文件上传 API

```
POST   /v1/files/upload                     -- 通用文件上传
POST   /v1/files/upload/image               -- 图片上传（自动压缩）
POST   /v1/files/upload/recording           -- 录音文件上传
GET    /v1/files/{id}/download              -- 文件下载
GET    /v1/files/{id}/presigned-url          -- 获取预签名URL
```

---

## 12. 组织与权限 API

### 12.1 组织架构（管理端）

```
GET    /v1/organizations/tree               -- 组织架构树
POST   /v1/organizations                    -- 创建组织
PUT    /v1/organizations/{id}               -- 更新组织
DELETE /v1/organizations/{id}               -- 删除组织
GET    /v1/organizations/{id}/members       -- 组织成员列表
```

### 12.2 角色权限（管理端）

```
GET    /v1/roles                            -- 角色列表
POST   /v1/roles                            -- 创建角色
PUT    /v1/roles/{id}                       -- 更新角色
DELETE /v1/roles/{id}                       -- 删除角色
GET    /v1/roles/{id}/permissions           -- 角色权限列表
PUT    /v1/roles/{id}/permissions           -- 更新角色权限
POST   /v1/users/{id}/roles                -- 分配用户角色
```

---

## 13. 租户管理 API（平台管理端）

```
GET    /v1/admin/tenants                    -- 租户列表
POST   /v1/admin/tenants                    -- 创建租户
PUT    /v1/admin/tenants/{id}               -- 更新租户
PUT    /v1/admin/tenants/{id}/status        -- 启用/禁用租户
GET    /v1/admin/tenants/{id}/statistics    -- 租户使用统计
```

---

## 14. 服务间内部 API (gRPC/Feign)

这些 API 仅用于微服务之间的内部调用，不对外暴露：

```
// 数据权限查询（org-svc → 各业务服务）
DataScope getUserDataScope(Long userId, String permissionCode)
List<Long> getSubordinateUserIds(Long userId)
String getUserOrgPath(Long userId)

// 客户信息查询（customer-svc → 其他服务）
CustomerDTO getCustomerById(Long customerId)
List<CustomerDTO> getCustomersByIds(List<Long> customerIds)

// 录音分析（ai-svc）
TranscriptionResult transcribeRecording(String fileUrl)
AnalysisResult analyzeContent(String text, Long tenantId)
List<ViolationItem> checkViolations(String text, Long tenantId)

// 消息通知（notify-svc）
void sendNotification(NotificationDTO notification)
void sendBatchNotifications(List<NotificationDTO> notifications)

// 文件服务（file-svc）
FileInfo uploadFile(MultipartFile file, String bucket)
String getPresignedUrl(Long fileId, int expireMinutes)
```
