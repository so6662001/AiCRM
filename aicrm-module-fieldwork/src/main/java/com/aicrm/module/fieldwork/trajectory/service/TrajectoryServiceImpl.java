package com.aicrm.module.fieldwork.trajectory.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.customer.entity.Customer;
import com.aicrm.module.crm.customer.mapper.CustomerMapper;
import com.aicrm.module.fieldwork.checkin.entity.CheckinRecord;
import com.aicrm.module.fieldwork.checkin.mapper.CheckinRecordMapper;
import com.aicrm.module.fieldwork.trajectory.dto.LocationReportDTO;
import com.aicrm.module.fieldwork.trajectory.dto.TrajectoryPointVO;
import com.aicrm.module.fieldwork.trajectory.dto.TrajectoryVO;
import com.aicrm.module.fieldwork.trajectory.entity.LocationReport;
import com.aicrm.module.fieldwork.trajectory.mapper.LocationReportMapper;
import com.aicrm.module.fieldwork.visit.entity.VisitRecord;
import com.aicrm.module.fieldwork.visit.mapper.VisitRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行动轨迹服务实现
 */
@Service
@RequiredArgsConstructor
public class TrajectoryServiceImpl implements TrajectoryService {

    private static final String TYPE_LOCATION = "location";
    private static final String TYPE_CHECKIN = "checkin";
    private static final String TYPE_VISIT_CHECKIN = "visit_checkin";

    private final LocationReportMapper locationReportMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final VisitRecordMapper visitRecordMapper;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportLocation(LocationReportDTO dto) {
        LocationReport report = new LocationReport();
        BeanUtil.copyProperties(dto, report);
        report.setTenantId(TenantContext.getTenantId());
        report.setUserId(TenantContext.getUserId());
        report.setReportTime(LocalDateTime.now());
        report.setCreatedTime(LocalDateTime.now());

        locationReportMapper.insert(report);
    }

    @Override
    public TrajectoryVO getDailyTrajectory(Long userId, String date) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return emptyTrajectory(userId, date);
        }

        Long effectiveUserId = userId != null ? userId : TenantContext.getUserId();
        if (effectiveUserId == null) {
            return emptyTrajectory(null, date);
        }

        LocalDate targetDate = date != null && !date.isBlank()
                ? LocalDate.parse(date)
                : LocalDate.now();
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        List<TrajectoryPointVO> points = new ArrayList<>();

        // 1. 查询 location_report
        LambdaQueryWrapper<LocationReport> lrWrapper = new LambdaQueryWrapper<>();
        lrWrapper.eq(LocationReport::getTenantId, tenantId)
                .eq(LocationReport::getUserId, effectiveUserId)
                .ge(LocationReport::getReportTime, startOfDay)
                .le(LocationReport::getReportTime, endOfDay)
                .orderByAsc(LocationReport::getReportTime);
        List<LocationReport> locationReports = locationReportMapper.selectList(lrWrapper);

        for (LocationReport lr : locationReports) {
            TrajectoryPointVO point = new TrajectoryPointVO();
            point.setLongitude(lr.getLongitude());
            point.setLatitude(lr.getLatitude());
            point.setTime(lr.getReportTime());
            point.setAddress(lr.getAddress());
            point.setType(TYPE_LOCATION);
            point.setLabel("定位");
            points.add(point);
        }

        // 2. 查询 checkin_record
        LambdaQueryWrapper<CheckinRecord> crWrapper = new LambdaQueryWrapper<>();
        crWrapper.eq(CheckinRecord::getTenantId, tenantId)
                .eq(CheckinRecord::getUserId, effectiveUserId)
                .ge(CheckinRecord::getCheckinTime, startOfDay)
                .le(CheckinRecord::getCheckinTime, endOfDay)
                .orderByAsc(CheckinRecord::getCheckinTime);
        List<CheckinRecord> checkinRecords = checkinRecordMapper.selectList(crWrapper);

        for (CheckinRecord cr : checkinRecords) {
            TrajectoryPointVO point = new TrajectoryPointVO();
            point.setLongitude(cr.getLongitude());
            point.setLatitude(cr.getLatitude());
            point.setTime(cr.getCheckinTime());
            point.setAddress(cr.getAddress());
            point.setVisitId(cr.getRelatedVisitId());

            if (cr.getCheckinType() != null && cr.getCheckinType() == 2) {
                point.setType(TYPE_VISIT_CHECKIN);
                point.setLabel("拜访签到");
                if (cr.getRelatedVisitId() != null) {
                    VisitRecord visit = visitRecordMapper.selectById(cr.getRelatedVisitId());
                    if (visit != null && visit.getCustomerId() != null) {
                        Customer customer = customerMapper.selectById(visit.getCustomerId());
                        point.setCustomerName(customer != null ? customer.getCustomerName() : null);
                    }
                }
            } else {
                point.setType(TYPE_CHECKIN);
                point.setLabel("打卡");
            }
            points.add(point);
        }

        // 按时间排序
        points.sort(Comparator.comparing(TrajectoryPointVO::getTime));

        TrajectoryVO vo = new TrajectoryVO();
        vo.setUserId(effectiveUserId);
        vo.setUserName(resolveUserName(effectiveUserId));
        vo.setDate(targetDate.toString());
        vo.setPoints(points);
        vo.setTotalStops(points.size());
        vo.setTotalDistance(calculateTotalDistance(points));

        return vo;
    }

    private TrajectoryVO emptyTrajectory(Long userId, String date) {
        TrajectoryVO vo = new TrajectoryVO();
        vo.setUserId(userId);
        vo.setUserName(null);
        vo.setDate(date != null ? date : LocalDate.now().toString());
        vo.setPoints(List.of());
        vo.setTotalStops(0);
        vo.setTotalDistance(0.0);
        return vo;
    }

    private Double calculateTotalDistance(List<TrajectoryPointVO> points) {
        if (points == null || points.size() < 2) {
            return 0.0;
        }
        double total = 0;
        for (int i = 1; i < points.size(); i++) {
            TrajectoryPointVO p1 = points.get(i - 1);
            TrajectoryPointVO p2 = points.get(i);
            if (p1.getLongitude() != null && p1.getLatitude() != null
                    && p2.getLongitude() != null && p2.getLatitude() != null) {
                total += haversineDistance(
                        p1.getLatitude().doubleValue(), p1.getLongitude().doubleValue(),
                        p2.getLatitude().doubleValue(), p2.getLongitude().doubleValue()
                );
            }
        }
        return Math.round(total * 100) / 100.0;
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径(公里)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String resolveUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        // TODO: 集成用户服务获取用户名
        return null;
    }
}
