package com.acc.chattr.domain.auth.dto;

import com.acc.chattr.domain.auth.entity.Device;

import java.time.Instant;

public record DeviceResponse(
    String deviceId,
    String deviceName,
    String platform,
    Instant lastActiveAt,
    Instant registeredAt
) {
    public static DeviceResponse from(Device device) {
        return new DeviceResponse(
            device.getDeviceId(),
            device.getDeviceName(),
            device.getPlatform(),
            device.getLastActiveAt(),
            device.getCreatedAt()
        );
    }
}
