package com.acc.chattr.common.exception;

import com.acc.chattr.common.code.BusinessErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final BusinessErrorCode businessErrorCode;

    public BusinessException(BusinessErrorCode errorCode) {
        super(errorCode.getMessage());
        this.businessErrorCode = errorCode;
    }
}
