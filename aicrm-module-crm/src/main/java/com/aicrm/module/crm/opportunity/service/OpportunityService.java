package com.aicrm.module.crm.opportunity.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.opportunity.dto.*;

import java.math.BigDecimal;

/**
 * 商机服务接口
 */
public interface OpportunityService {

    /**
     * 分页查询商机
     */
    PageResult<OpportunityVO> page(OpportunityQueryDTO query);

    /**
     * 根据ID获取商机详情
     */
    OpportunityVO getById(Long id);

    /**
     * 创建商机
     */
    Long create(OpportunityCreateDTO dto);

    /**
     * 更新商机
     */
    void update(OpportunityUpdateDTO dto);

    /**
     * 删除商机
     */
    void delete(Long id);

    /**
     * 变更商机阶段
     */
    void changeStage(Long id, StageChangeDTO dto);

    /**
     * 赢单
     */
    void win(Long id, BigDecimal actualAmount);

    /**
     * 输单
     */
    void lose(Long id, String lossReason);
}
