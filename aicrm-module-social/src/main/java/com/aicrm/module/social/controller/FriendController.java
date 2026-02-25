package com.aicrm.module.social.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.social.dto.*;
import com.aicrm.module.social.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 好友管理Controller
 */
@RestController
@RequestMapping("/v1/friends")
@RequiredArgsConstructor
@Tag(name = "好友管理")
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    @Operation(summary = "分页查询好友")
    public Result<PageResult<FriendVO>> page(@Valid FriendQueryDTO query) {
        return Result.ok(friendService.page(query));
    }

    @GetMapping("/statistics")
    @Operation(summary = "好友统计")
    public Result<FriendStatisticsVO> statistics() {
        return Result.ok(friendService.statistics());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取好友详情")
    public Result<FriendVO> getById(@PathVariable Long id) {
        return Result.ok(friendService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建好友")
    public Result<Long> create(@Valid @RequestBody FriendCreateDTO dto) {
        return Result.ok(friendService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新好友")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody FriendUpdateDTO dto) {
        dto.setId(id);
        friendService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除好友")
    public Result<Void> delete(@PathVariable Long id) {
        friendService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/link-customer")
    @Operation(summary = "关联客户")
    public Result<Void> linkCustomer(@PathVariable Long id, @RequestBody LinkCustomerRequest request) {
        friendService.linkCustomer(id, request != null ? request.getCustomerId() : null);
        return Result.ok();
    }

    @PostMapping("/{id}/convert-to-lead")
    @Operation(summary = "好友转线索")
    public Result<Void> convertToLead(@PathVariable Long id) {
        friendService.convertToLead(id);
        return Result.ok();
    }

    @PostMapping("/sync-wechat")
    @Operation(summary = "同步微信好友")
    public Result<Void> syncWechat() {
        return Result.ok();
    }

    @Data
    public static class LinkCustomerRequest {
        private Long customerId;
    }
}
