package com.aicrm.module.crm.erp.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.erp.dto.ErpReceivableVO;
import com.aicrm.module.crm.erp.dto.ErpTransactionVO;

/**
 * ERP财务服务
 */
public interface ErpFinanceService {

    ErpReceivableVO getReceivables(Long customerId);

    PageResult<ErpTransactionVO> getTransactions(Long customerId, int pageNum, int pageSize);
}
