package com.acc.chattr.domain.auth.repository;

import com.acc.chattr.domain.auth.entity.Device;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.util.List;
import java.util.Optional;

@Repository
public class DeviceDynamoRepository implements DeviceRepository {

    private static final int BATCH_SIZE = 25;

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Device> table;

    public DeviceDynamoRepository(DynamoDbEnhancedClient enhancedClient, DynamoDbTable<Device> deviceTable) {
        this.enhancedClient = enhancedClient;
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

    @Override
    public void deleteAllByUserId(String userId) {
        List<Device> items = table.query(
                QueryConditional.keyEqualTo(Key.builder().partitionValue(userId).build()))
            .stream()
            .flatMap(page -> page.items().stream())
            .toList();
        batchDelete(items);
    }

    private void batchDelete(List<Device> items) {
        for (int i = 0; i < items.size(); i += BATCH_SIZE) {
            List<Device> batch = items.subList(i, Math.min(i + BATCH_SIZE, items.size()));
            WriteBatch.Builder<Device> batchBuilder = WriteBatch.builder(Device.class)
                .mappedTableResource(table);
            batch.forEach(batchBuilder::addDeleteItem);
            BatchWriteResult result = enhancedClient.batchWriteItem(
                BatchWriteItemEnhancedRequest.builder().writeBatches(batchBuilder.build()).build());
            List<Key> unprocessed = result.unprocessedDeleteItemsForTable(table);
            while (!unprocessed.isEmpty()) {
                WriteBatch.Builder<Device> retryBuilder = WriteBatch.builder(Device.class)
                    .mappedTableResource(table);
                unprocessed.forEach(retryBuilder::addDeleteItem);
                result = enhancedClient.batchWriteItem(
                    BatchWriteItemEnhancedRequest.builder().writeBatches(retryBuilder.build()).build());
                unprocessed = result.unprocessedDeleteItemsForTable(table);
            }
        }
    }
}
