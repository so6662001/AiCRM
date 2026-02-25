package com.aicrm.module.crm.followup.controller;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 跟进记录控制器
 */
@RestController
@RequestMapping("/v1/follow-ups")
@RequiredArgsConstructor
@Tag(name = "跟进管理", description = "跟进记录相关接口")
public class FollowUpController {

    private final FollowUpService followUpService;

    @GetMapping
    @Operation(summary = "分页查询跟进记录")
    public PageResult<FollowUpVO> page(@ModelAttribute FollowUpQueryDTO query) {
        return followUpService.page(query);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取跟进记录详情")
    public FollowUpVO getById(@PathVariable Long id) {
        return followUpService.getById(id);
    }

    @PostMapping
    @Operation(summary = "创建跟进记录")
    public Long create(@Valid @RequestBody FollowUpCreateDTO dto) {
        return followUpService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新跟进记录")
    public void update(@PathVariable Long id, @Valid @RequestBody FollowUpUpdateDTO dto) {
        dto.setId(id);
        followUpService.update(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除跟进记录")
    public void delete(@PathVariable Long id) {
        followUpService.delete(id);
    }
}
