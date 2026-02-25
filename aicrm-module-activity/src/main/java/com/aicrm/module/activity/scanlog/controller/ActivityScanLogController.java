package com.aicrm.module.activity.scanlog.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageQuery;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.activity.scanlog.dto.ScanLogQueryDTO;
import com.aicrm.module.activity.scanlog.dto.ScanLogVO;
import com.aicrm.module.activity.scanlog.service.ActivityScanLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 活动扫码日志控制器
 */
@RestController
@RequestMapping("/v1/activities/{activityId}/scan-logs")
@RequiredArgsConstructor
@Tag(name = "活动扫码日志", description = "活动扫码日志查询接口")
public class ActivityScanLogController {

    private final ActivityScanLogService activityScanLogService;

    @GetMapping
    @Operation(summary = "分页查询活动扫码日志")
    public Result<PageResult<ScanLogVO>> pageByActivityId(@PathVariable Long activityId,
                                                          @Valid ScanLogQueryDTO query) {
        query.setActivityId(activityId);
        return Result.ok(activityScanLogService.pageByActivityId(activityId, query));
    }
}
