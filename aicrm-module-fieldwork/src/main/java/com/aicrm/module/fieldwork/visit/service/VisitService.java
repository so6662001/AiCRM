package com.aicrm.module.fieldwork.visit.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.fieldwork.visit.dto.VisitCreateDTO;
import com.aicrm.module.fieldwork.visit.dto.VisitQueryDTO;
import com.aicrm.module.fieldwork.visit.dto.VisitVO;

/**
 * 拜访服务
 */
public interface VisitService {

    PageResult<VisitVO> page(VisitQueryDTO query);

    VisitVO getById(Long id);

    Long create(VisitCreateDTO dto);

    void complete(Long id, String visitResult);

    void cancel(Long id);
}
