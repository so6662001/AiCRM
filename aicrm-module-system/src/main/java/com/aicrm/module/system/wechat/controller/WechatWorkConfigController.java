package com.aicrm.module.system.wechat.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.system.wechat.dto.WechatWorkConfigDTO;
import com.aicrm.module.system.wechat.dto.WechatWorkConfigVO;
import com.aicrm.module.system.wechat.service.WechatWorkConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 企业微信配置Controller
 */
@RestController
@RequestMapping("/v1/wechat-work/config")
@RequiredArgsConstructor
@Tag(name = "企业微信配置")
public class WechatWorkConfigController {

    private final WechatWorkConfigService wechatWorkConfigService;

    @GetMapping
    @Operation(summary = "获取企业微信配置")
    public Result<WechatWorkConfigVO> getConfig() {
        return Result.ok(wechatWorkConfigService.getConfig());
    }

    @PostMapping
    @Operation(summary = "保存企业微信配置")
    public Result<Long> saveConfig(@Valid @RequestBody WechatWorkConfigDTO dto) {
        return Result.ok(wechatWorkConfigService.saveConfig(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新企业微信配置")
    public Result<Void> updateConfig(@PathVariable Long id, @Valid @RequestBody WechatWorkConfigDTO dto) {
        wechatWorkConfigService.updateConfig(id, dto);
        return Result.ok();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "测试企业微信连接")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        return Result.ok(wechatWorkConfigService.testConnection(id));
    }
}
