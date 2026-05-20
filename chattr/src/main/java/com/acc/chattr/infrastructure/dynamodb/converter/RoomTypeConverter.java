package com.acc.chattr.infrastructure.dynamodb.converter;

import com.acc.chattr.domain.message.RoomType;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class RoomTypeConverter implements AttributeConverter<RoomType> {
    public static final RoomTypeConverter INSTANCE = new RoomTypeConverter();

    @Override
    public AttributeValue transformFrom(RoomType input) {
        return AttributeValue.builder().s(input.name()).build();
    }

    @Override
    public RoomType transformTo(AttributeValue input) {
        return RoomType.valueOf(input.s());
    }

    @Override
    public EnhancedType<RoomType> type() {
        return EnhancedType.of(RoomType.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
