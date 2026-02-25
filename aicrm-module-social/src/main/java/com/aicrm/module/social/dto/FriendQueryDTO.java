package com.aicrm.module.social.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 好友查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "好友查询条件")
public class FriendQueryDTO extends PageQuery {

    @Schema(description = "销售员用户ID")
    private Long userId;

    @Schema(description = "好友类型 1平台/2企微/3双渠道")
    private Integer friendType;

    @Schema(description = "来源 1活动扫码/2企微同步/3手动添加/4线索导入/5名片扫描")
    private Integer source;

    @Schema(description = "关键词 搜索friendName/friendPhone/friendCompany")
    private String keyword;

    @Schema(description = "来源活动ID")
    private Long sourceActivityId;

    @Schema(description = "是否已关联客户")
    private Boolean hasCustomer;
}
