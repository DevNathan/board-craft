package com.app.boardcraftback.support.error;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode code;

    public AppException(ErrorCode code) {
        super(code.name());
        this.code = code;
    }

    public AppException(ErrorCode code, Throwable cause) {
        super(code.name(), cause);
        this.code = code;
    }

}