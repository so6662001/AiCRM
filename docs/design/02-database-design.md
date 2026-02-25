# AiCRM 数据库设计文档

## 1. 数据库整体策略

### 1.1 分库策略

| 数据库 | 包含表 | 说明 |
|---|---|---|
| `aicrm_tenant` | 租户、组织、用户角色相关表 | 平台级数据 |
| `aicrm_biz` | 线索、客户、商机、跟进等业务表 | 核心业务数据（按租户ID分表） |
| `aicrm_fieldwork` | 外勤、打卡、拜访、录音等表 | 外勤相关数据 |
| `aicrm_task` | 任务相关表 | 任务管理 |
| `aicrm_file` | 文件元数据表 | 文件信息记录 |
| `aicrm_activity` | 活动、参与人相关表 | 活动管理数据 |
| `aicrm_social` | 好友关系、企业微信相关表 | 社交关系数据 |
| `aicrm_audit` | 操作日志、审计表 | 审计数据（量大，定期归档） |

### 1.2 分表策略（ShardingSphere）

对于高数据量表，按 `tenant_id` 进行分表：

```
customer          → customer_0 ~ customer_31    (32张表)
lead              → lead_0 ~ lead_31            (32张表)
follow_up_record  → follow_up_record_0 ~ _31    (32张表)
audit_log         → 按月分表 audit_log_202601 ...
```

### 1.3 公共字段规范

所有业务表包含以下公共字段：

```sql
`id`              BIGINT       NOT NULL COMMENT '主键（雪花算法）',
`tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
`created_by`      BIGINT       DEFAULT NULL COMMENT '创建人ID',
`created_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_by`      BIGINT       DEFAULT NULL COMMENT '更新人ID',
`updated_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
`version`         INT          NOT NULL DEFAULT 1 COMMENT '乐观锁版本号'
```

---

## 2. 租户与组织表设计

### 2.1 租户表 (tenant)

```sql
CREATE TABLE `tenant` (
  `id`                BIGINT       NOT NULL COMMENT '租户ID',
  `tenant_code`       VARCHAR(64)  NOT NULL COMMENT '租户编码（唯一）',
  `tenant_name`       VARCHAR(128) NOT NULL COMMENT '租户名称',
  `logo_url`          VARCHAR(512) DEFAULT NULL COMMENT '租户Logo',
  `contact_name`      VARCHAR(64)  DEFAULT NULL COMMENT '联系人',
  `contact_phone`     VARCHAR(32)  DEFAULT NULL COMMENT '联系电话',
  `contact_email`     VARCHAR(128) DEFAULT NULL COMMENT '联系邮箱',
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-禁用 1-启用 2-试用）',
  `edition`           VARCHAR(32)  NOT NULL DEFAULT 'basic' COMMENT '版本（basic/pro/enterprise）',
  `max_users`         INT          NOT NULL DEFAULT 10 COMMENT '最大用户数',
  `expire_date`       DATE         DEFAULT NULL COMMENT '到期时间',
  `storage_quota_mb`  BIGINT       NOT NULL DEFAULT 5120 COMMENT '存储配额(MB)',
  `storage_used_mb`   BIGINT       NOT NULL DEFAULT 0 COMMENT '已用存储(MB)',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';
```

### 2.2 租户 ERP 配置表 (tenant_erp_config)

```sql
CREATE TABLE `tenant_erp_config` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL COMMENT '租户ID',
  `erp_type`          VARCHAR(32)  NOT NULL COMMENT 'ERP类型（standard/kingdee/yonyou/sap/custom）',
  `erp_name`          VARCHAR(128) NOT NULL COMMENT 'ERP名称',
  `api_base_url`      VARCHAR(512) NOT NULL COMMENT 'ERP API基础地址',
  `auth_type`         VARCHAR(32)  NOT NULL DEFAULT 'api_key' COMMENT '认证方式（api_key/oauth2/basic）',
  `auth_config`       TEXT         DEFAULT NULL COMMENT '认证配置（加密JSON）',
  `field_mapping`     TEXT         DEFAULT NULL COMMENT '字段映射配置（JSON）',
  `sync_strategy`     VARCHAR(32)  NOT NULL DEFAULT 'manual' COMMENT '同步策略（manual/scheduled/realtime）',
  `sync_cron`         VARCHAR(64)  DEFAULT NULL COMMENT '定时同步Cron表达式',
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
  `last_sync_time`    DATETIME     DEFAULT NULL COMMENT '最后同步时间',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户ERP配置表';
```

### 2.3 组织架构表 (organization)

```sql
CREATE TABLE `organization` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL COMMENT '租户ID',
  `parent_id`         BIGINT       DEFAULT 0 COMMENT '上级组织ID（0=顶级）',
  `org_name`          VARCHAR(128) NOT NULL COMMENT '组织名称',
  `org_code`          VARCHAR(64)  DEFAULT NULL COMMENT '组织编码',
  `org_type`          TINYINT      NOT NULL DEFAULT 1 COMMENT '类型（1-公司 2-部门 3-团队）',
  `org_path`          VARCHAR(512) NOT NULL COMMENT '层级路径（如 /1/3/7/）',
  `leader_user_id`    BIGINT       DEFAULT NULL COMMENT '负责人用户ID',
  `sort_order`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_parent` (`tenant_id`, `parent_id`),
  KEY `idx_org_path` (`tenant_id`, `org_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织架构表';
```

### 2.4 角色表 (role)

```sql
CREATE TABLE `role` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `role_code`         VARCHAR(64)  NOT NULL COMMENT '角色编码',
  `role_name`         VARCHAR(128) NOT NULL COMMENT '角色名称',
  `role_type`         TINYINT      NOT NULL DEFAULT 2 COMMENT '类型（1-系统内置 2-自定义）',
  `data_scope`        TINYINT      NOT NULL DEFAULT 4 COMMENT '数据范围（1-全部 2-本部门及以下 3-本部门 4-仅本人）',
  `remark`            VARCHAR(512) DEFAULT NULL,
  `status`            TINYINT      NOT NULL DEFAULT 1,
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_role_code` (`tenant_id`, `role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

### 2.5 角色权限表 (role_permission)

```sql
CREATE TABLE `role_permission` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `role_id`           BIGINT       NOT NULL,
  `permission_code`   VARCHAR(128) NOT NULL COMMENT '权限编码（如 lead:create, customer:view）',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`tenant_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';
```

### 2.6 用户角色关联表 (user_role)

```sql
CREATE TABLE `user_role` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `user_id`           BIGINT       NOT NULL COMMENT '用户ID（来自现有系统）',
  `role_id`           BIGINT       NOT NULL,
  `org_id`            BIGINT       NOT NULL COMMENT '所属组织ID',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_user_role` (`tenant_id`, `user_id`, `role_id`),
  KEY `idx_org_id` (`tenant_id`, `org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
```

---

## 3. 线索管理表设计

### 3.1 线索表 (lead)

```sql
CREATE TABLE `lead` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `lead_no`           VARCHAR(32)  NOT NULL COMMENT '线索编号',
  `contact_name`      VARCHAR(64)  NOT NULL COMMENT '联系人姓名',
  `contact_phone`     VARCHAR(32)  DEFAULT NULL COMMENT '联系电话',
  `contact_email`     VARCHAR(128) DEFAULT NULL COMMENT '联系邮箱',
  `company_name`      VARCHAR(256) DEFAULT NULL COMMENT '公司名称',
  `position`          VARCHAR(64)  DEFAULT NULL COMMENT '职位',
  `source`            VARCHAR(32)  NOT NULL COMMENT '来源（manual/import/advertising/website/referral）',
  `source_detail`     VARCHAR(256) DEFAULT NULL COMMENT '来源详情',
  `intention_level`   CHAR(1)      DEFAULT NULL COMMENT '意向等级（A/B/C/D）',
  `lead_score`        INT          DEFAULT 0 COMMENT '线索评分',
  `status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0-待分配 1-已分配 2-跟进中 3-已转化 4-已退回 5-无效）',
  `owner_user_id`     BIGINT       DEFAULT NULL COMMENT '负责人（销售员）',
  `owner_org_id`      BIGINT       DEFAULT NULL COMMENT '负责人所属组织',
  `assign_user_id`    BIGINT       DEFAULT NULL COMMENT '分配人',
  `assign_time`       DATETIME     DEFAULT NULL COMMENT '分配时间',
  `first_follow_time` DATETIME     DEFAULT NULL COMMENT '首次跟进时间',
  `last_follow_time`  DATETIME     DEFAULT NULL COMMENT '最后跟进时间',
  `follow_count`      INT          NOT NULL DEFAULT 0 COMMENT '跟进次数',
  `convert_time`      DATETIME     DEFAULT NULL COMMENT '转化时间',
  `convert_customer_id` BIGINT     DEFAULT NULL COMMENT '转化后的客户ID',
  `return_reason`     VARCHAR(512) DEFAULT NULL COMMENT '退回原因',
  `in_pool`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否在公海池（0-否 1-是）',
  `pool_enter_time`   DATETIME     DEFAULT NULL COMMENT '进入公海池时间',
  `province`          VARCHAR(32)  DEFAULT NULL COMMENT '省份',
  `city`              VARCHAR(32)  DEFAULT NULL COMMENT '城市',
  `industry`          VARCHAR(64)  DEFAULT NULL COMMENT '行业',
  `remark`            TEXT         DEFAULT NULL COMMENT '备注',
  `ext_attrs`         JSON         DEFAULT NULL COMMENT '扩展属性（自定义字段）',
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  `version`           INT          NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_lead_no` (`tenant_id`, `lead_no`),
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_tenant_owner` (`tenant_id`, `owner_user_id`),
  KEY `idx_tenant_pool` (`tenant_id`, `in_pool`),
  KEY `idx_tenant_created` (`tenant_id`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索表';
```

### 3.2 线索分配记录表 (lead_assign_log)

```sql
CREATE TABLE `lead_assign_log` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `lead_id`           BIGINT       NOT NULL COMMENT '线索ID',
  `assign_type`       TINYINT      NOT NULL COMMENT '分配类型（1-手动分配 2-自动分配 3-经理再分配 4-退回 5-领取）',
  `from_user_id`      BIGINT       DEFAULT NULL COMMENT '来源用户（退回/转移时）',
  `to_user_id`        BIGINT       NOT NULL COMMENT '目标用户',
  `assign_by`         BIGINT       NOT NULL COMMENT '操作人',
  `remark`            VARCHAR(512) DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_lead` (`tenant_id`, `lead_id`),
  KEY `idx_tenant_to_user` (`tenant_id`, `to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索分配记录表';
```

### 3.3 公海池配置表 (lead_pool_config)

```sql
CREATE TABLE `lead_pool_config` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `pool_name`         VARCHAR(128) NOT NULL COMMENT '公海池名称',
  `recycle_days`      INT          NOT NULL DEFAULT 7 COMMENT '未跟进回收天数',
  `max_hold_count`    INT          NOT NULL DEFAULT 50 COMMENT '单人最大持有线索数',
  `daily_pick_limit`  INT          NOT NULL DEFAULT 5 COMMENT '每日领取上限',
  `visible_org_ids`   VARCHAR(1024) DEFAULT NULL COMMENT '可见组织ID列表（逗号分隔）',
  `status`            TINYINT      NOT NULL DEFAULT 1,
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公海池配置表';
```

---

## 4. 客户管理表设计

### 4.1 客户表 (customer)

```sql
CREATE TABLE `customer` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `customer_no`       VARCHAR(32)  NOT NULL COMMENT '客户编号',
  `customer_name`     VARCHAR(256) NOT NULL COMMENT '客户名称（公司名）',
  `short_name`        VARCHAR(128) DEFAULT NULL COMMENT '简称',
  `customer_type`     TINYINT      NOT NULL DEFAULT 1 COMMENT '类型（1-企业 2-个人）',
  `industry`          VARCHAR(64)  DEFAULT NULL COMMENT '行业',
  `scale`             VARCHAR(32)  DEFAULT NULL COMMENT '企业规模',
  `website`           VARCHAR(256) DEFAULT NULL COMMENT '网站',
  `province`          VARCHAR(32)  DEFAULT NULL COMMENT '省份',
  `city`              VARCHAR(32)  DEFAULT NULL COMMENT '城市',
  `district`          VARCHAR(32)  DEFAULT NULL COMMENT '区县',
  `address`           VARCHAR(512) DEFAULT NULL COMMENT '详细地址',
  `longitude`         DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `latitude`          DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `lifecycle_stage`   TINYINT      NOT NULL DEFAULT 1 COMMENT '生命周期（1-潜在 2-意向 3-成交 4-活跃 5-VIP 6-流失 7-无效 8-黑名单）',
  `level`             CHAR(1)      DEFAULT NULL COMMENT '客户等级（A/B/C/D）',
  `owner_user_id`     BIGINT       NOT NULL COMMENT '负责人',
  `owner_org_id`      BIGINT       DEFAULT NULL COMMENT '负责人所属组织',
  `source_lead_id`    BIGINT       DEFAULT NULL COMMENT '来源线索ID',
  `source`            VARCHAR(32)  DEFAULT NULL COMMENT '来源（lead_convert/manual/import/erp_sync）',
  `last_follow_time`  DATETIME     DEFAULT NULL COMMENT '最后跟进时间',
  `follow_count`      INT          NOT NULL DEFAULT 0 COMMENT '跟进次数',
  `next_follow_time`  DATETIME     DEFAULT NULL COMMENT '下次计划跟进时间',
  `expected_purchase_date` DATE    DEFAULT NULL COMMENT '预计采购日期',
  `deal_amount`       DECIMAL(15,2) DEFAULT 0 COMMENT '累计成交金额',
  `deal_count`        INT          NOT NULL DEFAULT 0 COMMENT '成交次数',
  `erp_customer_id`   VARCHAR(64)  DEFAULT NULL COMMENT 'ERP客户ID（同步用）',
  `erp_sync_status`   TINYINT      DEFAULT 0 COMMENT 'ERP同步状态（0-未同步 1-已同步 2-同步中 3-同步失败）',
  `erp_last_sync_time` DATETIME    DEFAULT NULL COMMENT 'ERP最后同步时间',
  `approval_status`   TINYINT      DEFAULT 0 COMMENT '审核状态（0-无需审核 1-待审核 2-审核通过 3-审核拒绝）',
  `remark`            TEXT         DEFAULT NULL,
  `tags`              VARCHAR(512) DEFAULT NULL COMMENT '标签（逗号分隔）',
  `ext_attrs`         JSON         DEFAULT NULL COMMENT '扩展属性',
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  `version`           INT          NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_customer_no` (`tenant_id`, `customer_no`),
  KEY `idx_tenant_owner` (`tenant_id`, `owner_user_id`),
  KEY `idx_tenant_stage` (`tenant_id`, `lifecycle_stage`),
  KEY `idx_tenant_erp` (`tenant_id`, `erp_customer_id`),
  KEY `idx_tenant_name` (`tenant_id`, `customer_name`),
  KEY `idx_tenant_created` (`tenant_id`, `created_time`),
  KEY `idx_tenant_purchase_date` (`tenant_id`, `expected_purchase_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';
```

### 4.2 客户联系人表 (customer_contact)

```sql
CREATE TABLE `customer_contact` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `customer_id`       BIGINT       NOT NULL COMMENT '客户ID',
  `contact_name`      VARCHAR(64)  NOT NULL COMMENT '联系人姓名',
  `gender`            TINYINT      DEFAULT NULL COMMENT '性别（0-未知 1-男 2-女）',
  `position`          VARCHAR(64)  DEFAULT NULL COMMENT '职位',
  `department`        VARCHAR(64)  DEFAULT NULL COMMENT '部门',
  `phone`             VARCHAR(32)  DEFAULT NULL COMMENT '手机号',
  `telephone`         VARCHAR(32)  DEFAULT NULL COMMENT '座机',
  `email`             VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `wechat`            VARCHAR(64)  DEFAULT NULL COMMENT '微信号',
  `is_primary`        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否主联系人',
  `is_decision_maker` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否决策人',
  `remark`            VARCHAR(512) DEFAULT NULL,
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_customer` (`tenant_id`, `customer_id`),
  KEY `idx_phone` (`tenant_id`, `phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户联系人表';
```

### 4.3 客户审核表 (customer_approval)

```sql
CREATE TABLE `customer_approval` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `customer_id`       BIGINT       NOT NULL COMMENT '客户ID',
  `approval_type`     TINYINT      NOT NULL COMMENT '审核类型（1-新增审核 2-ERP下发审核）',
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（1-待审核 2-通过 3-拒绝）',
  `applicant_id`      BIGINT       NOT NULL COMMENT '申请人',
  `approver_id`       BIGINT       DEFAULT NULL COMMENT '审核人',
  `approve_time`      DATETIME     DEFAULT NULL COMMENT '审核时间',
  `approve_remark`    VARCHAR(512) DEFAULT NULL COMMENT '审核意见',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_customer` (`tenant_id`, `customer_id`),
  KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户审核表';
```

---

## 5. 商机管理表设计

### 5.1 商机表 (opportunity)

```sql
CREATE TABLE `opportunity` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `opportunity_no`    VARCHAR(32)  NOT NULL COMMENT '商机编号',
  `opportunity_name`  VARCHAR(256) NOT NULL COMMENT '商机名称',
  `customer_id`       BIGINT       NOT NULL COMMENT '关联客户ID',
  `contact_id`        BIGINT       DEFAULT NULL COMMENT '关联联系人ID',
  `stage_id`          BIGINT       NOT NULL COMMENT '当前阶段ID',
  `expected_amount`   DECIMAL(15,2) DEFAULT NULL COMMENT '预计金额',
  `actual_amount`     DECIMAL(15,2) DEFAULT NULL COMMENT '实际成交金额',
  `win_rate`          INT          DEFAULT NULL COMMENT '赢率（%）',
  `expected_close_date` DATE       DEFAULT NULL COMMENT '预计成交日期',
  `actual_close_date` DATE         DEFAULT NULL COMMENT '实际成交日期',
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（1-进行中 2-赢单 3-输单 4-无效）',
  `loss_reason`       VARCHAR(512) DEFAULT NULL COMMENT '输单原因',
  `competitor`        VARCHAR(256) DEFAULT NULL COMMENT '竞争对手',
  `owner_user_id`     BIGINT       NOT NULL COMMENT '负责人',
  `owner_org_id`      BIGINT       DEFAULT NULL COMMENT '负责人所属组织',
  `last_follow_time`  DATETIME     DEFAULT NULL COMMENT '最后跟进时间',
  `follow_count`      INT          NOT NULL DEFAULT 0 COMMENT '跟进次数',
  `remark`            TEXT         DEFAULT NULL,
  `ext_attrs`         JSON         DEFAULT NULL,
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  `version`           INT          NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_opp_no` (`tenant_id`, `opportunity_no`),
  KEY `idx_tenant_customer` (`tenant_id`, `customer_id`),
  KEY `idx_tenant_owner` (`tenant_id`, `owner_user_id`),
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_tenant_stage` (`tenant_id`, `stage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机表';
```

### 5.2 商机阶段配置表 (opportunity_stage_config)

```sql
CREATE TABLE `opportunity_stage_config` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `stage_name`        VARCHAR(64)  NOT NULL COMMENT '阶段名称',
  `stage_code`        VARCHAR(32)  NOT NULL COMMENT '阶段编码',
  `win_rate`          INT          NOT NULL DEFAULT 0 COMMENT '默认赢率（%）',
  `sort_order`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `is_won`            TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否为赢单阶段',
  `is_lost`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否为输单阶段',
  `status`            TINYINT      NOT NULL DEFAULT 1,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机阶段配置表';
```

### 5.3 商机阶段变更记录表 (opportunity_stage_log)

```sql
CREATE TABLE `opportunity_stage_log` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `opportunity_id`    BIGINT       NOT NULL COMMENT '商机ID',
  `from_stage_id`     BIGINT       DEFAULT NULL COMMENT '变更前阶段',
  `to_stage_id`       BIGINT       NOT NULL COMMENT '变更后阶段',
  `stay_days`         INT          DEFAULT NULL COMMENT '上一阶段停留天数',
  `remark`            VARCHAR(512) DEFAULT NULL,
  `operated_by`       BIGINT       NOT NULL COMMENT '操作人',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_opp` (`tenant_id`, `opportunity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机阶段变更记录表';
```

---

## 6. 跟进管理表设计

### 6.1 跟进记录表 (follow_up_record)

```sql
CREATE TABLE `follow_up_record` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `biz_type`          TINYINT      NOT NULL COMMENT '关联业务类型（1-线索 2-客户 3-商机）',
  `biz_id`            BIGINT       NOT NULL COMMENT '关联业务ID',
  `customer_id`       BIGINT       DEFAULT NULL COMMENT '客户ID（冗余，方便查询）',
  `follow_type`       TINYINT      NOT NULL COMMENT '跟进方式（1-现场拜访 2-电话 3-微信 4-邮件 5-企业微信 6-其他）',
  `content`           TEXT         NOT NULL COMMENT '跟进内容',
  `visit_id`          BIGINT       DEFAULT NULL COMMENT '关联拜访记录ID',
  `next_follow_time`  DATETIME     DEFAULT NULL COMMENT '下次跟进时间',
  `next_follow_note`  VARCHAR(512) DEFAULT NULL COMMENT '下次跟进备注',
  `has_recording`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否有录音',
  `recording_file_id` BIGINT       DEFAULT NULL COMMENT '录音文件ID',
  `ai_summary`        TEXT         DEFAULT NULL COMMENT 'AI摘要',
  `ai_analysis`       TEXT         DEFAULT NULL COMMENT 'AI分析结果',
  `has_violation`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否有违规',
  `violation_detail`  TEXT         DEFAULT NULL COMMENT '违规详情',
  `follow_user_id`    BIGINT       NOT NULL COMMENT '跟进人',
  `follow_user_org_id` BIGINT      DEFAULT NULL COMMENT '跟进人所属组织',
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_biz` (`tenant_id`, `biz_type`, `biz_id`),
  KEY `idx_tenant_customer` (`tenant_id`, `customer_id`),
  KEY `idx_tenant_user` (`tenant_id`, `follow_user_id`),
  KEY `idx_tenant_created` (`tenant_id`, `created_time`),
  KEY `idx_has_violation` (`tenant_id`, `has_violation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跟进记录表';
```

### 6.2 跟进附件表 (follow_up_attachment)

```sql
CREATE TABLE `follow_up_attachment` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `follow_up_id`      BIGINT       NOT NULL COMMENT '跟进记录ID',
  `file_name`         VARCHAR(256) NOT NULL COMMENT '文件名',
  `file_type`         VARCHAR(32)  NOT NULL COMMENT '文件类型（image/audio/video/document）',
  `file_size`         BIGINT       NOT NULL COMMENT '文件大小（字节）',
  `file_url`          VARCHAR(1024) NOT NULL COMMENT '文件URL（OSS地址）',
  `sort_order`        INT          NOT NULL DEFAULT 0,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_follow` (`tenant_id`, `follow_up_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跟进附件表';
```

---

## 7. 外勤管理表设计

### 7.1 拜访记录表 (visit_record)

```sql
CREATE TABLE `visit_record` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `visit_no`          VARCHAR(32)  NOT NULL COMMENT '拜访编号',
  `customer_id`       BIGINT       NOT NULL COMMENT '客户ID',
  `contact_id`        BIGINT       DEFAULT NULL COMMENT '联系人ID',
  `visit_type`        TINYINT      NOT NULL COMMENT '拜访方式（1-现场拜访 2-电话拜访 3-微信拜访 4-企业微信 5-视频会议）',
  `visit_purpose`     VARCHAR(256) DEFAULT NULL COMMENT '拜访目的',
  `visit_result`      TEXT         DEFAULT NULL COMMENT '拜访结果',
  `visit_time`        DATETIME     NOT NULL COMMENT '拜访时间',
  `visit_end_time`    DATETIME     DEFAULT NULL COMMENT '拜访结束时间',
  `visit_duration`    INT          DEFAULT NULL COMMENT '拜访时长（分钟）',

  -- 现场拜访字段
  `checkin_address`   VARCHAR(512) DEFAULT NULL COMMENT '签到地址',
  `checkin_longitude`  DECIMAL(10,7) DEFAULT NULL COMMENT '签到经度',
  `checkin_latitude`   DECIMAL(10,7) DEFAULT NULL COMMENT '签到纬度',
  `checkin_time`      DATETIME     DEFAULT NULL COMMENT '签到时间',
  `checkin_photo_url`  VARCHAR(1024) DEFAULT NULL COMMENT '签到照片URL',
  `checkout_time`     DATETIME     DEFAULT NULL COMMENT '签退时间',
  `checkout_address`  VARCHAR(512) DEFAULT NULL COMMENT '签退地址',

  -- 电话拜访字段
  `call_phone`        VARCHAR(32)  DEFAULT NULL COMMENT '通话号码',
  `call_duration`     INT          DEFAULT NULL COMMENT '通话时长（秒）',
  `recording_file_id` BIGINT       DEFAULT NULL COMMENT '录音文件ID',
  `recording_url`     VARCHAR(1024) DEFAULT NULL COMMENT '录音文件URL',
  `ai_transcription`  TEXT         DEFAULT NULL COMMENT 'AI转写文本',
  `ai_summary`        TEXT         DEFAULT NULL COMMENT 'AI内容摘要',
  `ai_sentiment`      VARCHAR(32)  DEFAULT NULL COMMENT 'AI情感分析（positive/neutral/negative）',
  `has_violation`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否有违规',
  `violation_words`   TEXT         DEFAULT NULL COMMENT '违规用词详情（JSON）',

  -- 微信/企业微信拜访字段
  `chat_screenshot_urls` TEXT      DEFAULT NULL COMMENT '聊天截图URL列表（JSON数组）',

  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（1-计划中 2-进行中 3-已完成 4-已取消）',
  `visitor_user_id`   BIGINT       NOT NULL COMMENT '拜访人',
  `visitor_org_id`    BIGINT       DEFAULT NULL COMMENT '拜访人所属组织',
  `follow_up_id`      BIGINT       DEFAULT NULL COMMENT '关联跟进记录ID',
  `remark`            TEXT         DEFAULT NULL,
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_visit_no` (`tenant_id`, `visit_no`),
  KEY `idx_tenant_customer` (`tenant_id`, `customer_id`),
  KEY `idx_tenant_visitor` (`tenant_id`, `visitor_user_id`),
  KEY `idx_tenant_visit_time` (`tenant_id`, `visit_time`),
  KEY `idx_tenant_type` (`tenant_id`, `visit_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拜访记录表';
```

### 7.2 签到打卡表 (checkin_record)

```sql
CREATE TABLE `checkin_record` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `user_id`           BIGINT       NOT NULL COMMENT '用户ID',
  `checkin_type`      TINYINT      NOT NULL COMMENT '类型（1-外出打卡 2-客户拜访签到 3-日常考勤）',
  `checkin_time`      DATETIME     NOT NULL COMMENT '打卡时间',
  `address`           VARCHAR(512) DEFAULT NULL COMMENT '打卡地址',
  `longitude`         DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `latitude`          DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `photo_url`         VARCHAR(1024) DEFAULT NULL COMMENT '打卡照片',
  `wifi_name`         VARCHAR(128) DEFAULT NULL COMMENT 'WiFi名称（可选定位辅助）',
  `device_info`       VARCHAR(256) DEFAULT NULL COMMENT '设备信息',
  `related_visit_id`  BIGINT       DEFAULT NULL COMMENT '关联拜访记录ID',
  `remark`            VARCHAR(512) DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user_time` (`tenant_id`, `user_id`, `checkin_time`),
  KEY `idx_tenant_type` (`tenant_id`, `checkin_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到打卡表';
```

### 7.3 电话录音表 (call_recording)

```sql
CREATE TABLE `call_recording` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `user_id`           BIGINT       NOT NULL COMMENT '销售人员ID',
  `customer_id`       BIGINT       DEFAULT NULL COMMENT '客户ID',
  `contact_id`        BIGINT       DEFAULT NULL COMMENT '联系人ID',
  `visit_id`          BIGINT       DEFAULT NULL COMMENT '关联拜访ID',
  `call_type`         TINYINT      NOT NULL COMMENT '通话类型（1-呼出 2-呼入）',
  `caller_number`     VARCHAR(32)  DEFAULT NULL COMMENT '主叫号码',
  `callee_number`     VARCHAR(32)  DEFAULT NULL COMMENT '被叫号码',
  `call_start_time`   DATETIME     NOT NULL COMMENT '通话开始时间',
  `call_end_time`     DATETIME     DEFAULT NULL COMMENT '通话结束时间',
  `call_duration`     INT          DEFAULT 0 COMMENT '通话时长（秒）',
  `recording_file_url` VARCHAR(1024) DEFAULT NULL COMMENT '录音文件URL',
  `recording_file_size` BIGINT     DEFAULT NULL COMMENT '录音文件大小（字节）',
  `recording_format`  VARCHAR(16)  DEFAULT NULL COMMENT '录音格式（mp3/wav/amr）',
  `transcription_status` TINYINT   DEFAULT 0 COMMENT '转写状态（0-未转写 1-转写中 2-已转写 3-转写失败）',
  `transcription_text` LONGTEXT    DEFAULT NULL COMMENT '转写文本',
  `ai_summary`        TEXT         DEFAULT NULL COMMENT 'AI摘要',
  `ai_keywords`       VARCHAR(512) DEFAULT NULL COMMENT 'AI关键词',
  `ai_sentiment`      VARCHAR(32)  DEFAULT NULL COMMENT 'AI情感分析',
  `ai_action_items`   TEXT         DEFAULT NULL COMMENT 'AI提取的行动项（JSON）',
  `violation_check_status` TINYINT DEFAULT 0 COMMENT '违规检查状态（0-未检查 1-检查中 2-已检查）',
  `has_violation`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否有违规',
  `violation_detail`  TEXT         DEFAULT NULL COMMENT '违规详情（JSON）',
  `violation_level`   TINYINT      DEFAULT NULL COMMENT '违规等级（1-低 2-中 3-高）',
  `source`            VARCHAR(32)  DEFAULT NULL COMMENT '录音来源（app_auto/manual_upload/enterprise_wechat）',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user` (`tenant_id`, `user_id`),
  KEY `idx_tenant_customer` (`tenant_id`, `customer_id`),
  KEY `idx_tenant_visit` (`tenant_id`, `visit_id`),
  KEY `idx_violation` (`tenant_id`, `has_violation`),
  KEY `idx_call_time` (`tenant_id`, `call_start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电话录音表';
```

### 7.4 违规词库表 (violation_word)

```sql
CREATE TABLE `violation_word` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `word`              VARCHAR(128) NOT NULL COMMENT '违规词',
  `category`          VARCHAR(64)  DEFAULT NULL COMMENT '分类（虚假承诺/过度宣传/恶意攻击/其他）',
  `level`             TINYINT      NOT NULL DEFAULT 1 COMMENT '等级（1-低 2-中 3-高）',
  `status`            TINYINT      NOT NULL DEFAULT 1,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_word` (`tenant_id`, `word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违规词库表';
```

---

## 8. 任务管理表设计

### 8.1 工作任务表 (work_task)

```sql
CREATE TABLE `work_task` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `task_no`           VARCHAR(32)  NOT NULL COMMENT '任务编号',
  `task_title`        VARCHAR(256) NOT NULL COMMENT '任务标题',
  `task_content`      TEXT         DEFAULT NULL COMMENT '任务内容',
  `task_type`         TINYINT      NOT NULL COMMENT '类型（1-客户拜访 2-电话跟进 3-商机推进 4-资料整理 5-其他）',
  `priority`          TINYINT      NOT NULL DEFAULT 2 COMMENT '优先级（1-低 2-中 3-高 4-紧急）',
  `status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0-待开始 1-进行中 2-已完成 3-已取消 4-已逾期）',
  `plan_start_time`   DATETIME     DEFAULT NULL COMMENT '计划开始时间',
  `plan_end_time`     DATETIME     DEFAULT NULL COMMENT '计划结束时间',
  `actual_start_time` DATETIME     DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time`   DATETIME     DEFAULT NULL COMMENT '实际结束时间',
  `completion_note`   TEXT         DEFAULT NULL COMMENT '完成说明',
  `completion_rate`   INT          DEFAULT 0 COMMENT '完成率（%）',
  `assign_type`       TINYINT      NOT NULL DEFAULT 1 COMMENT '来源（1-自主创建 2-上级安排）',
  `assignee_user_id`  BIGINT       NOT NULL COMMENT '执行人',
  `assigner_user_id`  BIGINT       DEFAULT NULL COMMENT '安排人（上级安排时）',
  `related_biz_type`  TINYINT      DEFAULT NULL COMMENT '关联业务类型（1-线索 2-客户 3-商机）',
  `related_biz_id`    BIGINT       DEFAULT NULL COMMENT '关联业务ID',
  `related_visit_id`  BIGINT       DEFAULT NULL COMMENT '关联拜访ID',
  `remind_time`       DATETIME     DEFAULT NULL COMMENT '提醒时间',
  `is_reminded`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已提醒',
  `created_by`        BIGINT       DEFAULT NULL,
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by`        BIGINT       DEFAULT NULL,
  `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_task_no` (`tenant_id`, `task_no`),
  KEY `idx_tenant_assignee` (`tenant_id`, `assignee_user_id`),
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_tenant_plan_end` (`tenant_id`, `plan_end_time`),
  KEY `idx_tenant_date` (`tenant_id`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作任务表';
```

---

## 9. ERP 同步记录表

### 9.1 ERP 同步日志表 (erp_sync_log)

```sql
CREATE TABLE `erp_sync_log` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `erp_config_id`     BIGINT       NOT NULL COMMENT 'ERP配置ID',
  `sync_type`         TINYINT      NOT NULL COMMENT '同步类型（1-ERP→CRM 2-CRM→ERP）',
  `sync_mode`         TINYINT      NOT NULL COMMENT '同步模式（1-全量 2-增量 3-单条）',
  `biz_type`          VARCHAR(32)  NOT NULL COMMENT '业务类型（customer/product/order）',
  `total_count`       INT          DEFAULT 0 COMMENT '总记录数',
  `success_count`     INT          DEFAULT 0 COMMENT '成功数',
  `fail_count`        INT          DEFAULT 0 COMMENT '失败数',
  `skip_count`        INT          DEFAULT 0 COMMENT '跳过数',
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（1-进行中 2-成功 3-部分失败 4-失败）',
  `error_message`     TEXT         DEFAULT NULL COMMENT '错误信息',
  `start_time`        DATETIME     NOT NULL COMMENT '开始时间',
  `end_time`          DATETIME     DEFAULT NULL COMMENT '结束时间',
  `operated_by`       BIGINT       DEFAULT NULL COMMENT '操作人',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_config` (`tenant_id`, `erp_config_id`),
  KEY `idx_tenant_time` (`tenant_id`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP同步日志表';
```

### 9.2 ERP 同步明细表 (erp_sync_detail)

```sql
CREATE TABLE `erp_sync_detail` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `sync_log_id`       BIGINT       NOT NULL COMMENT '同步日志ID',
  `crm_biz_id`        BIGINT       DEFAULT NULL COMMENT 'CRM业务ID',
  `erp_biz_id`        VARCHAR(64)  DEFAULT NULL COMMENT 'ERP业务ID',
  `sync_action`       TINYINT      NOT NULL COMMENT '操作（1-新增 2-更新 3-跳过）',
  `status`            TINYINT      NOT NULL COMMENT '状态（1-成功 2-失败 3-跳过）',
  `request_data`      TEXT         DEFAULT NULL COMMENT '请求数据（JSON）',
  `response_data`     TEXT         DEFAULT NULL COMMENT '响应数据（JSON）',
  `error_message`     VARCHAR(1024) DEFAULT NULL COMMENT '错误信息',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sync_log_id` (`tenant_id`, `sync_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP同步明细表';
```

---

## 10. 通知与消息表

### 10.1 消息通知表 (notification)

```sql
CREATE TABLE `notification` (
  `id`                BIGINT       NOT NULL,
  `tenant_id`         BIGINT       NOT NULL,
  `user_id`           BIGINT       NOT NULL COMMENT '接收人',
  `title`             VARCHAR(256) NOT NULL COMMENT '标题',
  `content`           TEXT         NOT NULL COMMENT '内容',
  `msg_type`          TINYINT      NOT NULL COMMENT '类型（1-任务提醒 2-线索分配 3-审核通知 4-违规警报 5-系统通知）',
  `biz_type`          VARCHAR(32)  DEFAULT NULL COMMENT '关联业务类型',
  `biz_id`            BIGINT       DEFAULT NULL COMMENT '关联业务ID',
  `is_read`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已读',
  `read_time`         DATETIME     DEFAULT NULL COMMENT '阅读时间',
  `push_channels`     VARCHAR(128) DEFAULT NULL COMMENT '推送渠道（app_push/sms/email）',
  `push_status`       TINYINT      DEFAULT 1 COMMENT '推送状态（1-待推送 2-已推送 3-推送失败）',
  `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user` (`tenant_id`, `user_id`, `is_read`),
  KEY `idx_tenant_created` (`tenant_id`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';
```

---

## 11. ER 关系概览

```
tenant (1) ──── (N) organization
tenant (1) ──── (N) tenant_erp_config
tenant (1) ──── (N) role
role   (1) ──── (N) role_permission
role   (1) ──── (N) user_role
organization (1) ── (N) user_role

lead   (1) ──── (N) lead_assign_log
lead   (1) ──── (N) follow_up_record (biz_type=1)
lead   (1) ──── (0..1) customer (convert)

customer (1) ──── (N) customer_contact
customer (1) ──── (N) opportunity
customer (1) ──── (N) follow_up_record (biz_type=2)
customer (1) ──── (N) visit_record
customer (1) ──── (N) customer_approval
customer (1) ──── (N) call_recording

opportunity (1) ── (N) follow_up_record (biz_type=3)
opportunity (1) ── (N) opportunity_stage_log

follow_up_record (1) ── (N) follow_up_attachment

visit_record (1) ── (N) checkin_record
visit_record (1) ── (0..1) call_recording
visit_record (1) ── (0..1) follow_up_record

work_task (N) ──── (1) user (assignee)
work_task (0..1) ── (1) visit_record

erp_sync_log (1) ── (N) erp_sync_detail
notification (N) ── (1) user

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

> **注意**：V2 新增表（location_report、third_party_config、enterprise_query_cache、
> customer_blacklist、activity、activity_participant、tenant_wechat_work_config、friend）
> 的完整 DDL 定义请参见 [补充设计文档 V2](06-supplement-v2.md)。

---

## 12. 索引策略

### 12.1 索引设计原则

1. **所有查询必须走 `tenant_id` 前缀索引**（多租户隔离）
2. **高频查询字段建立联合索引**（如 tenant_id + owner_user_id）
3. **时间范围查询使用覆盖索引**
4. **全文搜索走 Elasticsearch**（客户名、联系人名、跟进内容等）

### 12.2 Elasticsearch 索引

```json
{
  "crm_customer": {
    "mappings": {
      "properties": {
        "tenant_id": { "type": "long" },
        "customer_name": { "type": "text", "analyzer": "ik_max_word" },
        "short_name": { "type": "text", "analyzer": "ik_max_word" },
        "contact_names": { "type": "text", "analyzer": "ik_max_word" },
        "phone_numbers": { "type": "keyword" },
        "industry": { "type": "keyword" },
        "owner_user_id": { "type": "long" },
        "lifecycle_stage": { "type": "integer" },
        "tags": { "type": "keyword" }
      }
    }
  },
  "crm_follow_up": {
    "mappings": {
      "properties": {
        "tenant_id": { "type": "long" },
        "content": { "type": "text", "analyzer": "ik_max_word" },
        "ai_summary": { "type": "text", "analyzer": "ik_max_word" },
        "follow_user_id": { "type": "long" },
        "customer_id": { "type": "long" },
        "created_time": { "type": "date" }
      }
    }
  }
}
```
