package com.acc.chattr.domain.workspace.repository;

import com.acc.chattr.domain.workspace.entity.WorkspaceRole;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class WorkspaceRoleConverter implements AttributeConverter<WorkspaceRole> {
    public static final WorkspaceRoleConverter INSTANCE = new WorkspaceRoleConverter();

    @Override
    public AttributeValue transformFrom(WorkspaceRole input) {
        return AttributeValue.builder().s(input.name()).build();
    }

    @Override
    public WorkspaceRole transformTo(AttributeValue input) {
        return WorkspaceRole.valueOf(input.s());
    }

    @Override
    public EnhancedType<WorkspaceRole> type() {
        return EnhancedType.of(WorkspaceRole.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
