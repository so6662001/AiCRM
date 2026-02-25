package com.aicrm.module.crm.erp.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * ERP应收款VO
 */
@Data
public class ErpReceivableVO implements Serializable {

    private BigDecimal totalReceivable;
    private BigDecimal overdueAmount;
    private BigDecimal currentAmount;
    private BigDecimal creditLimit;
    private BigDecimal creditAvailable;
    private String lastPaymentDate;
    private BigDecimal lastPaymentAmount;
    private List<AgingItem> agingAnalysis;

    @Data
    public static class AgingItem implements Serializable {
        private String period;
        private BigDecimal amount;
    }
}
