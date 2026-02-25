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
  `daily_quota`       INT          NOT NULL DEFAULT 100 COMMENT '企业搜索每日配额',
  `daily_used`        INT          NOT NULL DEFAULT 0 COMMENT '企业搜索今日已用',
  `contact_daily_quota` INT        NOT NULL DEFAULT 50 COMMENT '联系方式查询每日配额',
  `contact_daily_used`  INT        NOT NULL DEFAULT 0 COMMENT '联系方式查询今日已用',
  `contact_monthly_quota` INT      DEFAULT NULL COMMENT '联系方式查询月度配额（NULL=不限）',
  `contact_monthly_used`  INT      NOT NULL DEFAULT 0 COMMENT '联系方式查询本月已用',
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

### S2.5 企业联系方式表 (enterprise_contact)

五度易链的联系方式查询通常按次计费，因此查询到的联系方式需要本地持久化存储以便复用，避免重复付费查询。

```sql
CREATE TABLE `enterprise_contact` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `credit_code`       VARCHAR(32)  NOT NULL COMMENT '统一社会信用代码',
  `company_name`      VARCHAR(256) NOT NULL COMMENT '企业名称',
  `contact_name`      VARCHAR(64)  DEFAULT NULL COMMENT '联系人姓名',
  `position`          VARCHAR(128) DEFAULT NULL COMMENT '职位/职务',
  `department`        VARCHAR(128) DEFAULT NULL COMMENT '部门',
  `phone`             VARCHAR(32)  DEFAULT NULL COMMENT '手机号',
  `telephone`         VARCHAR(64)  DEFAULT NULL COMMENT '固话（可能含多个，逗号分隔）',
  `email`             VARCHAR(256) DEFAULT NULL COMMENT '邮箱（可能含多个，逗号分隔）',
  `source`            VARCHAR(32)  NOT NULL DEFAULT 'wdyl' COMMENT '数据来源（wdyl/tianyancha/qichacha）',
  `source_type`       VARCHAR(32)  DEFAULT NULL COMMENT '来源分类（annual_report=年报/sec_filing=证监会/recruitment=招聘/other=其他）',
  `reliability`       TINYINT      DEFAULT NULL COMMENT '可靠度（1-低 2-中 3-高）',
  `is_imported`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已导入为CRM联系人',
  `imported_customer_id` BIGINT    DEFAULT NULL COMMENT '导入到的客户ID',
  `imported_contact_id`  BIGINT    DEFAULT NULL COMMENT '导入后的联系人ID',
  `query_user_id`     BIGINT       NOT NULL COMMENT '查询人',
  `query_time`        DATETIME     NOT NULL COMMENT '查询时间',
  `raw_data`          JSON         DEFAULT NULL COMMENT '原始响应数据',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_credit_code` (`tenant_id`, `credit_code`),
  KEY `idx_tenant_company` (`tenant_id`, `company_name`),
  KEY `idx_tenant_phone` (`tenant_id`, `phone`),
  KEY `idx_tenant_query_user` (`tenant_id`, `query_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业联系方式表（五度易链查询结果）';
```

### S2.6 联系方式查询日志表 (enterprise_contact_query_log)

每次调用五度易链查询联系方式都记录日志，用于配额管控、费用统计和审计追踪。

```sql
CREATE TABLE `enterprise_contact_query_log` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `credit_code`       VARCHAR(32)  NOT NULL COMMENT '查询企业信用代码',
  `company_name`      VARCHAR(256) NOT NULL COMMENT '查询企业名称',
  `query_user_id`     BIGINT       NOT NULL COMMENT '查询人ID',
  `query_user_name`   VARCHAR(64)  DEFAULT NULL COMMENT '查询人姓名',
  `query_type`        TINYINT      NOT NULL COMMENT '查询类型（1-首次查询 2-刷新查询）',
  `result_count`      INT          NOT NULL DEFAULT 0 COMMENT '返回联系人数',
  `phone_count`       INT          NOT NULL DEFAULT 0 COMMENT '返回手机号数',
  `email_count`       INT          NOT NULL DEFAULT 0 COMMENT '返回邮箱数',
  `is_cache_hit`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否命中缓存（命中则不消耗配额）',
  `cost_points`       INT          NOT NULL DEFAULT 0 COMMENT '消耗积分/点数',
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（1-成功 2-失败 3-配额不足 4-接口异常）',
  `error_message`     VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
  `request_data`      TEXT         DEFAULT NULL COMMENT '请求参数（JSON）',
  `response_data`     TEXT         DEFAULT NULL COMMENT '响应数据摘要（JSON，脱敏）',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user` (`tenant_id`, `query_user_id`),
  KEY `idx_tenant_credit_code` (`tenant_id`, `credit_code`),
  KEY `idx_tenant_time` (`tenant_id`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业联系方式查询日志表';
```

### S2.7 联系方式查询流程

```
┌───────────────────────────────────────────────────────────────────────┐
│                  企业联系方式查询完整流程                                │
│                                                                       │
│  用户点击 [查询联系方式]                                               │
│         │                                                             │
│         ▼                                                             │
│  ┌──────────────────┐                                                │
│  │ 1. 检查本地缓存   │                                                │
│  │ enterprise_contact│                                                │
│  │ 按 credit_code    │                                                │
│  │ 查询是否已有数据   │                                                │
│  └────────┬─────────┘                                                │
│           │                                                           │
│     ┌─────▼─────┐                                                    │
│     │ 有缓存？   │                                                    │
│     └─────┬─────┘                                                    │
│       是  │   否                                                      │
│     ┌─────▼──────────────────┐   ┌─────▼──────────────────────────┐  │
│     │ 判断缓存是否过期         │   │ 2. 检查配额                    │  │
│     │ (默认30天有效)           │   │ contact_daily_used < quota ?  │  │
│     │                        │   │ contact_monthly_used < quota ? │  │
│     │ 未过期 → 直接返回缓存   │   └─────┬──────────────────────────┘  │
│     │ (不消耗配额)            │         │                             │
│     │                        │    配额充足 │ 配额不足                  │
│     │ 已过期 → 用户可选择      │         │      │                     │
│     │ [查看缓存] 或 [刷新]    │         │  ┌───▼───────────────┐     │
│     └────────────────────────┘         │  │ 返回配额不足提示   │     │
│                                        │  │ 展示已有缓存数据   │     │
│                              ┌─────────▼┐ │ (如有)            │     │
│                              │ 3. 调用    │ └───────────────────┘     │
│                              │ 五度易链API│                           │
│                              │ 查询联系方式│                           │
│                              └─────┬─────┘                           │
│                                    │                                  │
│                              ┌─────▼─────────────────────────┐      │
│                              │ 4. 处理返回结果                 │      │
│                              │ ├── 写入 enterprise_contact    │      │
│                              │ ├── 写入查询日志               │      │
│                              │ ├── 更新配额计数               │      │
│                              │ └── 手机号与CRM客户联系人匹配   │      │
│                              │     标注哪些号码已在CRM中存在   │      │
│                              └─────┬─────────────────────────┘      │
│                                    │                                  │
│                              ┌─────▼─────────────────────────┐      │
│                              │ 5. 返回结果给前端               │      │
│                              │ ├── 联系人列表                 │      │
│                              │ ├── 每人可操作：               │      │
│                              │ │   [导入为联系人]              │      │
│                              │ │   [拨打电话]                 │      │
│                              │ │   [复制号码]                 │      │
│                              │ └── 配额剩余提示               │      │
│                              └───────────────────────────────┘      │
└───────────────────────────────────────────────────────────────────────┘
```

### S2.8 企业查询 API（完整）

```
GET    /v1/enterprise/search                      -- 搜索企业（五度易链）
GET    /v1/enterprise/{creditCode}                -- 企业详情
GET    /v1/enterprise/{creditCode}/risk           -- 企业风险信息
GET    /v1/enterprise/{creditCode}/contacts       -- 查询企业联系方式 ★
GET    /v1/enterprise/{creditCode}/contacts/cache -- 仅查看本地缓存的联系方式（不调用API）
POST   /v1/enterprise/{creditCode}/contacts/refresh -- 强制刷新联系方式（消耗配额）★
POST   /v1/enterprise/{creditCode}/contacts/{contactId}/import -- 导入联系人到CRM客户 ★
POST   /v1/enterprise/{creditCode}/contacts/batch-import       -- 批量导入联系人 ★
POST   /v1/enterprise/{creditCode}/import         -- 将企业导入为客户/线索
GET    /v1/enterprise/contact-query-logs          -- 联系方式查询日志
GET    /v1/enterprise/contact-quota               -- 查看配额使用情况
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
        },
        "contactQueryStatus": {
          "hasCache": true,
          "cachedContactCount": 5,
          "cacheTime": "2026-02-20T10:30:00",
          "cacheExpired": false
        }
      },
      {
        "companyName": "北京示例科技股份有限公司",
        "creditCode": "91110000MA0XXXXXX",
        "legalPerson": "李四",
        "registeredCapital": "1亿元",
        "isPlatformCustomer": false,
        "platformCustomerInfo": null,
        "contactQueryStatus": {
          "hasCache": false,
          "cachedContactCount": 0,
          "cacheTime": null,
          "cacheExpired": false
        }
      }
    ],
    "total": 2,
    "contactQuota": {
      "dailyRemaining": 42,
      "monthlyRemaining": 180
    }
  }
}
```

**查询企业联系方式响应**：
```json
GET /v1/enterprise/91440300MA5XXXXXX/contacts
{
  "code": 200,
  "data": {
    "companyName": "深圳示例科技有限公司",
    "creditCode": "91440300MA5XXXXXX",
    "dataSource": "cache",
    "queryTime": "2026-02-20T10:30:00",
    "cacheExpireTime": "2026-03-22T10:30:00",
    "contacts": [
      {
        "id": 30001,
        "contactName": "张三",
        "position": "董事长/总经理",
        "department": null,
        "phone": "138****8000",
        "phoneFull": "13800138000",
        "telephone": "0755-8888****",
        "telephoneFull": "0755-88881234",
        "email": "zhang***@example.com",
        "emailFull": "zhangsan@example.com",
        "sourceType": "annual_report",
        "sourceTypeLabel": "年报公示",
        "reliability": 3,
        "reliabilityLabel": "高",
        "isImported": true,
        "importedCustomerId": 5001,
        "importedCustomerName": "深圳示例科技有限公司",
        "existsInCrm": true,
        "crmMatchInfo": {
          "matchType": "phone",
          "customerId": 5001,
          "contactId": 6001,
          "contactName": "张三"
        }
      },
      {
        "id": 30002,
        "contactName": "李芳",
        "position": "采购经理",
        "department": "采购部",
        "phone": "139****9000",
        "phoneFull": "13900139000",
        "telephone": null,
        "telephoneFull": null,
        "email": "li***@example.com",
        "emailFull": "lifang@example.com",
        "sourceType": "recruitment",
        "sourceTypeLabel": "招聘信息",
        "reliability": 2,
        "reliabilityLabel": "中",
        "isImported": false,
        "importedCustomerId": null,
        "existsInCrm": false,
        "crmMatchInfo": null
      },
      {
        "id": 30003,
        "contactName": null,
        "position": null,
        "department": "总机",
        "phone": null,
        "phoneFull": null,
        "telephone": "0755-8888****,0755-8888****",
        "telephoneFull": "0755-88881234,0755-88885678",
        "email": "info***@example.com",
        "emailFull": "info@example.com",
        "sourceType": "annual_report",
        "sourceTypeLabel": "年报公示",
        "reliability": 3,
        "reliabilityLabel": "高",
        "isImported": false,
        "existsInCrm": false,
        "crmMatchInfo": null
      }
    ],
    "summary": {
      "totalContacts": 5,
      "withPhone": 3,
      "withEmail": 4,
      "highReliability": 2,
      "alreadyInCrm": 1,
      "alreadyImported": 1
    },
    "quota": {
      "dailyRemaining": 42,
      "monthlyRemaining": 180
    }
  }
}
```

**导入联系人到CRM客户请求**：
```json
POST /v1/enterprise/91440300MA5XXXXXX/contacts/30002/import
{
  "customerId": 5001,
  "isPrimary": false,
  "isDecisionMaker": true,
  "remark": "从五度易链查询导入"
}
```

**批量导入联系人请求**：
```json
POST /v1/enterprise/91440300MA5XXXXXX/contacts/batch-import
{
  "customerId": 5001,
  "contactIds": [30002, 30003],
  "remark": "批量导入采购部联系人"
}
```

**配额使用情况响应**：
```json
GET /v1/enterprise/contact-quota
{
  "code": 200,
  "data": {
    "daily": {
      "quota": 50,
      "used": 8,
      "remaining": 42,
      "resetTime": "2026-02-26T00:00:00"
    },
    "monthly": {
      "quota": 500,
      "used": 320,
      "remaining": 180,
      "resetTime": "2026-03-01T00:00:00"
    },
    "recentQueries": [
      {
        "queryTime": "2026-02-25T14:30:00",
        "companyName": "示例科技有限公司",
        "resultCount": 5,
        "queryUserName": "李明",
        "isCacheHit": false
      }
    ]
  }
}
```

### S2.9 联系方式数据脱敏策略

```
┌──────────────────────────────────────────────────────────┐
│                联系方式脱敏与权限策略                       │
│                                                            │
│  1. 展示脱敏                                              │
│     ├── 列表展示时默认脱敏：138****8000                    │
│     ├── 点击 [查看完整号码] 后显示全量                     │
│     └── 查看完整号码的操作记入审计日志                     │
│                                                            │
│  2. 权限控制                                               │
│     ├── 查询联系方式：需 enterprise:contact:query 权限     │
│     ├── 查看完整号码：需 enterprise:contact:view_full 权限 │
│     ├── 导入为联系人：需 customer:contact:create 权限      │
│     └── 管理员可配置是否允许导出联系方式                    │
│                                                            │
│  3. 接口返回字段策略                                       │
│     ├── phone / email → 脱敏版（列表展示）                │
│     ├── phoneFull / emailFull → 完整版                    │
│     └── 无 view_full 权限时，Full字段返回 null             │
│                                                            │
│  4. 防滥用策略                                             │
│     ├── 单用户每日查询上限（可配置，默认 20 次）           │
│     ├── 同一企业 30 天内重复查询不消耗配额（命中缓存）     │
│     ├── 查询日志全量记录，可审计追溯                       │
│     └── 异常查询频率自动告警                               │
└──────────────────────────────────────────────────────────┘
```

### S2.10 平台企业匹配逻辑

```
查询五度易链返回结果后：

1. 对每条结果，用统一社会信用代码匹配 customer 表
2. 若无信用代码，用企业名称模糊匹配
3. 标注匹配状态：
   ├── isPlatformCustomer: true  → 已是本租户客户
   ├── isPlatformCustomer: false → 不是本租户客户
   └── 展示所属销售员信息（数据权限内可见）

4. 可直接将查询到的企业一键导入为线索或客户

联系方式查询后额外匹配：
5. 将返回的手机号与 customer_contact 表匹配
6. 标注每个联系人是否已存在于CRM中
7. 已存在的联系人展示关联的客户名称和联系人ID
```

### S2.11 页面设计

```
PC 端 - 企业查询页面（含联系方式查询）
┌───────────────────────────────────────────────────────────────────┐
│  企业信息查询                              配额: 今日 42/50  本月 180/500│
│                                                                   │
│  🔍 [________________________] [搜索]                           │
│     输入企业名称或统一社会信用代码                                  │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │  深圳示例科技有限公司                                      │    │
│  │  统一社会信用代码: 91440300MA5XXXXXX                       │    │
│  │  法人: 张三  |  注册资本: 5000万  |  成立: 2018-05-10      │    │
│  │  状态: 存续  |  行业: 软件和信息技术服务业                  │    │
│  │  🟢 已是平台客户 → 负责人: 李明 (华南销售部)               │    │
│  │  📞 已有 5 个联系方式 (2026-02-20查询)                     │    │
│  │  [查看客户详情]  [查看联系方式]  [刷新联系方式]             │    │
│  ├──────────────────────────────────────────────────────────┤    │
│  │  北京示例科技股份有限公司                                   │    │
│  │  统一社会信用代码: 91110000MA0XXXXXX                       │    │
│  │  法人: 李四  |  注册资本: 1亿  |  成立: 2015-03-20         │    │
│  │  状态: 存续  |  行业: 互联网和相关服务                      │    │
│  │  ⚪ 非平台客户                                            │    │
│  │  📞 未查询联系方式                                        │    │
│  │  [查询联系方式]  [导入为线索]  [导入为客户]                 │    │
│  └──────────────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────────────┘

点击 [查看联系方式] / [查询联系方式] 后弹出或展开：
┌──────────────────────────────────────────────────────────────────┐
│  📞 深圳示例科技有限公司 - 联系方式                          [关闭] │
│                                                                    │
│  来源: 五度易链  |  查询时间: 2026-02-20  |  共 5 个联系人         │
│  [全部导入到客户]  [刷新数据(-1配额)]                              │
│                                                                    │
│  ┌────────┬──────────┬──────────┬──────────┬────────┬──────────┐ │
│  │ 姓名   │ 职位     │ 手机号    │ 固话     │ 邮箱   │ 操作     │ │
│  ├────────┼──────────┼──────────┼──────────┼────────┼──────────┤ │
│  │ 张三   │ 总经理    │138****000│0755-8888 │zhang***│ 🟢已在CRM│ │
│  │        │          │[查看完整] │[查看完整] │[查看]  │ [查看]   │ │
│  │ 🟢高   │ 来源:年报 │          │          │        │          │ │
│  ├────────┼──────────┼──────────┼──────────┼────────┼──────────┤ │
│  │ 李芳   │ 采购经理  │139****000│   -      │li***@  │ [导入]   │ │
│  │        │          │[查看完整] │          │[查看]  │ [拨打]   │ │
│  │ 🟡中   │ 来源:招聘 │          │          │        │ [复制]   │ │
│  ├────────┼──────────┼──────────┼──────────┼────────┼──────────┤ │
│  │ -      │ 总机     │   -      │0755-8888 │info*** │ [导入]   │ │
│  │        │          │          │0755-8888 │[查看]  │ [复制]   │ │
│  │ 🟢高   │ 来源:年报 │          │[查看完整] │        │          │ │
│  └────────┴──────────┴──────────┴──────────┴────────┴──────────┘ │
│                                                                    │
│  💡 提示：🟢高可靠度(年报/官方)  🟡中可靠度(招聘)  🔴低可靠度(其他) │
│  ⚠️ 同一企业30天内再次查询不消耗配额                               │
└──────────────────────────────────────────────────────────────────┘
```

```
APP 端 - 企业查询 + 联系方式
┌──────────────────────────────────────┐
│ ← 企业查询           配额: 42/50     │
├──────────────────────────────────────┤
│                                      │
│  🔍 [搜索企业名称___________]        │
│                                      │
│  ┌──────────────────────────────┐    │
│  │ 深圳示例科技有限公司          │    │
│  │ 法人:张三  资本:5000万        │    │
│  │ 🟢 已是平台客户              │    │
│  │ 📞 5个联系方式               │    │
│  │ [查看详情]  [查看联系方式]     │    │
│  ├──────────────────────────────┤    │
│  │ 北京示例科技股份有限公司       │    │
│  │ 法人:李四  资本:1亿           │    │
│  │ ⚪ 非平台客户                │    │
│  │ 📞 未查询                    │    │
│  │ [查询联系方式]  [导入]        │    │
│  └──────────────────────────────┘    │
│                                      │
│  点击 [查看联系方式] 进入：            │
│  ┌──────────────────────────────┐    │
│  │ ← 联系方式                    │    │
│  │ 深圳示例科技有限公司           │    │
│  │ 查询于 2026-02-20  共5人      │    │
│  │                              │    │
│  │ 👤 张三 · 总经理  🟢高       │    │
│  │ 📱 138****8000  [查看] [拨打] │    │
│  │ 📧 zhang***@... [查看]       │    │
│  │ 来源: 年报  🟢已在CRM         │    │
│  │                              │    │
│  │ 👤 李芳 · 采购经理  🟡中     │    │
│  │ 📱 139****9000  [查看] [拨打] │    │
│  │ 📧 li***@...    [查看]       │    │
│  │ 来源: 招聘                    │    │
│  │ [导入为联系人]                │    │
│  │                              │    │
│  │ 📞 总机  🟢高                │    │
│  │ ☎️ 0755-8888**** [查看]      │    │
│  │ 📧 info***@...  [查看]       │    │
│  │ 来源: 年报                    │    │
│  │ [导入为联系人]                │    │
│  │                              │    │
│  │     [全部导入]  [刷新数据]    │    │
│  └──────────────────────────────┘    │
│                                      │
└──────────────────────────────────────┘
```

### S2.12 客户详情中的联系方式查询入口

在客户 360° 视图中，如果客户已关联了统一社会信用代码（`credit_code`），则在联系人列表区域提供 [从五度易链获取更多联系人] 的入口：

```
客户详情 - 联系人Tab
┌───────────────────────────────────────────────────┐
│  联系人 (3)                    [新增联系人]         │
│                                                     │
│  👤 张三 · 总经理（主联系人/决策人）                  │
│     📱 13800138000  📧 zhangsan@example.com        │
│                                                     │
│  👤 王经理 · 采购部                                 │
│     📱 13900139000                                  │
│                                                     │
│  👤 总机                                            │
│     ☎️ 0755-88881234                                │
│                                                     │
│  ─────────────────────────────────────────────     │
│  📞 从五度易链获取更多联系人                          │
│  信用代码: 91440300MA5XXXXXX                        │
│  上次查询: 2026-02-20 (5个结果, 已导入1个)           │
│  [查看已查询联系方式]  [刷新查询]                     │
│                                                     │
└───────────────────────────────────────────────────┘
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

活动分为两大类：

| 类别 | 说明 | 典型场景 |
|---|---|---|
| **需要报名** | 客户扫码后须填写信息提交报名，需审核或直接通过 | 线下展会、培训会议、客户沙龙、产品发布会 |
| **不需要报名** | 客户扫码后直接参与（查看内容/加好友），无需填写报名表 | 线上推广、品牌宣传、优惠活动、内容分发 |

每个活动自动生成唯一二维码。不同类别的活动扫码后落地页体验不同，但都可触发加好友流程（详见 S8）。

### S7.2 活动表 (activity)

```sql
CREATE TABLE `activity` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `activity_no`       VARCHAR(32)  NOT NULL COMMENT '活动编号',
  `activity_name`     VARCHAR(256) NOT NULL COMMENT '活动名称',
  `activity_category` TINYINT      NOT NULL DEFAULT 1 COMMENT '活动类别（1-需要报名 2-不需要报名）',
  `activity_type`     TINYINT      NOT NULL COMMENT '活动类型（1-线下展会 2-线上推广 3-产品发布 4-客户沙龙 5-培训会议 6-品牌宣传 7-优惠活动 8-内容分发 9-其他）',
  `description`       TEXT         DEFAULT NULL COMMENT '活动描述',
  `rich_content`      LONGTEXT     DEFAULT NULL COMMENT '富文本活动详情（H5内容）',
  `cover_image_url`   VARCHAR(1024) DEFAULT NULL COMMENT '封面图片URL',
  `banner_urls`       TEXT         DEFAULT NULL COMMENT '轮播图URL列表（JSON数组）',
  `start_time`        DATETIME     NOT NULL COMMENT '活动开始时间',
  `end_time`          DATETIME     NOT NULL COMMENT '活动结束时间',

  -- 报名相关（仅 activity_category=1 时生效）
  `registration_start_time` DATETIME DEFAULT NULL COMMENT '报名开始时间',
  `registration_end_time`   DATETIME DEFAULT NULL COMMENT '报名截止时间',
  `max_participants`  INT          DEFAULT NULL COMMENT '最大报名人数（NULL=不限）',
  `current_participants` INT       NOT NULL DEFAULT 0 COMMENT '当前报名人数',
  `registration_approval` TINYINT  NOT NULL DEFAULT 0 COMMENT '报名是否需审核（0-自动通过 1-需审核）',
  `registration_form_config` JSON  DEFAULT NULL COMMENT '报名表单自定义字段配置（JSON）',
  `registration_notice`  TEXT      DEFAULT NULL COMMENT '报名须知/注意事项',
  `waitlist_enabled`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否开启候补（满额后可排队）',

  -- 地点/链接
  `location`          VARCHAR(512) DEFAULT NULL COMMENT '活动地点',
  `longitude`         DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `latitude`          DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `online_url`        VARCHAR(1024) DEFAULT NULL COMMENT '线上活动链接',

  -- 二维码
  `qr_code_url`       VARCHAR(1024) NOT NULL COMMENT '活动二维码URL',
  `qr_code_content`   VARCHAR(512) NOT NULL COMMENT '二维码内容（跳转链接）',
  `landing_page_url`  VARCHAR(1024) DEFAULT NULL COMMENT '自定义落地页URL（为空则使用系统默认）',

  -- 扫码统计（无需报名的活动用此统计）
  `total_scans`       INT          NOT NULL DEFAULT 0 COMMENT '总扫码次数',
  `unique_scans`      INT          NOT NULL DEFAULT 0 COMMENT '独立扫码人数（去重）',

  -- 状态与归属
  `status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0-草稿 1-未开始 2-报名中 3-报名截止 4-进行中 5-已结束 6-已取消）',
  `owner_user_id`     BIGINT       NOT NULL COMMENT '活动负责人',
  `owner_org_id`      BIGINT       DEFAULT NULL COMMENT '负责人所属组织',
  `collaborator_ids`  VARCHAR(512) DEFAULT NULL COMMENT '协作人ID列表（逗号分隔）',

  -- 费用
  `budget`            DECIMAL(15,2) DEFAULT NULL COMMENT '活动预算',
  `actual_cost`       DECIMAL(15,2) DEFAULT NULL COMMENT '实际费用',

  -- 企业微信
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
  KEY `idx_tenant_category` (`tenant_id`, `activity_category`),
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
  `ext_form_data`     JSON         DEFAULT NULL COMMENT '自定义表单字段数据（JSON）',
  `source`            TINYINT      NOT NULL DEFAULT 1 COMMENT '来源（1-扫码报名 2-手动录入 3-批量导入 4-扫码参与(无需报名)）',
  `scan_time`         DATETIME     DEFAULT NULL COMMENT '扫码时间',
  `registration_status` TINYINT    NOT NULL DEFAULT 1 COMMENT '报名状态（0-待审核 1-已通过 2-已拒绝 3-已取消 4-候补中）',
  `registration_time` DATETIME     DEFAULT NULL COMMENT '报名时间',
  `reject_reason`     VARCHAR(512) DEFAULT NULL COMMENT '拒绝原因',
  `checkin_status`    TINYINT      NOT NULL DEFAULT 0 COMMENT '签到状态（0-未签到 1-已签到）',
  `checkin_time`      DATETIME     DEFAULT NULL COMMENT '签到时间',
  `checkin_location`  VARCHAR(512) DEFAULT NULL COMMENT '签到地点',
  `is_customer`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已是客户',
  `customer_id`       BIGINT       DEFAULT NULL COMMENT '关联客户ID',
  `is_lead`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已转为线索',
  `lead_id`           BIGINT       DEFAULT NULL COMMENT '关联线索ID',
  `friend_added`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已加好友',
  `friend_id`         BIGINT       DEFAULT NULL COMMENT '关联好友ID',
  `wechat_added`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已加企业微信',
  `feedback`          TEXT         DEFAULT NULL COMMENT '参后反馈',
  `feedback_score`    TINYINT      DEFAULT NULL COMMENT '满意度评分（1~5）',
  `remark`            VARCHAR(512) DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_activity` (`tenant_id`, `activity_id`),
  KEY `idx_tenant_phone` (`tenant_id`, `participant_phone`),
  KEY `idx_activity_reg_status` (`tenant_id`, `activity_id`, `registration_status`),
  KEY `idx_activity_source` (`tenant_id`, `activity_id`, `source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动参与人表';
```

### S7.4 活动扫码记录表 (activity_scan_log)

用于"不需要报名"类活动的扫码统计，以及"需要报名"类活动的扫码漏斗分析：

```sql
CREATE TABLE `activity_scan_log` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `activity_id`       BIGINT       NOT NULL COMMENT '活动ID',
  `scan_fingerprint`  VARCHAR(128) DEFAULT NULL COMMENT '扫码者指纹（IP+UA hash，用于去重）',
  `scan_ip`           VARCHAR(64)  DEFAULT NULL COMMENT '扫码IP',
  `scan_ua`           VARCHAR(512) DEFAULT NULL COMMENT '浏览器UA',
  `scan_time`         DATETIME     NOT NULL COMMENT '扫码时间',
  `referer`           VARCHAR(512) DEFAULT NULL COMMENT '来源渠道',
  `did_register`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否完成了报名',
  `did_add_friend`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否添加了好友',
  `participant_id`    BIGINT       DEFAULT NULL COMMENT '关联参与人ID（报名后回填）',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_activity` (`tenant_id`, `activity_id`),
  KEY `idx_scan_time` (`tenant_id`, `scan_time`),
  KEY `idx_fingerprint` (`tenant_id`, `activity_id`, `scan_fingerprint`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动扫码记录表';
```

### S7.5 两类活动的业务差异

```
┌──────────────────────────────────────────────────────────────────┐
│               需要报名 vs 不需要报名 对比                          │
│                                                                    │
│  ┌──────────────┬──────────────────┬─────────────────────────┐    │
│  │              │ 需要报名          │ 不需要报名               │    │
│  ├──────────────┼──────────────────┼─────────────────────────┤    │
│  │ 活动状态流转  │ 草稿→未开始→报名中│ 草稿→未开始→进行中→已结束│    │
│  │              │ →报名截止→进行中  │                         │    │
│  │              │ →已结束           │                         │    │
│  ├──────────────┼──────────────────┼─────────────────────────┤    │
│  │ 扫码落地页    │ 活动详情 + 报名表单│ 活动内容页（图文详情）    │    │
│  │              │ + 报名须知        │ + 加好友入口             │    │
│  ├──────────────┼──────────────────┼─────────────────────────┤    │
│  │ 参与人来源    │ 扫码报名/手动录入 │ 扫码自动记录（仅采集     │    │
│  │              │ /批量导入         │ 基础信息）/手动录入      │    │
│  ├──────────────┼──────────────────┼─────────────────────────┤    │
│  │ 报名审核      │ 可配置是否需要审核│ 不涉及                  │    │
│  ├──────────────┼──────────────────┼─────────────────────────┤    │
│  │ 名额限制      │ 可设上限+候补     │ 不涉及                  │    │
│  ├──────────────┼──────────────────┼─────────────────────────┤    │
│  │ 签到          │ 支持现场签到      │ 不涉及                  │    │
│  ├──────────────┼──────────────────┼─────────────────────────┤    │
│  │ 核心统计指标  │ 报名数/通过率/签到│ 扫码量/独立访客/加好友数 │    │
│  │              │ 率/转化数         │ /转化数                 │    │
│  ├──────────────┼──────────────────┼─────────────────────────┤    │
│  │ 加好友        │ 报名成功后触发    │ 扫码后直接触发           │    │
│  ├──────────────┼──────────────────┼─────────────────────────┤    │
│  │ 报名表单      │ 必填姓名+电话     │ 不涉及                  │    │
│  │              │ +自定义字段       │                         │    │
│  └──────────────┴──────────────────┴─────────────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
```

### S7.6 活动状态流转

```
需要报名类活动：
┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐
│ 草稿  │──▶│未开始 │──▶│报名中 │──▶│报名截止│──▶│进行中 │──▶│已结束 │
└──────┘   └──────┘   └──────┘   └──────┘   └──────┘   └──────┘
                          │                                │
                          └─ 手动截止 ─▶ 报名截止            │
                                                      ┌───▼───┐
                                                      │ 已取消 │
                                                      └───────┘

不需要报名类活动：
┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐
│ 草稿  │──▶│未开始 │──▶│进行中 │──▶│已结束 │
└──────┘   └──────┘   └──────┘   └──────┘
                                     │
                                ┌───▼───┐
                                │ 已取消 │
                                └───────┘
```

### S7.7 活动管理 API

```
POST   /v1/activities                              -- 创建活动
GET    /v1/activities                              -- 活动列表（支持按类别筛选）
GET    /v1/activities/{id}                         -- 活动详情
PUT    /v1/activities/{id}                         -- 更新活动
DELETE /v1/activities/{id}                         -- 删除活动
PUT    /v1/activities/{id}/publish                 -- 发布活动（草稿→未开始/报名中）
PUT    /v1/activities/{id}/cancel                  -- 取消活动
PUT    /v1/activities/{id}/end                     -- 手动结束活动
PUT    /v1/activities/{id}/close-registration      -- 手动截止报名（需报名类）
GET    /v1/activities/{id}/qrcode                  -- 获取活动二维码
GET    /v1/activities/{id}/qrcode/download          -- 下载二维码图片（支持尺寸参数）
POST   /v1/activities/{id}/clone                   -- 复制活动

POST   /v1/activities/{id}/participants             -- 添加参与人（手动）
GET    /v1/activities/{id}/participants             -- 参与人列表（分页+筛选）
PUT    /v1/activities/{id}/participants/{pid}       -- 更新参与人信息
DELETE /v1/activities/{id}/participants/{pid}       -- 删除参与人
POST   /v1/activities/{id}/participants/import      -- 批量导入参与人
GET    /v1/activities/{id}/participants/export      -- 导出参与人
POST   /v1/activities/{id}/participants/{pid}/approve  -- 审核通过报名
POST   /v1/activities/{id}/participants/{pid}/reject   -- 审核拒绝报名
POST   /v1/activities/{id}/participants/batch-approve  -- 批量审核通过
POST   /v1/activities/{id}/participants/{pid}/checkin  -- 参与人签到
POST   /v1/activities/{id}/participants/batch-checkin  -- 批量签到（扫参与人码）
POST   /v1/activities/{id}/participants/{pid}/convert  -- 转为线索/客户
POST   /v1/activities/{id}/participants/{pid}/feedback -- 提交反馈

GET    /v1/activities/{id}/statistics               -- 活动数据统计
GET    /v1/activities/{id}/scan-logs                -- 扫码记录
GET    /v1/activities/summary                       -- 活动汇总报表
```

**活动扫码公开 API（无需鉴权）**：
```
GET    /v1/open/activities/{activityNo}/info           -- 活动信息（扫码后展示）
POST   /v1/open/activities/{activityNo}/register       -- 扫码报名（需报名类）
POST   /v1/open/activities/{activityNo}/scan           -- 记录扫码（不需报名类）
GET    /v1/open/activities/{activityNo}/check-registered -- 检查是否已报名（按手机号）
```

**创建需要报名的活动请求**：
```json
POST /v1/activities
{
  "activityName": "2026春季产品发布会",
  "activityCategory": 1,
  "activityType": 3,
  "description": "新一代ERP产品发布会，现场演示+交流",
  "richContent": "<h1>活动议程</h1><p>14:00-14:30 签到...</p>",
  "coverImageUrl": "https://oss.example.com/activity/cover_001.jpg",
  "startTime": "2026-03-15T14:00:00",
  "endTime": "2026-03-15T17:00:00",
  "registrationStartTime": "2026-02-25T00:00:00",
  "registrationEndTime": "2026-03-14T18:00:00",
  "location": "深圳市南山区XX酒店三楼会议厅",
  "maxParticipants": 200,
  "registrationApproval": 0,
  "registrationFormConfig": [
    { "field": "company", "label": "公司名称", "type": "text", "required": true },
    { "field": "position", "label": "职位", "type": "text", "required": false },
    { "field": "interest", "label": "关注模块", "type": "checkbox", "required": false,
      "options": ["库存管理", "财务管理", "供应链", "生产制造"] }
  ],
  "registrationNotice": "请携带名片到场签到",
  "budget": 50000,
  "wechatWorkBind": true
}
```

**创建不需要报名的活动请求**：
```json
POST /v1/activities
{
  "activityName": "2026春季促销活动",
  "activityCategory": 2,
  "activityType": 7,
  "description": "扫码了解春季促销优惠详情",
  "richContent": "<h1>春季特惠</h1><p>即日起至3月31日...</p>",
  "coverImageUrl": "https://oss.example.com/activity/promo_001.jpg",
  "startTime": "2026-03-01T00:00:00",
  "endTime": "2026-03-31T23:59:59",
  "onlineUrl": "https://www.example.com/promo/2026spring",
  "wechatWorkBind": true
}
```

**活动数据统计响应**：
```json
GET /v1/activities/{id}/statistics
{
  "code": 200,
  "data": {
    "activityCategory": 1,
    "basic": {
      "totalScans": 520,
      "uniqueScans": 380,
      "scanTrend": [
        { "date": "2026-03-01", "scans": 45 },
        { "date": "2026-03-02", "scans": 62 }
      ]
    },
    "registration": {
      "totalRegistrations": 210,
      "approved": 195,
      "rejected": 5,
      "pending": 10,
      "waitlisted": 0,
      "scanToRegisterRate": 55.3,
      "registrationTrend": [ ... ]
    },
    "checkin": {
      "checkedIn": 168,
      "notCheckedIn": 27,
      "checkinRate": 86.2
    },
    "conversion": {
      "friendAdded": 145,
      "wechatAdded": 120,
      "convertedToLead": 65,
      "convertedToCustomer": 12,
      "friendRate": 74.4
    },
    "feedback": {
      "totalFeedbacks": 89,
      "avgScore": 4.3,
      "scoreDistribution": { "5": 42, "4": 30, "3": 12, "2": 3, "1": 2 }
    }
  }
}
```

### S7.8 二维码 + 扫码流程

```
┌──────────────────────────────────────────────────────────────────┐
│              活动二维码与扫码流程                                   │
│                                                                    │
│  二维码内容格式：                                                  │
│  https://crm.example.com/a/{activityNo}                           │
│                                                                    │
│  ═══════ 需要报名的活动 ═══════                                   │
│                                                                    │
│  扫码 → H5落地页                                                  │
│  ┌──────────────────────────────────────┐                         │
│  │ 📷 活动封面图                         │                         │
│  │ 📌 2026春季产品发布会                  │                         │
│  │ 📅 2026-03-15 14:00~17:00            │                         │
│  │ 📍 深圳南山XX酒店                     │                         │
│  │ 👥 已报名 168/200 人                  │                         │
│  │                                      │                         │
│  │ ─── 活动详情 ───                     │                         │
│  │ (富文本内容/议程/嘉宾介绍)             │                         │
│  │                                      │                         │
│  │ ─── 报名须知 ───                     │                         │
│  │ 请携带名片到场签到                     │                         │
│  │                                      │                         │
│  │ ─── 立即报名 ───                     │                         │
│  │ 姓名 *  [____________]               │                         │
│  │ 手机 *  [____________]               │                         │
│  │ 公司 *  [____________]               │                         │
│  │ 职位    [____________]               │                         │
│  │ 关注模块 ☐库存 ☐财务 ☐供应链          │                         │
│  │                                      │                         │
│  │         [ 提交报名 ]                  │                         │
│  └──────────────────────────────────────┘                         │
│                                                                    │
│  报名成功后页面：                                                  │
│  ┌──────────────────────────────────────┐                         │
│  │ ✅ 报名成功！                         │                         │
│  │                                      │                         │
│  │ (如需审核) 您的报名已提交，             │                         │
│  │ 审核通过后将短信通知您                  │                         │
│  │                                      │                         │
│  │ (自动通过) 恭喜您报名成功！             │                         │
│  │ 活动时间：2026-03-15 14:00            │                         │
│  │ 活动地点：深圳南山XX酒店               │                         │
│  │                                      │                         │
│  │ 📲 添加好友获取更多资讯                │                         │
│  │ ┌──────────────┐                    │                         │
│  │ │ 企业微信活码   │ (绑定企微时显示)     │                         │
│  │ │ 或平台好友码   │ (未绑定企微时显示)   │                         │
│  │ └──────────────┘                    │                         │
│  └──────────────────────────────────────┘                         │
│                                                                    │
│  ═══════ 不需要报名的活动 ═══════                                  │
│                                                                    │
│  扫码 → H5落地页                                                  │
│  ┌──────────────────────────────────────┐                         │
│  │ 📷 活动封面图                         │                         │
│  │ 📌 2026春季促销活动                    │                         │
│  │ 📅 即日起至2026-03-31                 │                         │
│  │                                      │                         │
│  │ ─── 活动内容 ───                     │                         │
│  │ (富文本详情/促销信息/产品介绍)          │                         │
│  │                                      │                         │
│  │ 📲 扫码添加好友咨询详情                │                         │
│  │ ┌──────────────┐                    │                         │
│  │ │ 企业微信活码   │ (绑定企微时显示)     │                         │
│  │ │ 或平台好友码   │ (未绑定企微时显示)   │                         │
│  │ └──────────────┘                    │                         │
│  │                                      │                         │
│  │ 🔗 了解更多 → (跳转online_url)        │                         │
│  └──────────────────────────────────────┘                         │
│                                                                    │
│  二维码样式：                                                      │
│  ├── 中心Logo: 活动封面或租户Logo                                  │
│  ├── 底部文字: 活动名称                                            │
│  └── 尺寸: 支持下载 300/600/1200px                                 │
└──────────────────────────────────────────────────────────────────┘
```

### S7.9 PC Web 端页面设计

```
PC 端 - 活动列表页
┌───────────────────────────────────────────────────────────────────────┐
│  活动管理                                            [+ 创建活动]     │
│                                                                       │
│  [全部(25)] [需要报名(15)] [不需要报名(10)]                            │
│  [进行中(8)] [报名中(3)] [未开始(5)] [已结束(9)]                       │
│                                                                       │
│  🔍 [搜索活动名称_______]  活动类型 [全部 ▼]  时间 [本月 ▼]           │
│                                                                       │
│  ┌──────┬────────┬──────┬──────┬──────┬──────┬──────┬──────┬─────┐  │
│  │ 封面 │ 活动名称 │ 类别 │ 类型 │ 时间  │ 报名  │ 扫码  │ 状态 │ 操作│  │
│  ├──────┼────────┼──────┼──────┼──────┼──────┼──────┼──────┼─────┤  │
│  │ 🖼️  │ 2026春季│ 需报名│产品  │03-15 │168/  │ 520  │ 🟢  │ 详情│  │
│  │      │ 产品发布│      │发布  │14:00 │200   │      │报名中│ 编辑│  │
│  │      │ 会      │      │      │      │      │      │     │ 更多│  │
│  ├──────┼────────┼──────┼──────┼──────┼──────┼──────┼──────┼─────┤  │
│  │ 🖼️  │ 春季促销│不需要 │优惠  │03-01 │  -   │ 1280 │ 🟢  │ 详情│  │
│  │      │ 活动    │报名  │活动  │~03-31│      │      │进行中│ 编辑│  │
│  │      │        │      │      │      │      │      │     │ 更多│  │
│  ├──────┼────────┼──────┼──────┼──────┼──────┼──────┼──────┼─────┤  │
│  │ 🖼️  │ 客户答谢│ 需报名│客户  │03-28 │ 0/  │   0  │ ⚪  │ 详情│  │
│  │      │ 沙龙    │      │沙龙  │18:00 │ 50   │      │未开始│ 编辑│  │
│  └──────┴────────┴──────┴──────┴──────┴──────┴──────┴──────┴─────┘  │
│                                                                       │
│  共 25 条  < 1 2 3 >                                                  │
└───────────────────────────────────────────────────────────────────────┘
```

```
PC 端 - 创建/编辑活动页（需要报名类）
┌───────────────────────────────────────────────────────────────────────┐
│  ← 创建活动                                    [保存草稿] [发布活动]  │
│                                                                       │
│  ── 基本信息 ──────────────────────────────────────────────────────   │
│                                                                       │
│  活动类别 *    ● 需要报名  ○ 不需要报名                               │
│  活动名称 *    [2026春季产品发布会_______________]                     │
│  活动类型 *    [产品发布 ▼]                                           │
│  活动封面      [📷 上传封面图]    轮播图 [📷 添加]                    │
│                                                                       │
│  ── 时间与地点 ──────────────────────────────────────────────────     │
│                                                                       │
│  活动时间 *    [2026-03-15 14:00] ~ [2026-03-15 17:00]               │
│  活动地点      [深圳市南山区XX酒店三楼会议厅______]  [📍 地图选点]     │
│  线上链接      [https://___________________________]                  │
│                                                                       │
│  ── 报名设置 ──────────────────────────────────────────────────      │
│                                                                       │
│  报名时间 *    [2026-02-25 00:00] ~ [2026-03-14 18:00]               │
│  名额上限      [200] 人    ☐ 开启候补                                │
│  报名审核      ● 自动通过  ○ 需要审核                                │
│  报名须知      [请携带名片到场签到_________________]                   │
│                                                                       │
│  报名表单字段（姓名+手机为必填，以下为自定义字段）：                     │
│  ┌──────────────────────────────────────────────────┐                │
│  │ 字段名        类型          必填    选项          │                │
│  │ 公司名称      [文本输入 ▼]   [✓]                 │  [×]           │
│  │ 职位          [文本输入 ▼]   [ ]                 │  [×]           │
│  │ 关注模块      [多选框 ▼]     [ ]   库存/财务/..   │  [×]           │
│  │                                                  │                │
│  │ [+ 添加字段]                                     │                │
│  └──────────────────────────────────────────────────┘                │
│                                                                       │
│  ── 活动详情（富文本编辑器） ──────────────────────────────────       │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │ B I U  | H1 H2 | 📷 🔗 📊 |  源码                           │    │
│  │──────────────────────────────────────────────────────────────│    │
│  │                                                              │    │
│  │  活动议程、嘉宾介绍、产品亮点 等富文本内容...                   │    │
│  │                                                              │    │
│  └──────────────────────────────────────────────────────────────┘    │
│                                                                       │
│  ── 其他设置 ──────────────────────────────────────────────────      │
│                                                                       │
│  负责人 *      [李明 ▼]    协作人 [+ 添加]                            │
│  活动预算      [50000] 元                                             │
│  企业微信      ☑ 绑定企业微信（报名成功后展示企微活码）                │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
```

```
PC 端 - 活动详情页（需要报名类）
┌───────────────────────────────────────────────────────────────────────┐
│  ← 活动详情                        [编辑] [复制活动] [下载二维码] ... │
│                                                                       │
│  ┌──────────────────────────────┬─────────────────────────────────┐  │
│  │ 🖼️ 封面图                    │  2026春季产品发布会               │  │
│  │                              │  🏷️ 需要报名 · 产品发布           │  │
│  │                              │  🟢 报名中                       │  │
│  │                              │                                 │  │
│  │                              │  📅 2026-03-15 14:00~17:00      │  │
│  │                              │  📍 深圳南山XX酒店三楼            │  │
│  │                              │  📝 报名截止: 2026-03-14 18:00   │  │
│  │                              │  👤 负责人: 李明                  │  │
│  │                              │  💰 预算: ¥50,000               │  │
│  └──────────────────────────────┘                                 │  │
│                                                                       │
│  ── 数据概览 ──────────────────────────────────────────────────      │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐           │
│  │ 扫码量    │ 报名人数  │ 通过审核  │ 已签到   │ 加好友   │           │
│  │   520    │   210    │   195    │   168   │   145    │           │
│  │          │  通过率93%│          │签到率86%│ 转化率74%│           │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘           │
│                                                                       │
│  [活动详情] [参与人管理] [扫码记录] [数据统计]                         │
│  ───────── ^^^^^^^^^^                                                │
│                                                                       │
│  参与人管理 Tab：                                                      │
│  [全部(210)] [待审核(10)] [已通过(195)] [已拒绝(5)] [已签到(168)]      │
│                                                                       │
│  🔍 [搜索姓名/手机___]  来源 [全部 ▼]    [+ 手动添加] [批量导入]      │
│                                           [批量审核通过] [导出]        │
│  ┌──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┐   │
│  │ ☐   │ 姓名 │ 手机  │ 公司 │ 来源 │报名状态│ 签到 │ 好友 │ 操作 │   │
│  ├──────┼──────┼──────┼──────┼──────┼──────┼──────┼──────┼──────┤   │
│  │ ☐   │张三  │138***│示例  │扫码  │✅通过 │✅已签│🟢已加│转线索│   │
│  │      │      │      │科技  │报名  │      │到   │     │ 详情│   │
│  ├──────┼──────┼──────┼──────┼──────┼──────┼──────┼──────┼──────┤   │
│  │ ☐   │李四  │139***│XX   │手动  │⏳待审核│  -  │  -  │审核 │   │
│  │      │      │      │公司  │录入  │      │     │     │ 拒绝│   │
│  └──────┴──────┴──────┴──────┴──────┴──────┴──────┴──────┴──────┘   │
│                                                                       │
│  共 210 条  < 1 2 3 ... 11 >                                         │
│                                                                       │
│  ── 二维码 ──────────────────────                                    │
│  ┌──────────┐                                                        │
│  │ ▓▓▓▓▓▓▓▓ │  活动二维码                                            │
│  │ ▓      ▓ │  链接: https://crm.example.com/a/ACT20260315          │
│  │ ▓ LOGO ▓ │                                                        │
│  │ ▓      ▓ │  [下载 300px] [下载 600px] [下载 1200px]               │
│  │ ▓▓▓▓▓▓▓▓ │  [复制链接]                                            │
│  └──────────┘                                                        │
└───────────────────────────────────────────────────────────────────────┘
```

```
PC 端 - 活动详情页（不需要报名类）
┌───────────────────────────────────────────────────────────────────────┐
│  ← 活动详情                        [编辑] [复制活动] [下载二维码] ... │
│                                                                       │
│  ┌──────────────────────────────┬─────────────────────────────────┐  │
│  │ 🖼️ 封面图                    │  2026春季促销活动                │  │
│  │                              │  🏷️ 不需要报名 · 优惠活动        │  │
│  │                              │  🟢 进行中                      │  │
│  │                              │                                 │  │
│  │                              │  📅 2026-03-01 ~ 2026-03-31    │  │
│  │                              │  🔗 https://www.example.com/... │  │
│  │                              │  👤 负责人: 李明                  │  │
│  └──────────────────────────────┘                                 │  │
│                                                                       │
│  ── 数据概览 ──────────────────────────────────────────────────      │
│  ┌──────────┬──────────┬──────────┬──────────┐                      │
│  │ 总扫码    │ 独立访客  │ 加好友   │ 转线索   │                      │
│  │  1,280   │   890    │   320    │   85    │                      │
│  │          │  去重率70%│ 转化率36%│ 线索率10%│                      │
│  └──────────┴──────────┴──────────┴──────────┘                      │
│                                                                       │
│  [活动详情] [扫码趋势] [好友列表] [数据统计]                           │
│  ───────── ^^^^^^^^^^                                                │
│                                                                       │
│  扫码趋势 Tab：                                                       │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │                    📊 每日扫码趋势                            │    │
│  │  60│          *                                              │    │
│  │  50│       *     *                                           │    │
│  │  40│    *           *    *                                   │    │
│  │  30│ *                 *    *                                 │    │
│  │  20│                          *                              │    │
│  │    └───┬───┬───┬───┬───┬───┬───┬──                          │    │
│  │     03/01 02  03  04  05  06  07                             │    │
│  └──────────────────────────────────────────────────────────────┘    │
│                                                                       │
│  ── 二维码 ──────────────────────                                    │
│  ┌──────────┐                                                        │
│  │ ▓▓▓▓▓▓▓▓ │  活动二维码                                            │
│  │ ▓      ▓ │  [下载 300px] [下载 600px] [下载 1200px]               │
│  │ ▓ LOGO ▓ │  [复制链接]                                            │
│  │ ▓▓▓▓▓▓▓▓ │                                                        │
│  └──────────┘                                                        │
└───────────────────────────────────────────────────────────────────────┘
```

```
PC 端 - 数据统计 Tab（两类活动共用，按类别展示不同指标）
┌───────────────────────────────────────────────────────────────────────┐
│  数据统计                                   时间范围 [活动全周期 ▼]   │
│                                                                       │
│  ── 漏斗分析（需要报名类活动） ──                                     │
│  ┌────────────────────────────────────────────────────────────┐      │
│  │ 扫码量 520 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  100%      │      │
│  │ 报名数 210 ━━━━━━━━━━━━━━━━━━                     40.4%   │      │
│  │ 审核通过195 ━━━━━━━━━━━━━━━━━                     37.5%   │      │
│  │ 签到数 168 ━━━━━━━━━━━━━━━                        32.3%   │      │
│  │ 加好友 145 ━━━━━━━━━━━━━                          27.9%   │      │
│  │ 转线索  65 ━━━━━━                                 12.5%   │      │
│  │ 转客户  12 ━━                                      2.3%   │      │
│  └────────────────────────────────────────────────────────────┘      │
│                                                                       │
│  ── 来源分布 ──           ── 满意度 ──                                │
│  ┌─────────────────┐     ┌─────────────────┐                        │
│  │ 🟦 扫码报名 78%  │     │ ⭐ 4.3 / 5.0    │                        │
│  │ 🟩 手动录入 15%  │     │ 89 条反馈       │                        │
│  │ 🟨 批量导入  7%  │     │ █████ 5星 47%   │                        │
│  └─────────────────┘     │ ████  4星 34%   │                        │
│                           │ ██    3星 13%   │                        │
│                           │ █     2星  3%   │                        │
│                           │ █     1星  2%   │                        │
│                           └─────────────────┘                        │
└───────────────────────────────────────────────────────────────────────┘
```

### S7.10 APP 端页面设计

```
APP - 活动列表
┌──────────────────────────────────────┐
│ ← 活动管理                    [+]    │
├──────────────────────────────────────┤
│                                      │
│  [全部] [需要报名] [不需要报名]        │
│  [进行中] [报名中] [未开始] [已结束]   │
│                                      │
│  ┌──────────────────────────────┐    │
│  │ 🖼️ 2026春季产品发布会         │    │
│  │ 🏷️ 需报名 · 产品发布          │    │
│  │ 📅 03-15 14:00  📍 南山XX酒店 │    │
│  │ 👥 报名 168/200  🟢 报名中    │    │
│  │ [查看详情]  [二维码]           │    │
│  ├──────────────────────────────┤    │
│  │ 🖼️ 春季促销活动               │    │
│  │ 🏷️ 无需报名 · 优惠活动        │    │
│  │ 📅 03-01 ~ 03-31             │    │
│  │ 📊 扫码 1280  好友 320        │    │
│  │ 🟢 进行中                    │    │
│  │ [查看详情]  [二维码]           │    │
│  └──────────────────────────────┘    │
│                                      │
└──────────────────────────────────────┘

APP - 活动详情（需要报名类）
┌──────────────────────────────────────┐
│ ← 活动详情                    ...    │
├──────────────────────────────────────┤
│                                      │
│  🖼️ [封面图]                         │
│                                      │
│  2026春季产品发布会                    │
│  🏷️ 需要报名 · 产品发布   🟢 报名中   │
│  📅 2026-03-15 14:00~17:00          │
│  📍 深圳南山XX酒店三楼                │
│                                      │
│  ── 数据 ──                          │
│  ┌────────┬────────┬────────┐       │
│  │ 扫码520│报名210 │签到168 │       │
│  └────────┴────────┴────────┘       │
│                                      │
│  ── 二维码 ──                        │
│  ┌───────────────────────────┐      │
│  │       ▓▓▓▓▓▓▓▓            │      │
│  │       ▓ LOGO ▓            │      │
│  │       ▓▓▓▓▓▓▓▓            │      │
│  │  [保存到相册] [分享] [复制链接]│      │
│  └───────────────────────────┘      │
│                                      │
│  ── 快捷操作 ──                      │
│  [参与人(210)]  [审核(10)]  [签到]    │
│                                      │
│  ── 活动详情 ──                      │
│  (富文本内容展示)                     │
│                                      │
└──────────────────────────────────────┘

APP - 活动详情（不需要报名类）
┌──────────────────────────────────────┐
│ ← 活动详情                    ...    │
├──────────────────────────────────────┤
│                                      │
│  🖼️ [封面图]                         │
│                                      │
│  春季促销活动                         │
│  🏷️ 无需报名 · 优惠活动   🟢 进行中  │
│  📅 2026-03-01 ~ 2026-03-31         │
│  🔗 www.example.com/promo/...        │
│                                      │
│  ── 数据 ──                          │
│  ┌────────┬────────┬────────┐       │
│  │扫码1280│访客 890│好友 320│       │
│  └────────┴────────┴────────┘       │
│                                      │
│  ── 二维码 ──                        │
│  ┌───────────────────────────┐      │
│  │       ▓▓▓▓▓▓▓▓            │      │
│  │       ▓ LOGO ▓            │      │
│  │       ▓▓▓▓▓▓▓▓            │      │
│  │  [保存到相册] [分享] [复制链接]│      │
│  └───────────────────────────┘      │
│                                      │
│  ── 扫码趋势 ──                      │
│  (迷你折线图)                        │
│                                      │
│  ── 活动内容 ──                      │
│  (富文本内容展示)                     │
│                                      │
└──────────────────────────────────────┘

APP - 扫码签到（管理者在活动现场使用）
┌──────────────────────────────────────┐
│ ← 活动签到                           │
│ 2026春季产品发布会                     │
├──────────────────────────────────────┤
│                                      │
│  已签到 168 / 195 (已通过)            │
│                                      │
│  ┌──────────────────────────────┐    │
│  │                              │    │
│  │      📷 扫描参与人二维码       │    │
│  │         或手动搜索            │    │
│  │                              │    │
│  └──────────────────────────────┘    │
│                                      │
│  🔍 [搜索姓名/手机号__________]      │
│                                      │
│  最近签到：                           │
│  ✅ 14:02  张三 (示例科技)            │
│  ✅ 14:05  王芳 (XX公司)              │
│  ✅ 14:08  李明 (YY集团)              │
│                                      │
└──────────────────────────────────────┘
```

---

## S8. 活动扫码 + 企业微信/平台好友

### S8.1 扫码后的好友添加流程

```
客户扫活动二维码
        │
        ▼
┌───────────────────┐
│ 判断活动类别        │
└─────────┬─────────┘
          │
    ┌─────┴──────┐
    ▼            ▼
需要报名      不需要报名
    │            │
    ▼            ▼
┌─────────┐  ┌──────────────┐
│落地页     │  │落地页          │
│活动详情   │  │活动内容展示    │
│+报名表单  │  │记录扫码        │
└────┬────┘  └──────┬───────┘
     │               │
     ▼               │
 提交报名             │
 (审核通过后           │
  或自动通过)          │
     │               │
     └──────┬────────┘
            ▼
┌───────────────────┐
│ 检查租户是否        │
│ 绑定企业微信？      │
└─────────┬─────────┘
     是   │   否
     ▼        ▼
┌──────────┐  ┌────────────┐
│展示企微活码│  │展示平台好友码│
│同时加企微  │  │添加平台好友  │
│+平台好友   │  │记录好友关系  │
└──────────┘  └────────────┘
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

enterprise_query_cache (1) ── (N) enterprise_contact (按credit_code关联)
enterprise_contact (0..1) ── (1) customer_contact (导入后关联)
enterprise_contact_query_log (N) ── (1) user (查询人)

user (1) ──── (N) location_report
```
