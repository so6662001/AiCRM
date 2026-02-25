package com.aicrm.module.crm.blacklist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BlacklistCreateDTO implements Serializable {

    private Long customerId;
    @NotBlank(message = "公司名称不能为空")
    private String companyName;
    private String creditCode;
    private String contactPhone;
    @NotNull(message = "黑名单类型不能为空")
    private Integer blacklistType;
    @NotBlank(message = "原因不能为空")
    private String reason;
    private List<String> evidenceUrls;
}
