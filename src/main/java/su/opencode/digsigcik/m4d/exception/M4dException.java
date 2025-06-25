package su.opencode.digsigcik.m4d.exception;

import org.springframework.http.HttpStatus;
import su.opencode.digsigcik.common.exception.custom.BaseException;

public class M4dException extends BaseException {

    public M4dException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }

    public M4dException(String reason, String code) {
        super(HttpStatus.BAD_REQUEST, reason, code);
    }

    @Override
    protected String getDefaultCode() {
        return "CODE_400";
    }
}
