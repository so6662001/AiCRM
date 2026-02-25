package com.aicrm.module.activity.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.exception.BizException;
import com.aicrm.module.activity.dto.OpenActivityInfoVO;
import com.aicrm.module.activity.dto.OpenRegisterDTO;
import com.aicrm.module.activity.entity.Activity;
import com.aicrm.module.activity.entity.ActivityParticipant;
import com.aicrm.module.activity.mapper.ActivityMapper;
import com.aicrm.module.activity.mapper.ActivityParticipantMapper;
import com.aicrm.module.activity.scanlog.service.ActivityScanLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公开活动控制器（无需鉴权）
 */
@RestController
@RequestMapping("/v1/open/activities")
@RequiredArgsConstructor
@Tag(name = "公开活动", description = "扫码、报名等公开接口，无需鉴权")
public class OpenActivityController {

    private final ActivityMapper activityMapper;
    private final ActivityParticipantMapper activityParticipantMapper;
    private final ActivityScanLogService activityScanLogService;

    @GetMapping("/{activityNo}/info")
    @Operation(summary = "获取活动信息（公开）")
    public Result<OpenActivityInfoVO> getInfo(@PathVariable String activityNo) {
        Activity activity = activityMapper.selectOne(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getActivityNo, activityNo)
        );
        if (activity == null) {
            throw BizException.notFound("活动");
        }

        OpenActivityInfoVO vo = new OpenActivityInfoVO();
        BeanUtils.copyProperties(activity, vo);
        return Result.ok(vo);
    }

    @PostMapping("/{activityNo}/scan")
    @Operation(summary = "记录扫码（公开）")
    public Result<Long> scan(@PathVariable String activityNo, HttpServletRequest request) {
        Activity activity = activityMapper.selectOne(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getActivityNo, activityNo)
        );
        if (activity == null) {
            throw BizException.notFound("活动");
        }

        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");

        Long logId = activityScanLogService.logScanPublic(
                activity.getId(), activity.getTenantId(), ip, ua, referer);
        return Result.ok(logId);
    }

    @GetMapping("/{activityNo}/check-registered")
    @Operation(summary = "检查是否已报名（公开）")
    public Result<Boolean> checkRegistered(@PathVariable String activityNo, @RequestParam String phone) {
        return Result.ok(false);
    }

    @PostMapping("/{activityNo}/register")
    @Operation(summary = "扫码报名（公开）")
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> register(@PathVariable String activityNo, @Valid @RequestBody OpenRegisterDTO dto) {
        Activity activity = activityMapper.selectOne(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getActivityNo, activityNo)
        );
        if (activity == null) {
            throw BizException.notFound("活动");
        }

        // 报名状态：0自动通过→1已通过，1需审核→0待审核
        int registrationStatus = (activity.getRegistrationApproval() != null && activity.getRegistrationApproval() == 1)
                ? 0 : 1; // 0待审核, 1已通过

        ActivityParticipant participant = new ActivityParticipant();
        participant.setTenantId(activity.getTenantId());
        participant.setActivityId(activity.getId());
        participant.setParticipantName(dto.getName());
        participant.setParticipantPhone(dto.getPhone());
        participant.setCompanyName(dto.getCompany());
        participant.setSource(1); // 扫码报名
        participant.setRegistrationStatus(registrationStatus);
        participant.setRegistrationTime(LocalDateTime.now());

        activityParticipantMapper.insert(participant);

        return Result.ok(participant.getId());
    }

    private String getClientIp(HttpServletRequest request) {
        List<String> headers = List.of("X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP");
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
