package com.aicrm.module.fieldwork.trajectory.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.fieldwork.trajectory.dto.LocationReportDTO;
import com.aicrm.module.fieldwork.trajectory.dto.TrajectoryVO;
import com.aicrm.module.fieldwork.trajectory.service.TrajectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 行动轨迹Controller
 */
@RestController
@RequestMapping("/v1/trajectories")
@RequiredArgsConstructor
@Tag(name = "行动轨迹")
public class TrajectoryController {

    private final TrajectoryService trajectoryService;

    @GetMapping
    @Operation(summary = "轨迹列表")
    public Result<java.util.List<java.util.Map<String, Object>>> list() {
        return Result.ok(java.util.List.of());
    }

    @GetMapping("/team")
    @Operation(summary = "团队轨迹")
    public Result<java.util.List<java.util.Map<String, Object>>> team() {
        return Result.ok(java.util.List.of());
    }

    @PostMapping("/report")
    @Operation(summary = "上报位置")
    public Result<Void> reportLocation(@RequestBody LocationReportDTO dto) {
        trajectoryService.reportLocation(dto);
        return Result.ok();
    }

    @GetMapping("/{userId}/daily")
    @Operation(summary = "获取日轨迹")
    public Result<TrajectoryVO> getDailyTrajectory(
            @PathVariable Long userId,
            @RequestParam String date) {
        return Result.ok(trajectoryService.getDailyTrajectory(userId, date));
    }
}
