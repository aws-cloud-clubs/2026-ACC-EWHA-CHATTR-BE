package com.acc.chattr.domain.auth.repository;

import com.acc.chattr.domain.auth.entity.Device;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository {
    void save(Device device);
    Optional<Device> findByUserIdAndDeviceId(String userId, String deviceId);
    List<Device> findByUserId(String userId);
}
