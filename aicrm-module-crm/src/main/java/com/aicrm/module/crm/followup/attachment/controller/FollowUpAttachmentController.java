package com.aicrm.module.crm.followup.attachment.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.followup.attachment.dto.AttachmentCreateDTO;
import com.aicrm.module.crm.followup.attachment.dto.AttachmentVO;
import com.aicrm.module.crm.followup.attachment.service.FollowUpAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 跟进附件控制器
 */
@RestController
@RequestMapping("/v1/follow-ups/{followUpId}/attachments")
@RequiredArgsConstructor
@Tag(name = "跟进附件", description = "跟进附件管理接口")
public class FollowUpAttachmentController {

    private final FollowUpAttachmentService followUpAttachmentService;

    @GetMapping
    @Operation(summary = "查询跟进附件列表")
    public Result<List<AttachmentVO>> listByFollowUpId(@PathVariable Long followUpId) {
        return Result.ok(followUpAttachmentService.listByFollowUpId(followUpId));
    }

    @PostMapping
    @Operation(summary = "创建跟进附件")
    public Result<Long> create(@PathVariable Long followUpId, @Valid @RequestBody AttachmentCreateDTO dto) {
        dto.setFollowUpId(followUpId);
        return Result.ok(followUpAttachmentService.create(dto));
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "删除跟进附件")
    public Result<Void> delete(@PathVariable Long followUpId, @PathVariable Long attachmentId) {
        followUpAttachmentService.delete(attachmentId);
        return Result.ok();
    }
}
