package com.acc.chattr.config;

import com.acc.chattr.domain.channel.Channel;
import com.acc.chattr.domain.channel.ChannelMember;
import com.acc.chattr.domain.dm.Dm;
import com.acc.chattr.domain.message.Message;
import com.acc.chattr.domain.user.User;
import com.acc.chattr.domain.workspace.Workspace;
import com.acc.chattr.domain.workspace.WorkspaceMember;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;

@Configuration
public class DynamoDbConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.dynamodb.endpoint:}")
    private String dynamoEndpoint;

    @Value("${chattr.dynamodb.table.user}")
    private String userTableName;

    @Value("${chattr.dynamodb.table.workspace}")
    private String workspaceTableName;

    @Value("${chattr.dynamodb.table.workspace-member}")
    private String workspaceMemberTableName;

    @Value("${chattr.dynamodb.table.channel}")
    private String channelTableName;

    @Value("${chattr.dynamodb.table.channel-member}")
    private String channelMemberTableName;

    @Value("${chattr.dynamodb.table.dm}")
    private String dmTableName;

    @Value("${chattr.dynamodb.table.message}")
    private String messageTableName;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
            .region(Region.of(region));
        if (dynamoEndpoint != null && !dynamoEndpoint.isBlank()) {
            // 로컬 DynamoDB Local — 더미 credentials 사용
            builder.endpointOverride(URI.create(dynamoEndpoint))
                   .credentialsProvider(StaticCredentialsProvider.create(
                       AwsBasicCredentials.create("local", "local")));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    }

    @Bean
    public DynamoDbTable<User> userTable(DynamoDbEnhancedClient client) {
        return client.table(userTableName, TableSchema.fromBean(User.class));
    }

    @Bean
    public DynamoDbTable<Workspace> workspaceTable(DynamoDbEnhancedClient client) {
        return client.table(workspaceTableName, TableSchema.fromBean(Workspace.class));
    }

    @Bean
    public DynamoDbTable<WorkspaceMember> workspaceMemberTable(DynamoDbEnhancedClient client) {
        return client.table(workspaceMemberTableName, TableSchema.fromBean(WorkspaceMember.class));
    }

    @Bean
    public DynamoDbTable<Channel> channelTable(DynamoDbEnhancedClient client) {
        return client.table(channelTableName, TableSchema.fromBean(Channel.class));
    }

    @Bean
    public DynamoDbTable<ChannelMember> channelMemberTable(DynamoDbEnhancedClient client) {
        return client.table(channelMemberTableName, TableSchema.fromBean(ChannelMember.class));
    }

    @Bean
    public DynamoDbTable<Dm> dmTable(DynamoDbEnhancedClient client) {
        return client.table(dmTableName, TableSchema.fromBean(Dm.class));
    }

    @Bean
    public DynamoDbTable<Message> messageTable(DynamoDbEnhancedClient client) {
        return client.table(messageTableName, TableSchema.fromBean(Message.class));
    }
}
