package com.aicrm.module.crm.blacklist.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 黑名单VO
 */
@Data
public class BlacklistVO implements Serializable {

    private Long id;
    private Long customerId;
    private String customerName;
    private String companyName;
    private String creditCode;
    private String contactPhone;
    private Integer blacklistType;
    private String blacklistTypeLabel;
    private String reason;
    private String evidenceUrls;
    private Integer status;
    private Long operatedBy;
    private Long releaseBy;
    private LocalDateTime releaseTime;
    private String releaseReason;
    private LocalDateTime createdTime;
}
