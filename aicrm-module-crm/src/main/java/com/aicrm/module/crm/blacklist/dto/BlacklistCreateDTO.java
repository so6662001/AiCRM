package com.aicrm.module.crm.blacklist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 黑名单创建DTO
 */
@Data
public class BlacklistCreateDTO implements Serializable {

    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    @NotNull(message = "黑名单类型不能为空")
    private Integer blacklistType;
    @NotBlank(message = "原因不能为空")
    private String reason;
    private List<String> evidenceUrls;
}
