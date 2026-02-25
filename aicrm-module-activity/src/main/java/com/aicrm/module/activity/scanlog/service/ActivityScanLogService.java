package com.aicrm.module.activity.scanlog.service;

import com.aicrm.common.page.PageQuery;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.activity.scanlog.dto.ScanLogVO;

/**
 * 活动扫码日志服务
 */
public interface ActivityScanLogService {

    /**
     * 按活动ID分页查询扫码日志
     */
    PageResult<ScanLogVO> pageByActivityId(Long activityId, PageQuery query);

    /**
     * 记录扫码
     * 创建扫码记录，更新activity的totalScans+1；若fingerprint(ip+ua hash)首次出现则uniqueScans+1
     * @return 扫码日志ID
     */
    Long logScan(Long activityId, String ip, String ua, String referer);

    /**
     * 记录扫码（公开API用，从activity获取tenantId）
     * @param tenantId 租户ID，从activity获取
     */
    Long logScanPublic(Long activityId, Long tenantId, String ip, String ua, String referer);
}
