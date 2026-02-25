package com.aicrm.module.fieldwork.checkin.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.fieldwork.checkin.dto.CheckinCreateDTO;
import com.aicrm.module.fieldwork.checkin.dto.CheckinQueryDTO;
import com.aicrm.module.fieldwork.checkin.dto.CheckinVO;
import com.aicrm.module.fieldwork.checkin.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 签到打卡Controller
 */
@RestController
@RequestMapping("/v1/checkins")
@RequiredArgsConstructor
@Tag(name = "签到打卡")
public class CheckinController {

    private final CheckinService checkinService;

    @GetMapping
    @Operation(summary = "分页查询签到记录")
    public Result<PageResult<CheckinVO>> page(@Valid CheckinQueryDTO query) {
        return Result.ok(checkinService.page(query));
    }

    @GetMapping("/today")
    @Operation(summary = "获取当天签到记录")
    public Result<List<CheckinVO>> getToday(@RequestParam(required = false) Long userId) {
        return Result.ok(checkinService.getToday(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取签到详情")
    public Result<CheckinVO> getById(@PathVariable Long id) {
        return Result.ok(checkinService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建签到")
    public Result<Long> create(@Valid @RequestBody CheckinCreateDTO dto) {
        return Result.ok(checkinService.create(dto));
    }
}
