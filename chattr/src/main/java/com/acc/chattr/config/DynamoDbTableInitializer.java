package com.acc.chattr.config;

import com.acc.chattr.domain.channel.Channel;
import com.acc.chattr.domain.channel.ChannelMember;
import com.acc.chattr.domain.dm.Dm;
import com.acc.chattr.domain.message.Message;
import com.acc.chattr.domain.user.User;
import com.acc.chattr.domain.workspace.Workspace;
import com.acc.chattr.domain.workspace.WorkspaceMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveSpecification;
import software.amazon.awssdk.services.dynamodb.model.UpdateTimeToLiveRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamoDbTableInitializer implements ApplicationRunner {

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbTable<User> userTable;
    private final DynamoDbTable<Workspace> workspaceTable;
    private final DynamoDbTable<WorkspaceMember> workspaceMemberTable;
    private final DynamoDbTable<Channel> channelTable;
    private final DynamoDbTable<ChannelMember> channelMemberTable;
    private final DynamoDbTable<Dm> dmTable;
    private final DynamoDbTable<Message> messageTable;

    @Value("${aws.dynamodb.endpoint:}")
    private String endpoint;

    @Override
    public void run(ApplicationArguments args) {
        if (endpoint == null || endpoint.isBlank()) {
            return; // 프로덕션은 Terraform이 테이블 관리
        }
        log.info("DynamoDB Local 감지 — 테이블 초기화 시작");

        createTable(userTable,
            requestWithGsi("cognito-sub-index"));

        createTable(workspaceTable,
            CreateTableEnhancedRequest.builder()
                .billingMode(BillingMode.PAY_PER_REQUEST).build());

        createTable(workspaceMemberTable,
            requestWithGsi("user-workspaces-index"));

        createTable(channelTable,
            requestWithGsi("workspace-channels-index"));

        createTable(channelMemberTable,
            requestWithGsi("user-channels-index"));

        createTable(dmTable,
            requestWithGsi("dm-users-index"));

        createTable(messageTable,
            requestWithGsi("room-messages-index"));

        enableTtl(messageTable.tableName(), "ttl");

        log.info("DynamoDB 테이블 준비 완료");
    }

    private void createTable(DynamoDbTable<?> table, CreateTableEnhancedRequest request) {
        try {
            table.createTable(request);
            log.info("테이블 생성: {}", table.tableName());
        } catch (ResourceInUseException e) {
            log.debug("테이블 이미 존재: {}", table.tableName());
        }
    }

    private void enableTtl(String tableName, String attributeName) {
        try {
            dynamoDbClient.updateTimeToLive(UpdateTimeToLiveRequest.builder()
                .tableName(tableName)
                .timeToLiveSpecification(TimeToLiveSpecification.builder()
                    .attributeName(attributeName)
                    .enabled(true)
                    .build())
                .build());
        } catch (Exception e) {
            log.debug("TTL 이미 설정됨: {}", tableName);
        }
    }

    private CreateTableEnhancedRequest requestWithGsi(String indexName) {
        return CreateTableEnhancedRequest.builder()
            .globalSecondaryIndices(EnhancedGlobalSecondaryIndex.builder()
                .indexName(indexName)
                .projection(p -> p.projectionType(ProjectionType.ALL))
                .build())
            .billingMode(BillingMode.PAY_PER_REQUEST)
            .build();
    }
}
