package com.aicrm.module.system.wechat.controller;

import com.aicrm.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 企业微信回调Controller（独立路径 /v1/wechat-work）
 */
@RestController
@RequestMapping("/v1/wechat-work")
@RequiredArgsConstructor
@Tag(name = "企业微信回调")
public class WechatWorkCallbackController {

    @GetMapping("/contact-way")
    @Operation(summary = "获取联系我方式")
    public Result<java.util.Map<String, String>> getContactWay() {
        return Result.ok(java.util.Map.of("contactWayUrl", "https://work.weixin.qq.com/demo"));
    }

    @PostMapping("/callback")
    @Operation(summary = "企业微信回调")
    public Result<Void> callback() {
        return Result.ok();
    }
}
