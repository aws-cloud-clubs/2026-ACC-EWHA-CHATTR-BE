package com.acc.chattr.common.util;

import com.acc.chattr.common.code.GeneralErrorCode;
import com.acc.chattr.common.exception.GeneralException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DynamoDB {@code LastEvaluatedKey}를 URL-safe Base64 문자열(커서)로 인코딩/디코딩합니다.
 *
 * 현재 스키마의 모든 키 속성은 문자열(S) 타입만 사용하므로,
 * {@link AttributeValue}를 {@code Map<String, String>}으로 단순화하여 직렬화합니다.
 */
@UtilityClass
public class CursorUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};

    /**
     * LastEvaluatedKey → URL-safe Base64 커서 문자열.
     * {@code null} 또는 빈 맵이면 {@code null} 반환.
     */
    public static String encode(Map<String, AttributeValue> lastKey) {
        if (lastKey == null || lastKey.isEmpty()) return null;
        try {
            Map<String, String> keyMap = lastKey.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().s() != null ? e.getValue().s() : e.getValue().n()
                ));
            byte[] json = MAPPER.writeValueAsBytes(keyMap);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * URL-safe Base64 커서 문자열 → LastEvaluatedKey.
     * {@code null} 또는 빈 문자열이면 {@code null} 반환 (첫 페이지 시작).
     *
     * @throws GeneralException 커서 형식이 잘못된 경우 400
     */
    public static Map<String, AttributeValue> decode(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            byte[] json = Base64.getUrlDecoder().decode(cursor);
            Map<String, String> keyMap = MAPPER.readValue(json, MAP_TYPE);
            return keyMap.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> AttributeValue.builder().s(e.getValue()).build()
                ));
        } catch (Exception e) {
            throw new GeneralException(GeneralErrorCode.INVALID_REQUEST_PARAMETER);
        }
    }
}
