package com.aicrm.module.crm.violation.service;

import com.aicrm.module.crm.violation.dto.ViolationWordDTO;
import com.aicrm.module.crm.violation.dto.ViolationWordVO;

import java.util.List;

/**
 * 违规词库服务
 */
public interface ViolationWordService {

    List<ViolationWordVO> list();

    Long create(ViolationWordDTO dto);

    void update(Long id, ViolationWordDTO dto);

    void delete(Long id);
}
