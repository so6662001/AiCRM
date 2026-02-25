package com.aicrm.module.crm.blacklist.dto;

import com.aicrm.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 黑名单查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BlacklistQueryDTO extends PageQuery {

    /** 状态 1生效/2已解除 */
    private Integer status;
    /** 黑名单类型 1欺诈/2恶意投诉/3空壳/4竞对/5其他 */
    private Integer blacklistType;
    /** 关键词(企业名称/电话) */
    private String keyword;
}
