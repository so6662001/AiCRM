package com.aicrm.module.activity.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageQuery;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.activity.dto.ParticipantCreateDTO;
import com.aicrm.module.activity.dto.ParticipantVO;
import com.aicrm.module.activity.dto.RejectReasonDTO;
import com.aicrm.module.activity.service.ParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 活动参与人管理控制器
 */
@RestController
@RequestMapping("/v1/activities/{activityId}/participants")
@RequiredArgsConstructor
@Tag(name = "活动参与人管理", description = "参与人分页、添加、审核、签到等接口")
public class ParticipantController {

    private final ParticipantService participantService;

    @GetMapping
    @Operation(summary = "分页查询参与人")
    public Result<PageResult<ParticipantVO>> page(@PathVariable Long activityId, PageQuery query) {
        return Result.ok(participantService.page(activityId, query));
    }

    @PostMapping
    @Operation(summary = "添加参与人")
    public Result<Long> add(@PathVariable Long activityId, @Valid @RequestBody ParticipantCreateDTO dto) {
        return Result.ok(participantService.add(activityId, dto));
    }

    @PostMapping("/{pid}/approve")
    @Operation(summary = "通过报名")
    public Result<Void> approve(@PathVariable Long activityId, @PathVariable("pid") Long participantId) {
        participantService.approve(activityId, participantId);
        return Result.ok();
    }

    @PostMapping("/{pid}/reject")
    @Operation(summary = "拒绝报名")
    public Result<Void> reject(@PathVariable Long activityId, @PathVariable("pid") Long participantId,
                               @RequestBody(required = false) RejectReasonDTO dto) {
        participantService.reject(activityId, participantId, dto != null ? dto.getReason() : null);
        return Result.ok();
    }

    @PostMapping("/{pid}/checkin")
    @Operation(summary = "签到")
    public Result<Void> checkin(@PathVariable Long activityId, @PathVariable("pid") Long participantId) {
        participantService.checkin(activityId, participantId);
        return Result.ok();
    }
}
