package com.acc.chattr.common.exception;

import com.acc.chattr.common.code.GeneralErrorCode;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final GeneralErrorCode errorCode;

    public GeneralException(GeneralErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
