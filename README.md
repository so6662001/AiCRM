# AiCRM - 智能客户关系管理系统

平台级多租户 CRM 系统，集成客户管理、线索管理、商机管理、外勤管理、AI 智能分析及 ERP 对接能力。

## 技术栈

- **后端**：Java / Spring Cloud Alibaba 微服务架构
- **PC 前端**：Vue3 + TypeScript + Vite + Element Plus（微前端集成）
- **移动端**：UniApp (Vue3) 跨平台 APP
- **数据库**：MySQL 8.0（ShardingSphere 分库分表）
- **缓存**：Redis Cluster
- **搜索**：Elasticsearch 8.x
- **消息队列**：RocketMQ
- **AI**：ASR 语音转写 + LLM 内容分析

## 核心功能

- 线索管理与智能分配（公海池机制）
- 客户全生命周期管理（360° 客户视图）
- 商机管理与销售漏斗
- 多方式客户拜访管理（现场/电话/微信/企业微信）
- 电话录音自动采集与 AI 分析
- 通话违规用词检测与告警
- 外勤签到打卡
- 工作任务管理
- ERP 系统双向数据同步
- 多租户 SaaS 架构
- 层级数据权限控制

## 设计文档

| 文档 | 说明 |
|---|---|
| [系统架构设计](docs/design/01-system-architecture.md) | 整体架构、技术选型、微服务划分、多租户设计 |
| [数据库设计](docs/design/02-database-design.md) | 全部表结构、索引策略、分库分表方案 |
| [API 接口设计](docs/design/03-api-design.md) | RESTful API 规范、所有模块接口定义 |
| [移动端 APP 设计](docs/design/04-app-design.md) | APP 页面结构、交互设计、原生能力、离线策略 |
| [部署与高并发架构](docs/design/05-deployment-and-scalability.md) | 百万并发方案、K8s 部署、监控告警、安全设计 |
