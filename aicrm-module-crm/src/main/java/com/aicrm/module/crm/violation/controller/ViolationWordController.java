package com.aicrm.module.crm.violation.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.violation.dto.ViolationWordDTO;
import com.aicrm.module.crm.violation.dto.ViolationWordVO;
import com.aicrm.module.crm.violation.service.ViolationWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 违规词库Controller
 */
@RestController
@RequestMapping("/v1/violation-words")
@RequiredArgsConstructor
@Tag(name = "违规词库")
public class ViolationWordController {

    private final ViolationWordService violationWordService;

    @GetMapping
    @Operation(summary = "查询违规词列表")
    public Result<List<ViolationWordVO>> list() {
        return Result.ok(violationWordService.list());
    }

    @PostMapping
    @Operation(summary = "新增违规词")
    public Result<Long> create(@Valid @RequestBody ViolationWordDTO dto) {
        return Result.ok(violationWordService.create(dto));
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入违规词")
    public Result<Void> importWords() {
        return Result.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新违规词")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ViolationWordDTO dto) {
        violationWordService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除违规词")
    public Result<Void> delete(@PathVariable Long id) {
        violationWordService.delete(id);
        return Result.ok();
    }
}
