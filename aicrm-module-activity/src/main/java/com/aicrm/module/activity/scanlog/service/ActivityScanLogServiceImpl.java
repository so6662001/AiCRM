package com.aicrm.module.activity.scanlog.service;

import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageQuery;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.activity.entity.Activity;
import com.aicrm.module.activity.mapper.ActivityMapper;
import com.aicrm.module.activity.scanlog.dto.ScanLogVO;
import com.aicrm.module.activity.scanlog.entity.ActivityScanLog;
import com.aicrm.module.activity.scanlog.mapper.ActivityScanLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动扫码日志服务实现
 */
@Service
@RequiredArgsConstructor
public class ActivityScanLogServiceImpl implements ActivityScanLogService {

    private final ActivityScanLogMapper activityScanLogMapper;
    private final ActivityMapper activityMapper;

    @Override
    public PageResult<ScanLogVO> pageByActivityId(Long activityId, PageQuery query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        LambdaQueryWrapper<ActivityScanLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityScanLog::getTenantId, tenantId)
                .eq(ActivityScanLog::getActivityId, activityId)
                .orderByDesc(ActivityScanLog::getScanTime);

        Page<ActivityScanLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<ActivityScanLog> result = activityScanLogMapper.selectPage(page, wrapper);

        List<ScanLogVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long logScan(Long activityId, String ip, String ua, String referer) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }
        return doLogScan(activityId, tenantId, ip, ua, referer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long logScanPublic(Long activityId, Long tenantId, String ip, String ua, String referer) {
        return doLogScan(activityId, tenantId, ip, ua, referer);
    }

    private Long doLogScan(Long activityId, Long tenantId, String ip, String ua, String referer) {
        String fingerprint = computeFingerprint(ip, ua);
        LocalDateTime now = LocalDateTime.now();

        // 检查同一fingerprint是否已存在
        long existCount = activityScanLogMapper.selectCount(
                new LambdaQueryWrapper<ActivityScanLog>()
                        .eq(ActivityScanLog::getTenantId, tenantId)
                        .eq(ActivityScanLog::getActivityId, activityId)
                        .eq(ActivityScanLog::getScanFingerprint, fingerprint)
        );

        boolean isUnique = (existCount == 0);

        // 创建扫码记录
        ActivityScanLog log = new ActivityScanLog();
        log.setTenantId(tenantId);
        log.setActivityId(activityId);
        log.setScanFingerprint(fingerprint);
        log.setScanIp(ip);
        log.setScanUa(ua);
        log.setScanTime(now);
        log.setReferer(referer);
        log.setDidRegister(0);
        log.setDidAddFriend(0);
        log.setParticipantId(null);
        log.setCreatedTime(now);

        activityScanLogMapper.insert(log);

        // 更新activity的totalScans+1，首次fingerprint时uniqueScans+1
        LambdaUpdateWrapper<Activity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Activity::getId, activityId)
                .eq(Activity::getTenantId, tenantId)
                .setSql("total_scans = total_scans + 1, unique_scans = unique_scans + " + (isUnique ? "1" : "0"));
        activityMapper.update(null, updateWrapper);

        return log.getId();
    }

    private String computeFingerprint(String ip, String ua) {
        String input = (ip != null ? ip : "") + "|" + (ua != null ? ua : "");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }

    private ScanLogVO convertToVO(ActivityScanLog entity) {
        ScanLogVO vo = new ScanLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
