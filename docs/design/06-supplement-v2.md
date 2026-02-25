# AiCRM 补充设计文档（V2 需求）

本文档涵盖 9 项新增需求的完整设计方案。

---

## 需求清单

| 编号 | 需求 | 涉及模块 |
|---|---|---|
| S1 | 销售员行动轨迹（地图展示） | 外勤服务、APP、PC |
| S2 | 五度易链企业信息查询 + 平台企业标识 | 客户服务、第三方集成 |
| S3 | 销售员数据报表 | 报表服务 |
| S4 | 客户360视图增加ERP欠款与往来明细 | ERP集成、客户服务 |
| S5 | 客户列表显示即将采购日期/天数 | 客户服务 |
| S6 | 客户黑名单与无效客户管理 | 客户服务 |
| S7 | 活动管理（二维码 + 参与人员） | 新增活动服务 |
| S8 | 活动扫码 + 企业微信好友/平台好友 | 活动服务、好友服务 |
| S9 | 好友管理 | 新增好友服务 |

---

## S1. 销售员行动轨迹

### S1.1 设计思路

通过采集销售员的打卡签到记录中的 GPS 坐标，按时间顺序连线形成行动轨迹，在地图上展示，支持按日期查看轨迹。管理层可查看下属员工的轨迹。

### S1.2 数据采集

已有数据源（无需新增采集，直接复用）：
- `checkin_record` 表中的经纬度、时间
- `visit_record` 表中的签到经纬度、时间

为提高轨迹精度，新增**位置上报表**（APP 后台定期上报位置）：

```sql
CREATE TABLE `location_report` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `user_id`           BIGINT       NOT NULL COMMENT '用户ID',
  `longitude`         DECIMAL(10,7) NOT NULL COMMENT '经度',
  `latitude`          DECIMAL(10,7) NOT NULL COMMENT '纬度',
  `accuracy`          FLOAT        DEFAULT NULL COMMENT '精度（米）',
  `address`           VARCHAR(512) DEFAULT NULL COMMENT '地址（反向地理编码）',
  `report_time`       DATETIME     NOT NULL COMMENT '上报时间',
  `report_type`       TINYINT      NOT NULL DEFAULT 1 COMMENT '类型（1-定时上报 2-打卡关联 3-拜访关联）',
  `related_biz_type`  VARCHAR(32)  DEFAULT NULL COMMENT '关联业务类型（checkin/visit）',
  `related_biz_id`    BIGINT       DEFAULT NULL COMMENT '关联业务ID',
  `battery_level`     INT          DEFAULT NULL COMMENT '电池电量（%）',
  `network_type`      VARCHAR(16)  DEFAULT NULL COMMENT '网络类型（wifi/4g/5g）',
  `device_info`       VARCHAR(256) DEFAULT NULL COMMENT '设备信息',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user_time` (`tenant_id`, `user_id`, `report_time`),
  KEY `idx_report_time` (`tenant_id`, `report_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='位置上报表';
```

### S1.3 轨迹 API

```
GET    /v1/trajectories                           -- 轨迹查询
GET    /v1/trajectories/{userId}/daily             -- 某用户某日轨迹
GET    /v1/trajectories/team                       -- 团队轨迹（管理层）
```

**轨迹查询响应**：
```json
GET /v1/trajectories/{userId}/daily?date=2026-02-25
{
  "code": 200,
  "data": {
    "userId": 101,
    "userName": "李明",
    "date": "2026-02-25",
    "totalDistance": 45.2,
    "totalStops": 5,
    "startTime": "2026-02-25T08:30:00",
    "endTime": "2026-02-25T18:15:00",
    "points": [
      {
        "longitude": 113.9306,
        "latitude": 22.5429,
        "time": "2026-02-25T08:30:00",
        "address": "深圳市南山区科技园",
        "type": "checkin",
        "label": "外出打卡",
        "stayDuration": null
      },
      {
        "longitude": 113.9420,
        "latitude": 22.5510,
        "time": "2026-02-25T09:15:00",
        "address": "深圳市南山区XX大厦",
        "type": "location",
        "label": "途经",
        "stayDuration": null
      },
      {
        "longitude": 113.9580,
        "latitude": 22.5630,
        "time": "2026-02-25T10:00:00",
        "address": "深圳市福田区XX科技园B栋",
        "type": "visit_checkin",
        "label": "拜访签到 - 示例科技",
        "stayDuration": 90,
        "customerName": "示例科技有限公司",
        "visitId": 7001
      }
    ],
    "visitSummary": [
      {
        "customerName": "示例科技有限公司",
        "address": "福田区XX科技园B栋",
        "checkinTime": "10:00",
        "checkoutTime": "11:30",
        "stayMinutes": 90
      }
    ]
  }
}
```

### S1.4 地图展示方案

```
┌───────────────────────────────────────────────────┐
│   APP 轨迹页面                                      │
│                                                     │
│   ← 行动轨迹              [2026-02-25 ▼]          │
│                                                     │
│   ┌─────────────────────────────────────────────┐  │
│   │                                             │  │
│   │   🗺️ 高德地图                                │  │
│   │                                             │  │
│   │     🟢 08:30 外出打卡                        │  │
│   │      │                                      │  │
│   │      ⋮ (轨迹连线)                            │  │
│   │      │                                      │  │
│   │     🔴 10:00 拜访签到(示例科技)               │  │
│   │      │  停留 90min                           │  │
│   │      ⋮                                      │  │
│   │      │                                      │  │
│   │     🔴 14:00 拜访签到(XX公司)                 │  │
│   │      │  停留 60min                           │  │
│   │      ⋮                                      │  │
│   │     🟢 18:15 返回打卡                        │  │
│   │                                             │  │
│   └─────────────────────────────────────────────┘  │
│                                                     │
│   📊 今日统计                                       │
│   行驶距离: 45.2km  拜访客户: 3家  停留总时长: 4.5h  │
│                                                     │
│   📋 停留点明细                                      │
│   1. 10:00~11:30 示例科技(90min)  ▶                │
│   2. 14:00~15:00 XX公司(60min)    ▶                │
│   3. 16:00~17:30 YY科技(90min)    ▶                │
│                                                     │
└───────────────────────────────────────────────────┘
```

### S1.5 位置上报策略

```
┌──────────────────────────────────────────────────────┐
│                APP 后台位置上报策略                     │
│                                                        │
│  外勤模式开启后：                                      │
│  ├── 上报频率：每 5 分钟上报一次 GPS 位置              │
│  ├── 省电策略：静止超过 10 分钟，降低为 15 分钟一次     │
│  ├── 移动检测：检测到明显位移后恢复 5 分钟频率          │
│  ├── 本地缓存：无网时本地缓存，有网后批量上传           │
│  └── 隐私合规：用户可关闭（但管理员可设置强制开启）      │
│                                                        │
│  数据清理策略：                                        │
│  ├── 原始位置数据保留 3 个月                           │
│  ├── 3 个月后仅保留打卡和拜访关联的关键点               │
│  └── 轨迹摘要数据永久保留                              │
└──────────────────────────────────────────────────────┘
```

---

## S2. 五度易链企业信息查询

### S2.1 设计思路

集成五度易链（wdyl.com）开放 API，在 CRM 中可查询企业工商信息、风险信息等。查询结果需标注该企业是否已是 CRM 平台中某租户的客户。

### S2.2 集成架构

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│  CRM 前端    │────────▶│ aicrm-       │────────▶│  五度易链     │
│  (搜索企业)  │         │ customer-svc │         │  Open API    │
│              │◀────────│              │◀────────│              │
└──────────────┘         │              │         └──────────────┘
                         │  本地匹配     │
                         │  客户表查重   │
                         └──────────────┘
```

### S2.3 五度易链配置表 (third_party_config)

```sql
CREATE TABLE `third_party_config` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `provider`          VARCHAR(32)  NOT NULL COMMENT '服务商（wdyl/tianyancha/qichacha）',
  `api_base_url`      VARCHAR(512) NOT NULL COMMENT 'API基础地址',
  `app_key`           VARCHAR(256) NOT NULL COMMENT 'AppKey（加密存储）',
  `app_secret`        VARCHAR(256) NOT NULL COMMENT 'AppSecret（加密存储）',
  `daily_quota`       INT          NOT NULL DEFAULT 100 COMMENT '每日查询配额',
  `daily_used`        INT          NOT NULL DEFAULT 0 COMMENT '今日已用次数',
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_provider` (`tenant_id`, `provider`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方服务配置表';
```

### S2.4 企业查询缓存表 (enterprise_query_cache)

```sql
CREATE TABLE `enterprise_query_cache` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `company_name`      VARCHAR(256) NOT NULL COMMENT '企业名称',
  `credit_code`       VARCHAR(32)  DEFAULT NULL COMMENT '统一社会信用代码',
  `legal_person`      VARCHAR(64)  DEFAULT NULL COMMENT '法定代表人',
  `registered_capital` VARCHAR(64) DEFAULT NULL COMMENT '注册资本',
  `established_date`  DATE         DEFAULT NULL COMMENT '成立日期',
  `company_status`    VARCHAR(32)  DEFAULT NULL COMMENT '经营状态',
  `company_type`      VARCHAR(64)  DEFAULT NULL COMMENT '企业类型',
  `industry`          VARCHAR(128) DEFAULT NULL COMMENT '行业',
  `province`          VARCHAR(32)  DEFAULT NULL COMMENT '省份',
  `city`              VARCHAR(32)  DEFAULT NULL COMMENT '城市',
  `address`           VARCHAR(512) DEFAULT NULL COMMENT '注册地址',
  `business_scope`    TEXT         DEFAULT NULL COMMENT '经营范围',
  `contact_phone`     VARCHAR(64)  DEFAULT NULL COMMENT '联系电话',
  `contact_email`     VARCHAR(128) DEFAULT NULL COMMENT '联系邮箱',
  `website`           VARCHAR(256) DEFAULT NULL COMMENT '官网',
  `risk_info`         TEXT         DEFAULT NULL COMMENT '风险信息（JSON）',
  `raw_data`          JSON         DEFAULT NULL COMMENT '原始响应数据',
  `source`            VARCHAR(32)  NOT NULL DEFAULT 'wdyl' COMMENT '数据来源',
  `is_platform_customer` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已是平台客户',
  `matched_customer_id` BIGINT     DEFAULT NULL COMMENT '匹配到的客户ID',
  `matched_tenant_id`   BIGINT     DEFAULT NULL COMMENT '匹配到的租户ID',
  `query_time`        DATETIME     NOT NULL COMMENT '查询时间',
  `expire_time`       DATETIME     NOT NULL COMMENT '缓存过期时间',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_company` (`tenant_id`, `company_name`),
  KEY `idx_credit_code` (`credit_code`),
  KEY `idx_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业信息查询缓存表';
```

### S2.5 企业查询 API

```
GET    /v1/enterprise/search                      -- 搜索企业（五度易链）
GET    /v1/enterprise/{creditCode}                -- 企业详情
GET    /v1/enterprise/{creditCode}/risk           -- 企业风险信息
POST   /v1/enterprise/{creditCode}/import         -- 将企业导入为客户/线索
```

**企业搜索响应**：
```json
GET /v1/enterprise/search?keyword=示例科技
{
  "code": 200,
  "data": {
    "records": [
      {
        "companyName": "深圳示例科技有限公司",
        "creditCode": "91440300MA5XXXXXX",
        "legalPerson": "张三",
        "registeredCapital": "5000万元",
        "establishedDate": "2018-05-10",
        "companyStatus": "存续",
        "industry": "软件和信息技术服务业",
        "province": "广东",
        "city": "深圳",
        "isPlatformCustomer": true,
        "platformCustomerInfo": {
          "customerId": 5001,
          "customerName": "深圳示例科技有限公司",
          "ownerUserName": "李明",
          "lifecycleStage": "活跃客户"
        }
      },
      {
        "companyName": "北京示例科技股份有限公司",
        "creditCode": "91110000MA0XXXXXX",
        "legalPerson": "李四",
        "registeredCapital": "1亿元",
        "isPlatformCustomer": false,
        "platformCustomerInfo": null
      }
    ],
    "total": 2
  }
}
```

### S2.6 平台企业匹配逻辑

```
查询五度易链返回结果后：

1. 对每条结果，用统一社会信用代码匹配 customer 表
2. 若无信用代码，用企业名称模糊匹配
3. 标注匹配状态：
   ├── isPlatformCustomer: true  → 已是本租户客户
   ├── isPlatformCustomer: false → 不是本租户客户
   └── 展示所属销售员信息（数据权限内可见）

4. 可直接将查询到的企业一键导入为线索或客户
```

### S2.7 页面设计

```
PC 端 - 企业查询页面
┌───────────────────────────────────────────────────────────┐
│  企业信息查询                                               │
│                                                             │
│  🔍 [________________________] [搜索]                     │
│     输入企业名称或统一社会信用代码                            │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  深圳示例科技有限公司                                  │  │
│  │  统一社会信用代码: 91440300MA5XXXXXX                   │  │
│  │  法人: 张三  |  注册资本: 5000万  |  成立: 2018-05-10  │  │
│  │  状态: 存续  |  行业: 软件和信息技术服务业              │  │
│  │  🟢 已是平台客户 → 负责人: 李明 (华南销售部)           │  │
│  │  [查看客户详情]                                       │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │  北京示例科技股份有限公司                               │  │
│  │  统一社会信用代码: 91110000MA0XXXXXX                   │  │
│  │  法人: 李四  |  注册资本: 1亿  |  成立: 2015-03-20     │  │
│  │  状态: 存续  |  行业: 互联网和相关服务                  │  │
│  │  ⚪ 非平台客户                                        │  │
│  │  [导入为线索]  [导入为客户]                             │  │
│  └──────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘
```

---

## S3. 销售员数据报表

### S3.1 报表维度

```
┌──────────────────────────────────────────────────────────┐
│                 销售员数据报表体系                          │
│                                                            │
│  一、个人维度报表（销售员自己看）                            │
│  ├── 业绩看板：本月/本季/本年 成交额/目标完成率             │
│  ├── 客户统计：总客户数/新增客户/跟进客户/流失客户          │
│  ├── 线索统计：获取线索数/转化数/转化率                     │
│  ├── 商机统计：活跃商机数/赢单数/输单数/金额                │
│  ├── 跟进统计：跟进次数/每日平均/跟进方式分布               │
│  ├── 拜访统计：拜访次数/拜访方式分布/平均拜访时长           │
│  └── 违规统计：违规次数/违规词TOP/改善趋势                  │
│                                                            │
│  二、管理维度报表（经理/总监看）                             │
│  ├── 团队业绩排名：成交额/客户数/跟进次数排名               │
│  ├── 团队行动力报告：每人拜访数/跟进数/通话时长             │
│  ├── 团队转化分析：各成员线索→客户→成交转化漏斗             │
│  ├── 团队轨迹概览：今日各成员外出情况/位置分布              │
│  └── 异常预警：长期未跟进客户/线索积压/商机停滞             │
│                                                            │
│  三、运营维度报表                                           │
│  ├── 线索分配效率：分配后响应时间/跟进率                    │
│  ├── 渠道ROI：各来源线索的转化率对比                        │
│  └── 线索池健康度：池中线索数量/周转率                      │
└──────────────────────────────────────────────────────────┘
```

### S3.2 报表 API

```
GET    /v1/reports/sales/personal                  -- 个人业绩报表
GET    /v1/reports/sales/team                      -- 团队业绩报表（管理层）
GET    /v1/reports/sales/ranking                   -- 销售排行榜
GET    /v1/reports/sales/{userId}/detail            -- 某销售员详细报表
GET    /v1/reports/sales/action                    -- 行动力报告
GET    /v1/reports/sales/conversion-funnel          -- 转化漏斗
GET    /v1/reports/sales/anomaly                   -- 异常预警列表
GET    /v1/reports/sales/trend                     -- 趋势分析（折线图数据）
```

**个人业绩报表响应**：
```json
GET /v1/reports/sales/personal?period=2026-02
{
  "code": 200,
  "data": {
    "period": "2026-02",
    "performance": {
      "dealAmount": 850000,
      "dealTarget": 1000000,
      "completionRate": 85.0,
      "dealCount": 5,
      "newCustomers": 12,
      "newLeads": 25,
      "leadConversionRate": 32.0
    },
    "activity": {
      "totalFollowUps": 68,
      "dailyAvgFollowUps": 3.4,
      "totalVisits": 15,
      "onsiteVisits": 8,
      "phoneVisits": 5,
      "wechatVisits": 2,
      "totalCallDuration": 4520,
      "totalCallCount": 45
    },
    "pipeline": {
      "activeOpportunities": 8,
      "pipelineValue": 3500000,
      "wonThisMonth": 2,
      "wonAmount": 850000,
      "lostThisMonth": 1,
      "lostAmount": 200000
    },
    "violations": {
      "totalViolations": 3,
      "violationTrend": "decreasing",
      "lastViolationDate": "2026-02-18"
    },
    "comparison": {
      "vsLastMonth": {
        "dealAmountChange": 15.2,
        "followUpChange": 8.5,
        "visitChange": -5.0
      }
    }
  }
}
```

**团队行动力报告响应**：
```json
GET /v1/reports/sales/action?period=2026-02&orgId=301
{
  "code": 200,
  "data": {
    "members": [
      {
        "userId": 101,
        "userName": "李明",
        "followUps": 68,
        "visits": 15,
        "callDuration": 4520,
        "newCustomers": 12,
        "dealAmount": 850000,
        "avgResponseTime": 2.5,
        "overdueLeads": 0,
        "lastActiveTime": "2026-02-25T17:30:00"
      },
      {
        "userId": 102,
        "userName": "王芳",
        "followUps": 42,
        "visits": 8,
        "callDuration": 2100,
        "newCustomers": 6,
        "dealAmount": 450000,
        "avgResponseTime": 5.2,
        "overdueLeads": 3,
        "lastActiveTime": "2026-02-25T16:00:00"
      }
    ]
  }
}
```

---

## S4. 客户360视图 - ERP 欠款与往来明细

### S4.1 设计思路

在客户 360° 视图中，通过调用 ERP 的 API 接口实时获取客户的应收账款（欠款）和往来明细数据。这些数据不存储在 CRM 中，每次打开客户详情时实时从 ERP 拉取。

### S4.2 ERP 财务数据 API

CRM 调用 ERP 侧接口，需在 ERP 适配层中增加以下接口映射：

```
GET    /v1/customers/{id}/erp-receivables          -- 客户ERP应收账款
GET    /v1/customers/{id}/erp-transactions          -- 客户ERP往来明细
```

**应收账款响应**：
```json
GET /v1/customers/{id}/erp-receivables
{
  "code": 200,
  "data": {
    "erpCustomerId": "ERP-C001",
    "customerName": "示例科技有限公司",
    "summary": {
      "totalReceivable": 350000.00,
      "overdueAmount": 120000.00,
      "currentAmount": 230000.00,
      "creditLimit": 500000.00,
      "creditUsed": 350000.00,
      "creditAvailable": 150000.00
    },
    "agingAnalysis": [
      { "period": "0-30天", "amount": 150000.00 },
      { "period": "31-60天", "amount": 80000.00 },
      { "period": "61-90天", "amount": 70000.00 },
      { "period": "90天以上", "amount": 50000.00 }
    ],
    "lastPaymentDate": "2026-02-10",
    "lastPaymentAmount": 50000.00
  }
}
```

**往来明细响应**：
```json
GET /v1/customers/{id}/erp-transactions?pageNum=1&pageSize=20
{
  "code": 200,
  "data": {
    "records": [
      {
        "transactionId": "TXN-20260220-001",
        "transactionType": "销售出库",
        "documentNo": "SO-2026-0215",
        "transactionDate": "2026-02-20",
        "debitAmount": 180000.00,
        "creditAmount": 0,
        "balance": 350000.00,
        "remark": "2月第二批货款"
      },
      {
        "transactionId": "TXN-20260210-003",
        "transactionType": "收款",
        "documentNo": "REC-2026-0105",
        "transactionDate": "2026-02-10",
        "debitAmount": 0,
        "creditAmount": 50000.00,
        "balance": 170000.00,
        "remark": "银行转账"
      }
    ],
    "total": 25
  }
}
```

### S4.3 更新客户360°视图

在原有 `GET /v1/customers/{id}` 响应中增加 ERP 财务数据：

```json
{
  "data": {
    "customer": { ... },
    "contacts": [ ... ],
    "recentFollowUps": [ ... ],
    "opportunities": [ ... ],
    "visitRecords": [ ... ],
    "erpSyncInfo": { ... },
    "erpFinancial": {
      "totalReceivable": 350000.00,
      "overdueAmount": 120000.00,
      "creditLimit": 500000.00,
      "creditAvailable": 150000.00,
      "agingAnalysis": [ ... ],
      "recentTransactions": [ ... ]
    },
    "statistics": { ... }
  }
}
```

### S4.4 客户详情 - 财务Tab页面设计

```
┌───────────────────────────────────────────────────┐
│  客户详情 - 示例科技有限公司                         │
│                                                     │
│  [基本信息] [联系人] [跟进] [商机] [财务] [拜访]     │
│                                    ^^^^              │
│  ─────────── 财务信息（来自ERP） ───────────        │
│                                                     │
│  💰 应收账款概览                                    │
│  ┌──────────┬──────────┬──────────┐                │
│  │  应收总额  │  逾期金额  │  信用余额  │                │
│  │ ¥350,000 │ ¥120,000 │ ¥150,000 │                │
│  │          │  🔴       │          │                │
│  └──────────┴──────────┴──────────┘                │
│                                                     │
│  📊 账龄分析                                        │
│  0-30天    ████████████████  ¥150,000              │
│  31-60天   ████████          ¥80,000               │
│  61-90天   ███████           ¥70,000               │
│  90天以上  █████             ¥50,000  ⚠️           │
│                                                     │
│  📋 往来明细                                        │
│  ┌────────┬────────┬────────┬──────┬──────┐       │
│  │  日期   │  类型   │  单据号  │ 借方  │ 贷方  │       │
│  ├────────┼────────┼────────┼──────┼──────┤       │
│  │ 02-20  │ 销售出库│SO-0215 │18万  │  -   │       │
│  │ 02-10  │ 收款   │REC-0105│  -   │ 5万  │       │
│  │ 01-15  │ 销售出库│SO-0108 │12万  │  -   │       │
│  └────────┴────────┴────────┴──────┴──────┘       │
│                              [查看更多 >]           │
└───────────────────────────────────────────────────┘
```

---

## S5. 客户列表 - 即将采购日期/天数

### S5.1 设计思路

客户表增加 `expected_purchase_date`（预计采购日期）字段。列表展示时：
- 如果未过期，显示倒计时天数（如 "还剩 7 天"）
- 如果已过期，显示具体日期和过期天数（如 "已过期 3 天 (2026-02-22)"）

### S5.2 客户表新增字段

```sql
ALTER TABLE `customer` ADD COLUMN `expected_purchase_date` DATE DEFAULT NULL
  COMMENT '预计采购日期' AFTER `next_follow_time`;

ALTER TABLE `customer` ADD KEY `idx_tenant_purchase_date` (`tenant_id`, `expected_purchase_date`);
```

### S5.3 客户列表 API 响应更新

```json
GET /v1/customers?pageNum=1&pageSize=20
{
  "data": {
    "records": [
      {
        "id": 5001,
        "customerName": "示例科技有限公司",
        "ownerUserName": "李明",
        "lifecycleStage": 3,
        "expectedPurchaseDate": "2026-03-05",
        "purchaseCountdown": {
          "status": "upcoming",
          "daysRemaining": 8,
          "displayText": "还剩 8 天"
        },
        "lastFollowTime": "2026-02-25T14:00:00"
      },
      {
        "id": 5002,
        "customerName": "XX有限公司",
        "ownerUserName": "王芳",
        "lifecycleStage": 2,
        "expectedPurchaseDate": "2026-02-20",
        "purchaseCountdown": {
          "status": "overdue",
          "daysOverdue": 5,
          "displayText": "已过期 5 天",
          "overdueDate": "2026-02-20"
        },
        "lastFollowTime": "2026-02-18T10:00:00"
      },
      {
        "id": 5003,
        "customerName": "YY科技集团",
        "expectedPurchaseDate": null,
        "purchaseCountdown": null
      }
    ]
  }
}
```

### S5.4 列表新增筛选参数

```
GET /v1/customers
    &purchaseStatus=upcoming          -- 即将采购（未过期）
    &purchaseStatus=overdue           -- 已过期
    &purchaseDaysWithin=7             -- 7天内即将采购
    &purchaseOverdueDays=30           -- 过期超过30天
    &sortField=expectedPurchaseDate
    &sortOrder=asc
```

### S5.5 列表展示设计

```
客户列表中的采购日期列：

┌──────────────────────────────────────────────────────────┐
│ 客户名称            负责人   阶段     采购日期           │
├──────────────────────────────────────────────────────────┤
│ 示例科技有限公司     李明    成交客户  🟢 还剩 8 天       │
│ XX有限公司           王芳    意向客户  🟡 还剩 2 天       │
│ YY贸易公司           张强    成交客户  🔴 已过期 5 天     │
│                                       (2026-02-20)      │
│ ZZ集团               赵六    潜在客户  —                  │
└──────────────────────────────────────────────────────────┘
```

---

## S6. 客户黑名单与无效客户管理

### S6.1 设计思路

- **黑名单客户**：有欺诈、恶意行为等，禁止所有销售人员接触，不可被线索转化匹配
- **无效客户**：经验证确认无价值（注销企业、联系方式全部无效等），标记后不再显示在常规列表

### S6.2 客户表更新

在现有 `customer` 表中更新 `lifecycle_stage` 增加两个状态：

```
lifecycle_stage 扩展：
原有：1-潜在 2-意向 3-成交 4-活跃 5-VIP 6-流失
新增：7-无效 8-黑名单
```

### S6.3 客户黑名单表 (customer_blacklist)

```sql
CREATE TABLE `customer_blacklist` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `customer_id`       BIGINT       DEFAULT NULL COMMENT '关联客户ID（已有客户加黑时）',
  `company_name`      VARCHAR(256) NOT NULL COMMENT '企业名称',
  `credit_code`       VARCHAR(32)  DEFAULT NULL COMMENT '统一社会信用代码',
  `contact_phone`     VARCHAR(32)  DEFAULT NULL COMMENT '联系电话',
  `blacklist_type`    TINYINT      NOT NULL COMMENT '类型（1-欺诈 2-恶意投诉 3-空壳公司 4-同行竞对 5-其他）',
  `reason`            TEXT         NOT NULL COMMENT '拉黑原因',
  `evidence_urls`     TEXT         DEFAULT NULL COMMENT '证据材料URL（JSON数组）',
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（1-生效中 2-已解除）',
  `operated_by`       BIGINT       NOT NULL COMMENT '操作人',
  `release_by`        BIGINT       DEFAULT NULL COMMENT '解除人',
  `release_time`      DATETIME     DEFAULT NULL COMMENT '解除时间',
  `release_reason`    VARCHAR(512) DEFAULT NULL COMMENT '解除原因',
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_tenant_company` (`tenant_id`, `company_name`),
  KEY `idx_credit_code` (`tenant_id`, `credit_code`),
  KEY `idx_phone` (`tenant_id`, `contact_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户黑名单表';
```

### S6.4 黑名单/无效客户 API

```
POST   /v1/customers/{id}/blacklist                -- 加入黑名单
POST   /v1/customers/{id}/blacklist/release         -- 解除黑名单
GET    /v1/customers/blacklist                      -- 黑名单列表
POST   /v1/customers/{id}/mark-invalid              -- 标记为无效客户
POST   /v1/customers/{id}/reactivate                -- 重新激活（从无效恢复）
GET    /v1/customers/invalid                        -- 无效客户列表
```

**加入黑名单请求**：
```json
POST /v1/customers/{id}/blacklist
{
  "blacklistType": 1,
  "reason": "该客户多次诈骗，已报警",
  "evidenceUrls": [
    "https://oss.example.com/evidence/001.jpg",
    "https://oss.example.com/evidence/002.pdf"
  ]
}
```

### S6.5 黑名单拦截逻辑

```
┌──────────────────────────────────────────────────────────┐
│                 黑名单拦截规则                             │
│                                                            │
│  1. 新建线索时：                                          │
│     ├── 匹配手机号 → 命中黑名单则拒绝并提示               │
│     └── 匹配公司名/信用代码 → 命中黑名单则警告            │
│                                                            │
│  2. 线索转客户时：                                        │
│     └── 匹配黑名单 → 阻止转化并提示原因                   │
│                                                            │
│  3. 客户列表：                                            │
│     ├── 黑名单客户默认不显示在常规列表                     │
│     ├── 需切换到"黑名单"Tab查看                           │
│     └── 黑名单客户行上显示 🚫 标识                        │
│                                                            │
│  4. 无效客户：                                            │
│     ├── 无效客户默认不显示在常规列表                       │
│     ├── 需切换到"无效客户"Tab查看                         │
│     └── 可随时重新激活                                    │
│                                                            │
│  5. 黑名单为租户级共享，所有销售员均受约束                  │
└──────────────────────────────────────────────────────────┘
```

---

## S7. 活动管理

### S7.1 设计思路

支持创建营销活动，每个活动自动生成唯一二维码。二维码可用于线下推广，客户扫码后触发加好友流程（详见 S8）。活动支持参与人员管理和参与数据统计。

### S7.2 活动表 (activity)

```sql
CREATE TABLE `activity` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `activity_no`       VARCHAR(32)  NOT NULL COMMENT '活动编号',
  `activity_name`     VARCHAR(256) NOT NULL COMMENT '活动名称',
  `activity_type`     TINYINT      NOT NULL COMMENT '类型（1-线下展会 2-线上推广 3-产品发布 4-客户沙龙 5-培训会议 6-其他）',
  `description`       TEXT         DEFAULT NULL COMMENT '活动描述',
  `cover_image_url`   VARCHAR(1024) DEFAULT NULL COMMENT '封面图片URL',
  `start_time`        DATETIME     NOT NULL COMMENT '活动开始时间',
  `end_time`          DATETIME     NOT NULL COMMENT '活动结束时间',
  `location`          VARCHAR(512) DEFAULT NULL COMMENT '活动地点',
  `longitude`         DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `latitude`          DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `online_url`        VARCHAR(1024) DEFAULT NULL COMMENT '线上活动链接',
  `max_participants`  INT          DEFAULT NULL COMMENT '最大参与人数（NULL=不限）',
  `current_participants` INT       NOT NULL DEFAULT 0 COMMENT '当前参与人数',
  `qr_code_url`       VARCHAR(1024) NOT NULL COMMENT '活动二维码URL',
  `qr_code_content`   VARCHAR(512) NOT NULL COMMENT '二维码内容（跳转链接）',
  `landing_page_url`  VARCHAR(1024) DEFAULT NULL COMMENT '落地页URL',
  `status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0-草稿 1-未开始 2-进行中 3-已结束 4-已取消）',
  `owner_user_id`     BIGINT       NOT NULL COMMENT '活动负责人',
  `owner_org_id`      BIGINT       DEFAULT NULL COMMENT '负责人所属组织',
  `budget`            DECIMAL(15,2) DEFAULT NULL COMMENT '活动预算',
  `actual_cost`       DECIMAL(15,2) DEFAULT NULL COMMENT '实际费用',
  `wechat_work_bind`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否绑定企业微信',
  `wechat_work_qr_url` VARCHAR(1024) DEFAULT NULL COMMENT '企业微信活码URL',
  `remark`            TEXT         DEFAULT NULL,
  `ext_attrs`         JSON         DEFAULT NULL COMMENT '扩展属性',
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  `version`           INT          NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_activity_no` (`tenant_id`, `activity_no`),
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_tenant_time` (`tenant_id`, `start_time`),
  KEY `idx_tenant_owner` (`tenant_id`, `owner_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';
```

### S7.3 活动参与人表 (activity_participant)

```sql
CREATE TABLE `activity_participant` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `activity_id`       BIGINT       NOT NULL COMMENT '活动ID',
  `participant_name`  VARCHAR(64)  NOT NULL COMMENT '参与人姓名',
  `participant_phone` VARCHAR(32)  DEFAULT NULL COMMENT '参与人电话',
  `participant_email` VARCHAR(128) DEFAULT NULL COMMENT '参与人邮箱',
  `company_name`      VARCHAR(256) DEFAULT NULL COMMENT '公司名称',
  `position`          VARCHAR(64)  DEFAULT NULL COMMENT '职位',
  `source`            TINYINT      NOT NULL DEFAULT 1 COMMENT '来源（1-扫码报名 2-手动录入 3-批量导入）',
  `scan_time`         DATETIME     DEFAULT NULL COMMENT '扫码时间',
  `checkin_status`    TINYINT      NOT NULL DEFAULT 0 COMMENT '签到状态（0-未签到 1-已签到）',
  `checkin_time`      DATETIME     DEFAULT NULL COMMENT '签到时间',
  `is_customer`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已是客户',
  `customer_id`       BIGINT       DEFAULT NULL COMMENT '关联客户ID',
  `is_lead`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已转为线索',
  `lead_id`           BIGINT       DEFAULT NULL COMMENT '关联线索ID',
  `friend_added`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已加好友',
  `friend_id`         BIGINT       DEFAULT NULL COMMENT '关联好友ID',
  `wechat_added`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已加企业微信',
  `feedback`          TEXT         DEFAULT NULL COMMENT '反馈/备注',
  `remark`            VARCHAR(512) DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_activity` (`tenant_id`, `activity_id`),
  KEY `idx_tenant_phone` (`tenant_id`, `participant_phone`),
  KEY `idx_activity_source` (`tenant_id`, `activity_id`, `source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动参与人表';
```

### S7.4 活动管理 API

```
POST   /v1/activities                              -- 创建活动
GET    /v1/activities                              -- 活动列表
GET    /v1/activities/{id}                         -- 活动详情
PUT    /v1/activities/{id}                         -- 更新活动
DELETE /v1/activities/{id}                         -- 删除活动
PUT    /v1/activities/{id}/status                  -- 更新活动状态
GET    /v1/activities/{id}/qrcode                  -- 获取/刷新活动二维码
GET    /v1/activities/{id}/qrcode/download          -- 下载二维码图片

POST   /v1/activities/{id}/participants             -- 添加参与人
GET    /v1/activities/{id}/participants             -- 参与人列表
PUT    /v1/activities/{id}/participants/{pid}       -- 更新参与人信息
DELETE /v1/activities/{id}/participants/{pid}       -- 删除参与人
POST   /v1/activities/{id}/participants/import      -- 批量导入参与人
POST   /v1/activities/{id}/participants/{pid}/checkin -- 参与人签到
POST   /v1/activities/{id}/participants/{pid}/convert -- 转为线索/客户

GET    /v1/activities/{id}/statistics               -- 活动数据统计
GET    /v1/activities/summary                       -- 活动汇总统计
```

**活动扫码公开 API（无需鉴权）**：
```
GET    /v1/open/activities/{activityNo}/info        -- 活动信息（扫码后展示）
POST   /v1/open/activities/{activityNo}/register    -- 扫码报名
```

**创建活动请求**：
```json
POST /v1/activities
{
  "activityName": "2026春季产品发布会",
  "activityType": 3,
  "description": "新一代ERP产品线上发布会",
  "startTime": "2026-03-15T14:00:00",
  "endTime": "2026-03-15T17:00:00",
  "location": "深圳市南山区XX酒店三楼会议厅",
  "maxParticipants": 200,
  "budget": 50000,
  "wechatWorkBind": true
}
```

### S7.5 二维码生成方案

```
┌──────────────────────────────────────────────────┐
│              活动二维码生成策略                     │
│                                                    │
│  二维码内容格式：                                  │
│  https://crm.example.com/a/{activityNo}           │
│                                                    │
│  扫码流程：                                        │
│  1. 用户扫码 → 打开落地页（H5页面）               │
│  2. 落地页展示活动信息 + 报名表单                  │
│  3. 用户填写姓名、电话、公司等信息                 │
│  4. 提交后：                                       │
│     ├── 创建活动参与人记录                         │
│     ├── 自动匹配是否已是平台客户                    │
│     ├── 如租户绑定了企业微信 →                      │
│     │   同时展示企业微信活码（详见S8）              │
│     └── 否则展示平台好友二维码                      │
│                                                    │
│  二维码样式：                                      │
│  ├── 中心Logo: 活动封面或租户Logo                  │
│  ├── 底部文字: 活动名称                            │
│  └── 尺寸: 支持下载 300/600/1200px                 │
└──────────────────────────────────────────────────┘
```

---

## S8. 活动扫码 + 企业微信/平台好友

### S8.1 扫码后的好友添加流程

```
客户扫活动二维码
        │
        ▼
┌───────────────┐
│ 打开活动落地页 │
│ 填写报名信息   │
└───────┬───────┘
        │
        ▼
┌───────────────┐     ┌──────────────────────────────┐
│ 检查租户是否    │────▶│ 是 → 展示企业微信活码          │
│ 绑定企业微信？  │     │   同时加企业微信好友            │
│               │     │   AND 创建平台好友关系          │
│               │     │                                │
│               │     │   流程：                        │
│               │     │   1. 用户长按识别企业微信活码     │
│               │     │   2. 跳转企业微信添加好友        │
│               │     │   3. 企业微信回调通知CRM         │
│               │     │   4. CRM 记录好友关系            │
└───────┬───────┘     └──────────────────────────────┘
        │ 否
        ▼
┌──────────────────────────────────┐
│ 仅展示平台好友码                   │
│ 用户关注/添加 → CRM 记录好友关系   │
└──────────────────────────────────┘
```

### S8.2 企业微信配置表 (tenant_wechat_work_config)

```sql
CREATE TABLE `tenant_wechat_work_config` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL COMMENT '租户ID',
  `corp_id`           VARCHAR(64)  NOT NULL COMMENT '企业微信企业ID',
  `agent_id`          VARCHAR(64)  NOT NULL COMMENT '应用AgentId',
  `secret`            VARCHAR(256) NOT NULL COMMENT '应用Secret（加密存储）',
  `contact_secret`    VARCHAR(256) DEFAULT NULL COMMENT '通讯录Secret（加密存储）',
  `customer_secret`   VARCHAR(256) DEFAULT NULL COMMENT '客户联系Secret（加密存储）',
  `callback_token`    VARCHAR(128) DEFAULT NULL COMMENT '回调Token',
  `callback_aes_key`  VARCHAR(256) DEFAULT NULL COMMENT '回调EncodingAESKey',
  `contact_way_id`    VARCHAR(64)  DEFAULT NULL COMMENT '联系我方式ID（活码）',
  `welcome_msg`       VARCHAR(1024) DEFAULT NULL COMMENT '欢迎语模板',
  `status`            TINYINT      NOT NULL DEFAULT 1,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户企业微信配置表';
```

### S8.3 企业微信相关 API

```
POST   /v1/wechat-work/config                     -- 配置企业微信
PUT    /v1/wechat-work/config/{id}                -- 更新配置
POST   /v1/wechat-work/config/{id}/test           -- 测试连接
GET    /v1/wechat-work/contact-way                -- 获取企业微信活码
POST   /v1/wechat-work/callback                   -- 企业微信回调接口（添加好友等事件）
```

---

## S9. 好友管理

### S9.1 设计思路

CRM 平台级好友体系，记录销售员与客户之间的好友关系。好友来源包括活动扫码添加、企业微信同步、手动添加等。

### S9.2 好友关系表 (friend)

```sql
CREATE TABLE `friend` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `user_id`           BIGINT       NOT NULL COMMENT '销售员用户ID',
  `friend_name`       VARCHAR(64)  NOT NULL COMMENT '好友名称',
  `friend_phone`      VARCHAR(32)  DEFAULT NULL COMMENT '好友手机号',
  `friend_avatar_url` VARCHAR(1024) DEFAULT NULL COMMENT '好友头像',
  `friend_company`    VARCHAR(256) DEFAULT NULL COMMENT '好友所属公司',
  `friend_position`   VARCHAR(64)  DEFAULT NULL COMMENT '好友职位',
  `friend_type`       TINYINT      NOT NULL DEFAULT 1 COMMENT '好友类型（1-平台好友 2-企业微信好友 3-双渠道好友）',
  `source`            TINYINT      NOT NULL DEFAULT 1 COMMENT '来源（1-活动扫码 2-企业微信同步 3-手动添加 4-线索导入 5-名片扫描）',
  `source_activity_id` BIGINT     DEFAULT NULL COMMENT '来源活动ID（活动扫码时）',
  `wechat_external_userid` VARCHAR(64) DEFAULT NULL COMMENT '企业微信外部联系人ID',
  `wechat_unionid`    VARCHAR(64)  DEFAULT NULL COMMENT '微信unionid',
  `wechat_nickname`   VARCHAR(128) DEFAULT NULL COMMENT '微信昵称',
  `customer_id`       BIGINT       DEFAULT NULL COMMENT '关联客户ID（已转为客户时）',
  `contact_id`        BIGINT       DEFAULT NULL COMMENT '关联联系人ID',
  `lead_id`           BIGINT       DEFAULT NULL COMMENT '关联线索ID',
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（1-正常 2-已删除好友 3-已被删除 4-已拉黑）',
  `tags`              VARCHAR(512) DEFAULT NULL COMMENT '标签（逗号分隔）',
  `remark`            VARCHAR(512) DEFAULT NULL COMMENT '备注名',
  `add_time`          DATETIME     NOT NULL COMMENT '添加时间',
  `last_chat_time`    DATETIME     DEFAULT NULL COMMENT '最后聊天时间',
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user` (`tenant_id`, `user_id`),
  KEY `idx_tenant_phone` (`tenant_id`, `friend_phone`),
  KEY `idx_tenant_customer` (`tenant_id`, `customer_id`),
  KEY `idx_tenant_activity` (`tenant_id`, `source_activity_id`),
  KEY `idx_wechat_external` (`tenant_id`, `wechat_external_userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';
```

### S9.3 好友管理 API

```
GET    /v1/friends                                 -- 好友列表
GET    /v1/friends/{id}                           -- 好友详情
POST   /v1/friends                                 -- 手动添加好友
PUT    /v1/friends/{id}                           -- 更新好友信息
DELETE /v1/friends/{id}                           -- 删除好友
POST   /v1/friends/{id}/convert-to-lead            -- 好友转线索
POST   /v1/friends/{id}/link-customer              -- 好友关联客户
GET    /v1/friends/statistics                      -- 好友统计
POST   /v1/friends/sync-wechat                     -- 同步企业微信好友
```

**好友列表响应**：
```json
GET /v1/friends?pageNum=1&pageSize=20
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 20001,
        "friendName": "张三",
        "friendPhone": "13800138000",
        "friendCompany": "示例科技有限公司",
        "friendPosition": "采购总监",
        "friendType": "dual_channel",
        "friendTypeLabel": "双渠道好友",
        "source": "activity_scan",
        "sourceLabel": "活动扫码",
        "sourceActivityName": "2026春季产品发布会",
        "isCustomer": true,
        "customerName": "示例科技有限公司",
        "customerId": 5001,
        "wechatNickname": "张三_采购",
        "addTime": "2026-02-25T10:30:00",
        "status": "normal",
        "tags": ["潜在客户", "展会"]
      }
    ],
    "total": 150,
    "statistics": {
      "totalFriends": 150,
      "platformFriends": 80,
      "wechatFriends": 50,
      "dualChannelFriends": 20,
      "convertedToCustomer": 35,
      "newThisMonth": 25
    }
  }
}
```

### S9.4 好友管理 APP 页面

```
┌──────────────────────────────────────┐
│ ← 好友管理                🔍  ➕    │
├──────────────────────────────────────┤
│                                      │
│  [全部(150)] [平台(80)] [企微(50)]   │
│                                      │
│  📊 本月新增: 25  已转客户: 35       │
│                                      │
│  ┌──────────────────────────────┐    │
│  │ 👤 张三                      │    │
│  │ 示例科技 · 采购总监           │    │
│  │ 🟢平台 🟢企微  来源:展会扫码  │    │
│  │ ✅ 已关联客户                 │    │
│  │ 添加于 2026-02-25            │    │
│  ├──────────────────────────────┤    │
│  │ 👤 李四                      │    │
│  │ XX有限公司 · 技术总监         │    │
│  │ 🟢平台  来源:手动添加         │    │
│  │ [转为线索]                    │    │
│  │ 添加于 2026-02-24            │    │
│  ├──────────────────────────────┤    │
│  │ 👤 王五                      │    │
│  │ YY集团 · CEO                 │    │
│  │ 🟢企微  来源:企微同步         │    │
│  │ [关联客户]  [转为线索]         │    │
│  │ 添加于 2026-02-20            │    │
│  └──────────────────────────────┘    │
│                                      │
├──────────┬──────────┬──────────┬────┤
│   首页   │   客户   │    +    │工作 │我的│
└──────────┴──────────┴──────────┴────┘
```

---

## 新增微服务与模块变更汇总

### 新增服务

| 服务名 | 说明 |
|---|---|
| `aicrm-activity` | 活动管理服务：活动 CRUD、二维码生成、参与人管理、扫码落地页 |
| `aicrm-social` | 社交关系服务：好友管理、企业微信集成、好友-客户关联 |

### 现有服务变更

| 服务 | 变更内容 |
|---|---|
| `aicrm-customer` | 新增黑名单管理、无效客户管理、采购日期字段、五度易链企业查询集成 |
| `aicrm-fieldwork` | 新增位置上报接口、行动轨迹查询 |
| `aicrm-erp` | 新增应收账款和往来明细接口适配 |
| `aicrm-report` | 新增销售员个人/团队详细报表 |

### 工程结构补充

```
aicrm/
├── aicrm-modules/
│   ├── ...（原有模块）
│   ├── aicrm-activity/              -- 活动管理服务 (新增)
│   └── aicrm-social/                -- 社交关系服务 (新增)
├── aicrm-api/
│   ├── ...（原有API）
│   ├── aicrm-api-activity/          -- 活动服务API (新增)
│   └── aicrm-api-social/            -- 社交服务API (新增)

PC 前端补充：
├── views/
│   ├── trajectory/                  -- 行动轨迹 (新增)
│   │   └── TrajectoryMap.vue
│   ├── enterprise/                  -- 企业查询 (新增)
│   │   └── EnterpriseSearch.vue
│   ├── activity/                    -- 活动管理 (新增)
│   │   ├── ActivityList.vue
│   │   ├── ActivityDetail.vue
│   │   └── ParticipantList.vue
│   ├── friend/                      -- 好友管理 (新增)
│   │   ├── FriendList.vue
│   │   └── FriendDetail.vue
│   ├── customer/
│   │   ├── ...（原有）
│   │   ├── BlacklistList.vue        -- 黑名单 (新增)
│   │   └── InvalidList.vue          -- 无效客户 (新增)
│   └── report/
│       ├── ...（原有）
│       ├── SalesPersonalReport.vue  -- 个人报表 (新增)
│       └── TeamActionReport.vue     -- 团队行动力 (新增)

APP 补充：
├── pages/
│   ├── trajectory/                  -- 行动轨迹 (新增)
│   │   └── map.vue
│   ├── enterprise/                  -- 企业查询 (新增)
│   │   └── search.vue
│   ├── activity/                    -- 活动管理 (新增)
│   │   ├── list.vue
│   │   ├── detail.vue
│   │   └── scan.vue                 -- 扫码页
│   ├── friend/                      -- 好友管理 (新增)
│   │   ├── list.vue
│   │   └── detail.vue
│   └── report/                      -- 个人报表 (新增)
│       └── personal.vue
```

---

## 新增 ER 关系

```
activity (1) ──── (N) activity_participant
activity_participant (0..1) ── (1) customer
activity_participant (0..1) ── (1) lead
activity_participant (0..1) ── (1) friend

friend (0..1) ── (1) customer
friend (0..1) ── (1) customer_contact
friend (0..1) ── (1) lead
friend (N) ──── (1) user (销售员)

customer (1) ──── (N) customer_blacklist
tenant (1) ──── (1) tenant_wechat_work_config
tenant (1) ──── (N) third_party_config

user (1) ──── (N) location_report
```
