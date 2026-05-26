package com.acc.chattr.domain.auth.service;

import com.acc.chattr.common.code.BusinessErrorCode;
import com.acc.chattr.common.exception.BusinessException;
import com.acc.chattr.domain.auth.dto.DeviceResponse;
import com.acc.chattr.domain.auth.dto.RegisterDeviceRequest;
import com.acc.chattr.domain.auth.entity.Device;
import com.acc.chattr.domain.auth.repository.DeviceRepository;
import com.acc.chattr.domain.user.entity.User;
import com.acc.chattr.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceService(DeviceRepository deviceRepository, UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    public DeviceResponse register(String cognitoSub, RegisterDeviceRequest request) {
        User user = getUser(cognitoSub);

        Device device = deviceRepository
            .findByUserIdAndDeviceId(user.getId(), request.deviceId())
            .map(existing -> {
                existing.refreshActivity();
                existing.setDeviceName(request.deviceName());
                existing.setPlatform(request.platform());
                return existing;
            })
            .orElseGet(() -> Device.create(
                user.getId(),
                request.deviceId(),
                request.deviceName(),
                request.platform()
            ));

        deviceRepository.save(device);
        return DeviceResponse.from(device);
    }

    public List<DeviceResponse> getDevices(String cognitoSub) {
        User user = getUser(cognitoSub);
        return deviceRepository.findByUserId(user.getId()).stream()
            .map(DeviceResponse::from)
            .toList();
    }

    public void logout(String cognitoSub) {
        User user = getUser(cognitoSub);
        deviceRepository.deleteAllByUserId(user.getId());
    }

    private User getUser(String cognitoSub) {
        return userRepository.findByCognitoSub(cognitoSub)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND));
    }
}
