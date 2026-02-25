package com.aicrm.module.fieldwork.trajectory.service;

import com.aicrm.module.fieldwork.trajectory.dto.LocationReportDTO;
import com.aicrm.module.fieldwork.trajectory.dto.TrajectoryVO;

/**
 * 行动轨迹服务
 */
public interface TrajectoryService {

    void reportLocation(LocationReportDTO dto);

    TrajectoryVO getDailyTrajectory(Long userId, String date);
}
