package com.aicrm.module.crm.erp.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * ERP交易记录VO
 */
@Data
public class ErpTransactionVO implements Serializable {

    private String transactionId;
    private String transactionType;
    private String documentNo;
    private String transactionDate;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private BigDecimal balance;
    private String remark;
}
