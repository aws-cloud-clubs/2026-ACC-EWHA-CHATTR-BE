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
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
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

    @Bean
    public DynamoDbClient dynamoDbClient() {
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create());
        if (dynamoEndpoint != null && !dynamoEndpoint.isBlank()) {
            builder.endpointOverride(URI.create(dynamoEndpoint));
        }
        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    }

    @Bean
    public DynamoDbTable<User> userTable(DynamoDbEnhancedClient client) {
        return client.table("chattr-user", TableSchema.fromBean(User.class));
    }

    @Bean
    public DynamoDbTable<Workspace> workspaceTable(DynamoDbEnhancedClient client) {
        return client.table("chattr-workspace", TableSchema.fromBean(Workspace.class));
    }

    @Bean
    public DynamoDbTable<WorkspaceMember> workspaceMemberTable(DynamoDbEnhancedClient client) {
        return client.table("chattr-workspace-member", TableSchema.fromBean(WorkspaceMember.class));
    }

    @Bean
    public DynamoDbTable<Channel> channelTable(DynamoDbEnhancedClient client) {
        return client.table("chattr-channel", TableSchema.fromBean(Channel.class));
    }

    @Bean
    public DynamoDbTable<ChannelMember> channelMemberTable(DynamoDbEnhancedClient client) {
        return client.table("chattr-channel-member", TableSchema.fromBean(ChannelMember.class));
    }

    @Bean
    public DynamoDbTable<Dm> dmTable(DynamoDbEnhancedClient client) {
        return client.table("chattr-dm", TableSchema.fromBean(Dm.class));
    }

    @Bean
    public DynamoDbTable<Message> messageTable(DynamoDbEnhancedClient client) {
        return client.table("chattr-message", TableSchema.fromBean(Message.class));
    }
}
