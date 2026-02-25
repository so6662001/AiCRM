package com.aicrm.module.crm.erp.sync.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.erp.sync.dto.SyncLogDetailVO;
import com.aicrm.module.crm.erp.sync.dto.SyncLogQueryDTO;
import com.aicrm.module.crm.erp.sync.dto.SyncLogVO;

/**
 * ERP同步日志服务
 */
public interface ErpSyncLogService {

    /**
     * 分页查询同步日志
     */
    PageResult<SyncLogVO> page(SyncLogQueryDTO query);

    /**
     * 获取同步日志详情（含明细）
     */
    SyncLogDetailVO getDetail(Long logId);

    /**
     * 创建模拟同步记录（DEMO版）
     * @param syncType 1:ERP→CRM 2:CRM→ERP
     * @param bizType customer/product/order
     * @return 同步日志ID
     */
    Long createMockSync(Integer syncType, String bizType);
}
