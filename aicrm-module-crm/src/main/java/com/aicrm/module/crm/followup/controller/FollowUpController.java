package com.aicrm.module.crm.followup.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.followup.dto.FollowUpCreateDTO;
import com.aicrm.module.crm.followup.dto.FollowUpQueryDTO;
import com.aicrm.module.crm.followup.dto.FollowUpUpdateDTO;
import com.aicrm.module.crm.followup.dto.FollowUpVO;
import com.aicrm.module.crm.followup.service.FollowUpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/follow-ups")
@RequiredArgsConstructor
@Tag(name = "跟进管理", description = "跟进记录相关接口")
public class FollowUpController {

    private final FollowUpService followUpService;

    @GetMapping
    @Operation(summary = "分页查询跟进记录")
    public Result<PageResult<FollowUpVO>> page(@ModelAttribute FollowUpQueryDTO query) {
        return Result.ok(followUpService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取跟进记录详情")
    public Result<FollowUpVO> getById(@PathVariable Long id) {
        return Result.ok(followUpService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建跟进记录")
    public Result<Long> create(@Valid @RequestBody FollowUpCreateDTO dto) {
        return Result.ok(followUpService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新跟进记录")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody FollowUpUpdateDTO dto) {
        dto.setId(id);
        followUpService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除跟进记录")
    public Result<Void> delete(@PathVariable Long id) {
        followUpService.delete(id);
        return Result.ok();
    }
}
