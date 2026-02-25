-- AiCRM Demo 初始数据

-- 初始租户
INSERT IGNORE INTO `tenant` (`id`, `tenant_code`, `tenant_name`, `contact_name`, `contact_phone`, `status`, `edition`, `max_users`, `expire_date`)
VALUES (1, 'DEMO', 'AiCRM演示租户', '管理员', '13800000000', 1, 'enterprise', 100, '2027-12-31');

-- 初始组织架构
INSERT IGNORE INTO `organization` (`id`, `tenant_id`, `parent_id`, `org_name`, `org_code`, `org_type`, `org_path`, `sort_order`, `status`, `created_time`, `updated_time`, `deleted`, `version`)
VALUES
(100, 1, 0, '演示公司', 'HQ', 1, '/100/', 0, 1, NOW(), NOW(), 0, 1),
(101, 1, 100, '销售部', 'SALES', 2, '/100/101/', 1, 1, NOW(), NOW(), 0, 1),
(102, 1, 101, '华南销售团队', 'SALES_SOUTH', 3, '/100/101/102/', 1, 1, NOW(), NOW(), 0, 1),
(103, 1, 101, '华北销售团队', 'SALES_NORTH', 3, '/100/101/103/', 2, 1, NOW(), NOW(), 0, 1),
(104, 1, 100, '运营部', 'OPS', 2, '/100/104/', 2, 1, NOW(), NOW(), 0, 1);

-- 初始商机阶段配置
INSERT IGNORE INTO `opportunity_stage_config` (`id`, `tenant_id`, `stage_name`, `stage_code`, `win_rate`, `sort_order`, `is_won`, `is_lost`, `status`, `created_time`, `updated_time`, `deleted`)
VALUES
(1, 1, '初步接触', 'INITIAL', 10, 1, 0, 0, 1, NOW(), NOW(), 0),
(2, 1, '需求确认', 'REQUIREMENT', 30, 2, 0, 0, 1, NOW(), NOW(), 0),
(3, 1, '方案提案', 'PROPOSAL', 50, 3, 0, 0, 1, NOW(), NOW(), 0),
(4, 1, '报价阶段', 'QUOTATION', 70, 4, 0, 0, 1, NOW(), NOW(), 0),
(5, 1, '谈判阶段', 'NEGOTIATION', 90, 5, 0, 0, 1, NOW(), NOW(), 0),
(6, 1, '赢单', 'WON', 100, 6, 1, 0, 1, NOW(), NOW(), 0),
(7, 1, '输单', 'LOST', 0, 7, 0, 1, 1, NOW(), NOW(), 0);

-- 五度易链第三方配置(DEMO)
INSERT IGNORE INTO `third_party_config` (`id`, `tenant_id`, `provider`, `api_base_url`, `app_key`, `app_secret`, `daily_quota`, `daily_used`, `contact_daily_quota`, `contact_daily_used`, `contact_monthly_quota`, `contact_monthly_used`, `status`, `created_time`, `updated_time`, `deleted`, `version`)
VALUES (1, 1, 'wdyl', 'https://api.wdyl.com', 'demo_key', 'demo_secret', 100, 0, 50, 0, 500, 0, 1, NOW(), NOW(), 0, 1);
