package com.aicrm.module.crm.erp.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.erp.dto.ErpReceivableVO;
import com.aicrm.module.crm.erp.dto.ErpTransactionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * ERP财务服务实现 - DEMO版返回模拟数据
 */
@Service
@RequiredArgsConstructor
public class ErpFinanceServiceImpl implements ErpFinanceService {

    @Override
    public ErpReceivableVO getReceivables(Long customerId) {
        ErpReceivableVO vo = new ErpReceivableVO();
        vo.setTotalReceivable(new BigDecimal("350000"));
        vo.setOverdueAmount(new BigDecimal("200000"));
        vo.setCurrentAmount(new BigDecimal("150000"));
        vo.setCreditLimit(new BigDecimal("500000"));
        vo.setCreditAvailable(new BigDecimal("150000"));
        vo.setLastPaymentDate("2026-02-20");
        vo.setLastPaymentAmount(new BigDecimal("50000"));

        List<ErpReceivableVO.AgingItem> aging = List.of(
                createAgingItem("0-30天", new BigDecimal("150000")),
                createAgingItem("31-60天", new BigDecimal("80000")),
                createAgingItem("61-90天", new BigDecimal("70000")),
                createAgingItem("90+天", new BigDecimal("50000"))
        );
        vo.setAgingAnalysis(aging);
        return vo;
    }

    @Override
    public PageResult<ErpTransactionVO> getTransactions(Long customerId, int pageNum, int pageSize) {
        List<ErpTransactionVO> records = List.of(
                createTransaction("TXN001", "销售", "SO-2026-001", "2026-02-20", new BigDecimal("100000"), null, new BigDecimal("350000"), "2月订单"),
                createTransaction("TXN002", "收款", "PAY-2026-001", "2026-02-18", null, new BigDecimal("50000"), new BigDecimal("250000"), "银行转账"),
                createTransaction("TXN003", "销售", "SO-2026-002", "2026-02-15", new BigDecimal("80000"), null, new BigDecimal("300000"), "1月订单"),
                createTransaction("TXN004", "收款", "PAY-2026-002", "2026-02-10", null, new BigDecimal("100000"), new BigDecimal("220000"), "承兑汇票"),
                createTransaction("TXN005", "销售", "SO-2026-003", "2026-02-05", new BigDecimal("120000"), null, new BigDecimal("320000"), "年度合同")
        );
        return PageResult.of(records, 5, pageNum, pageSize);
    }

    private ErpReceivableVO.AgingItem createAgingItem(String period, BigDecimal amount) {
        ErpReceivableVO.AgingItem item = new ErpReceivableVO.AgingItem();
        item.setPeriod(period);
        item.setAmount(amount);
        return item;
    }

    private ErpTransactionVO createTransaction(String id, String type, String docNo, String date,
                                               BigDecimal debit, BigDecimal credit, BigDecimal balance, String remark) {
        ErpTransactionVO vo = new ErpTransactionVO();
        vo.setTransactionId(id);
        vo.setTransactionType(type);
        vo.setDocumentNo(docNo);
        vo.setTransactionDate(date);
        vo.setDebitAmount(debit);
        vo.setCreditAmount(credit);
        vo.setBalance(balance);
        vo.setRemark(remark);
        return vo;
    }
}
