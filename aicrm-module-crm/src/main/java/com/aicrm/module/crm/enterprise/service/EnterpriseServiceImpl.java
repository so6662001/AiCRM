package com.aicrm.module.crm.enterprise.service;

import cn.hutool.core.util.StrUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.customer.entity.Customer;
import com.aicrm.module.crm.customer.mapper.CustomerMapper;
import com.aicrm.module.crm.enterprise.dto.ContactImportDTO;
import com.aicrm.module.crm.enterprise.dto.ContactQuotaVO;
import com.aicrm.module.crm.enterprise.dto.EnterpriseContactVO;
import com.aicrm.module.crm.enterprise.dto.EnterpriseSearchVO;
import com.aicrm.module.crm.enterprise.entity.EnterpriseContact;
import com.aicrm.module.crm.enterprise.entity.EnterpriseQueryCache;
import com.aicrm.module.crm.enterprise.entity.ThirdPartyConfig;
import com.aicrm.module.crm.enterprise.mapper.EnterpriseContactMapper;
import com.aicrm.module.crm.enterprise.mapper.EnterpriseQueryCacheMapper;
import com.aicrm.module.crm.enterprise.mapper.ThirdPartyConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 五度易链企业信息服务实现（DEMO版）
 */
@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private static final String PROVIDER_WDYL = "wdyl";
    private static final int CACHE_EXPIRE_DAYS = 30;
    private static final int MOCK_ENTERPRISE_COUNT = 3;
    private static final int MOCK_CONTACT_COUNT = 3;

    private final EnterpriseQueryCacheMapper enterpriseQueryCacheMapper;
    private final EnterpriseContactMapper enterpriseContactMapper;
    private final ThirdPartyConfigMapper thirdPartyConfigMapper;
    private final CustomerMapper customerMapper;

    @Override
    public List<EnterpriseSearchVO> search(String keyword) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }

        // 先查缓存
        LambdaQueryWrapper<EnterpriseQueryCache> cacheWrapper = new LambdaQueryWrapper<>();
        cacheWrapper.eq(EnterpriseQueryCache::getTenantId, tenantId)
                .eq(EnterpriseQueryCache::getSource, "wdyl")
                .ge(EnterpriseQueryCache::getExpireTime, LocalDateTime.now());
        if (StrUtil.isNotBlank(keyword)) {
            cacheWrapper.and(w -> w.like(EnterpriseQueryCache::getCompanyName, keyword)
                    .or().like(EnterpriseQueryCache::getCreditCode, keyword));
        }
        List<EnterpriseQueryCache> cacheList = enterpriseQueryCacheMapper.selectList(cacheWrapper);

        // 无缓存则创建模拟数据
        if (cacheList.isEmpty() && StrUtil.isNotBlank(keyword)) {
            cacheList = createMockEnterprises(keyword, tenantId);
        } else if (cacheList.isEmpty()) {
            return List.of();
        }

        // 用creditCode匹配customer.erpCustomerId标记isPlatformCustomer
        List<String> creditCodes = cacheList.stream()
                .map(EnterpriseQueryCache::getCreditCode)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        Map<String, Customer> customerMap = findCustomersByCreditCode(tenantId, creditCodes);

        // 统计每个企业的缓存联系人数
        Map<String, Long> contactCountMap = countContactsByCreditCode(tenantId, creditCodes);

        List<EnterpriseSearchVO> result = new ArrayList<>();
        for (EnterpriseQueryCache cache : cacheList) {
            EnterpriseSearchVO vo = toSearchVO(cache);
            Customer matched = cache.getCreditCode() != null ? customerMap.get(cache.getCreditCode()) : null;
            vo.setIsPlatformCustomer(matched != null);
            vo.setMatchedCustomerId(matched != null ? matched.getId() : null);
            vo.setMatchedCustomerName(matched != null ? matched.getCustomerName() : null);
            vo.setContactCacheCount(contactCountMap.getOrDefault(cache.getCreditCode(), 0L).intValue());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<EnterpriseContactVO> queryContacts(String creditCode) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null || StrUtil.isBlank(creditCode)) {
            return List.of();
        }

        // 先查enterprise_contact缓存
        List<EnterpriseContact> cached = findContactsByCreditCode(tenantId, creditCode);
        boolean hasValidCache = cached.stream()
                .anyMatch(c -> c.getQueryTime() != null && c.getQueryTime().plusDays(CACHE_EXPIRE_DAYS).isAfter(LocalDateTime.now()));

        if (hasValidCache) {
            return cached.stream().map(this::toContactVO).toList();
        }

        // 无有效缓存，模拟生成3个联系人
        return createMockContacts(creditCode, tenantId);
    }

    @Override
    public List<EnterpriseContactVO> getContactsCache(String creditCode) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null || StrUtil.isBlank(creditCode)) {
            return List.of();
        }
        List<EnterpriseContact> cached = findContactsByCreditCode(tenantId, creditCode);
        return cached.stream().map(this::toContactVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importContact(String creditCode, Long contactId, ContactImportDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw BizException.forbidden("租户信息缺失");
        }

        LambdaUpdateWrapper<EnterpriseContact> update = new LambdaUpdateWrapper<>();
        update.eq(EnterpriseContact::getId, contactId)
                .eq(EnterpriseContact::getTenantId, tenantId)
                .eq(EnterpriseContact::getCreditCode, creditCode)
                .set(EnterpriseContact::getIsImported, 1)
                .set(EnterpriseContact::getImportedCustomerId, dto.getCustomerId())
                .set(EnterpriseContact::getUpdatedTime, LocalDateTime.now());
        enterpriseContactMapper.update(null, update);
    }

    @Override
    public ContactQuotaVO getContactQuota() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            ContactQuotaVO vo = new ContactQuotaVO();
            vo.setDailyQuota(0);
            vo.setDailyUsed(0);
            vo.setDailyRemaining(0);
            vo.setMonthlyQuota(0);
            vo.setMonthlyUsed(0);
            vo.setMonthlyRemaining(0);
            return vo;
        }

        LambdaQueryWrapper<ThirdPartyConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdPartyConfig::getTenantId, tenantId)
                .eq(ThirdPartyConfig::getProvider, PROVIDER_WDYL)
                .last("LIMIT 1");
        ThirdPartyConfig config = thirdPartyConfigMapper.selectOne(wrapper);

        ContactQuotaVO vo = new ContactQuotaVO();
        if (config == null) {
            vo.setDailyQuota(100);
            vo.setDailyUsed(0);
            vo.setDailyRemaining(100);
            vo.setMonthlyQuota(1000);
            vo.setMonthlyUsed(0);
            vo.setMonthlyRemaining(1000);
            return vo;
        }

        int dailyQuota = config.getContactDailyQuota() != null ? config.getContactDailyQuota() : 100;
        int dailyUsed = config.getContactDailyUsed() != null ? config.getContactDailyUsed() : 0;
        int monthlyQuota = config.getContactMonthlyQuota() != null ? config.getContactMonthlyQuota() : 1000;
        int monthlyUsed = config.getContactMonthlyUsed() != null ? config.getContactMonthlyUsed() : 0;

        vo.setDailyQuota(dailyQuota);
        vo.setDailyUsed(dailyUsed);
        vo.setDailyRemaining(Math.max(0, dailyQuota - dailyUsed));
        vo.setMonthlyQuota(monthlyQuota);
        vo.setMonthlyUsed(monthlyUsed);
        vo.setMonthlyRemaining(Math.max(0, monthlyQuota - monthlyUsed));
        return vo;
    }

    private List<EnterpriseQueryCache> createMockEnterprises(String keyword, Long tenantId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusDays(CACHE_EXPIRE_DAYS);
        String baseCode = "91" + Math.abs(keyword.hashCode() % 100000000);

        List<EnterpriseQueryCache> list = new ArrayList<>();
        for (int i = 0; i < MOCK_ENTERPRISE_COUNT; i++) {
            EnterpriseQueryCache cache = new EnterpriseQueryCache();
            cache.setTenantId(tenantId);
            cache.setCompanyName(keyword + "科技有限公司" + (i + 1));
            cache.setCreditCode(baseCode + String.format("%02d", i));
            cache.setLegalPerson("张三" + (i + 1));
            cache.setRegisteredCapital("1000万");
            cache.setEstablishedDate(LocalDate.of(2020, 1 + i, 1));
            cache.setCompanyStatus("在业");
            cache.setCompanyType("有限责任公司");
            cache.setIndustry("软件和信息技术服务业");
            cache.setProvince("北京市");
            cache.setCity("朝阳区");
            cache.setAddress("朝阳区某某路" + (i + 1) + "号");
            cache.setSource("wdyl");
            cache.setIsPlatformCustomer(0);
            cache.setQueryTime(now);
            cache.setExpireTime(expireTime);
            cache.setCreatedTime(now);
            enterpriseQueryCacheMapper.insert(cache);
            list.add(cache);
        }
        return list;
    }

    private List<EnterpriseContactVO> createMockContacts(String creditCode, Long tenantId) {
        // 获取企业名称
        LambdaQueryWrapper<EnterpriseQueryCache> cacheWrapper = new LambdaQueryWrapper<>();
        cacheWrapper.eq(EnterpriseQueryCache::getTenantId, tenantId)
                .eq(EnterpriseQueryCache::getCreditCode, creditCode)
                .last("LIMIT 1");
        EnterpriseQueryCache cache = enterpriseQueryCacheMapper.selectOne(cacheWrapper);
        String companyName = cache != null ? cache.getCompanyName() : "未知企业";

        Long userId = TenantContext.getUserId();
        LocalDateTime now = LocalDateTime.now();
        String[] names = {"李经理", "王主管", "赵总监"};
        String[] positions = {"销售经理", "市场主管", "技术总监"};
        String[] phones = {"13812345678", "13987654321", "13711112222"};
        String[] emails = {"li@example.com", "wang@example.com", "zhao@example.com"};

        List<EnterpriseContact> contacts = new ArrayList<>();
        for (int i = 0; i < MOCK_CONTACT_COUNT; i++) {
            EnterpriseContact contact = new EnterpriseContact();
            contact.setTenantId(tenantId);
            contact.setCreditCode(creditCode);
            contact.setCompanyName(companyName);
            contact.setContactName(names[i]);
            contact.setPosition(positions[i]);
            contact.setDepartment("销售部");
            contact.setPhone(phones[i]);
            contact.setTelephone("010-1234567" + i);
            contact.setEmail(emails[i]);
            contact.setSource("wdyl");
            contact.setSourceType("企业年报");
            contact.setReliability(2);
            contact.setIsImported(0);
            contact.setQueryUserId(userId);
            contact.setQueryTime(now);
            contact.setCreatedTime(now);
            contact.setUpdatedTime(now);
            enterpriseContactMapper.insert(contact);
            contacts.add(contact);
        }

        // 更新配额计数
        updateContactQuota(tenantId);

        return contacts.stream().map(this::toContactVO).toList();
    }

    private void updateContactQuota(Long tenantId) {
        LambdaQueryWrapper<ThirdPartyConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdPartyConfig::getTenantId, tenantId)
                .eq(ThirdPartyConfig::getProvider, PROVIDER_WDYL)
                .last("LIMIT 1");
        ThirdPartyConfig config = thirdPartyConfigMapper.selectOne(wrapper);
        if (config != null) {
            config.setContactDailyUsed((config.getContactDailyUsed() != null ? config.getContactDailyUsed() : 0) + 1);
            config.setContactMonthlyUsed((config.getContactMonthlyUsed() != null ? config.getContactMonthlyUsed() : 0) + 1);
            thirdPartyConfigMapper.updateById(config);
        }
    }

    private List<EnterpriseContact> findContactsByCreditCode(Long tenantId, String creditCode) {
        LambdaQueryWrapper<EnterpriseContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseContact::getTenantId, tenantId)
                .eq(EnterpriseContact::getCreditCode, creditCode);
        return enterpriseContactMapper.selectList(wrapper);
    }

    private Map<String, Customer> findCustomersByCreditCode(Long tenantId, List<String> creditCodes) {
        if (creditCodes.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getTenantId, tenantId)
                .in(Customer::getErpCustomerId, creditCodes);
        List<Customer> customers = customerMapper.selectList(wrapper);
        return customers.stream().collect(Collectors.toMap(Customer::getErpCustomerId, c -> c, (a, b) -> a));
    }

    private Map<String, Long> countContactsByCreditCode(Long tenantId, List<String> creditCodes) {
        if (creditCodes.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<EnterpriseContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseContact::getTenantId, tenantId)
                .in(EnterpriseContact::getCreditCode, creditCodes);
        List<EnterpriseContact> contacts = enterpriseContactMapper.selectList(wrapper);
        return contacts.stream()
                .collect(Collectors.groupingBy(EnterpriseContact::getCreditCode, Collectors.counting()));
    }

    private EnterpriseSearchVO toSearchVO(EnterpriseQueryCache cache) {
        EnterpriseSearchVO vo = new EnterpriseSearchVO();
        vo.setCompanyName(cache.getCompanyName());
        vo.setCreditCode(cache.getCreditCode());
        vo.setLegalPerson(cache.getLegalPerson());
        vo.setRegisteredCapital(cache.getRegisteredCapital());
        vo.setEstablishedDate(cache.getEstablishedDate());
        vo.setCompanyStatus(cache.getCompanyStatus());
        vo.setIndustry(cache.getIndustry());
        vo.setProvince(cache.getProvince());
        vo.setCity(cache.getCity());
        return vo;
    }

    private EnterpriseContactVO toContactVO(EnterpriseContact contact) {
        EnterpriseContactVO vo = new EnterpriseContactVO();
        vo.setId(contact.getId());
        vo.setContactName(contact.getContactName());
        vo.setPosition(contact.getPosition());
        vo.setDepartment(contact.getDepartment());
        vo.setPhone(maskPhone(contact.getPhone()));
        vo.setPhoneFull(contact.getPhone());
        vo.setTelephone(contact.getTelephone());
        vo.setEmail(maskEmail(contact.getEmail()));
        vo.setEmailFull(contact.getEmail());
        vo.setSourceType(contact.getSourceType());
        vo.setSourceTypeLabel(contact.getSourceType());
        vo.setReliability(contact.getReliability());
        vo.setReliabilityLabel(getReliabilityLabel(contact.getReliability()));
        vo.setIsImported(Objects.equals(contact.getIsImported(), 1));
        vo.setImportedCustomerId(contact.getImportedCustomerId());
        vo.setExistsInCrm(contact.getImportedCustomerId() != null);
        return vo;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        if (local.length() <= 2) {
            return "**" + email.substring(at);
        }
        return local.substring(0, 2) + "****" + email.substring(at);
    }

    private String getReliabilityLabel(Integer reliability) {
        if (reliability == null) return "";
        return switch (reliability) {
            case 1 -> "低";
            case 2 -> "中";
            case 3 -> "高";
            default -> "";
        };
    }
}
