package com.acc.chattr.domain.auth.repository;

import com.acc.chattr.domain.auth.entity.Device;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;

@Repository
public class DeviceDynamoRepository implements DeviceRepository {

    private final DynamoDbTable<Device> table;

    public DeviceDynamoRepository(DynamoDbTable<Device> deviceTable) {
        this.table = deviceTable;
    }

    @Override
    public void save(Device device) {
        table.putItem(device);
    }

    @Override
    public Optional<Device> findByUserIdAndDeviceId(String userId, String deviceId) {
        Device device = table.getItem(Key.builder()
            .partitionValue(userId)
            .sortValue(deviceId)
            .build());
        if (device == null || device.isDeleted()) return Optional.empty();
        return Optional.of(device);
    }

    @Override
    public List<Device> findByUserId(String userId) {
        return table.query(QueryConditional.keyEqualTo(Key.builder().partitionValue(userId).build()))
            .stream()
            .flatMap(page -> page.items().stream())
            .filter(d -> !d.isDeleted())
            .toList();
    }
}
