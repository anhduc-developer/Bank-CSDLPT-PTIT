package dev.distributed.bank.exception;

public class SiteDownException extends RuntimeException {
    public SiteDownException(String message) {
        super(message);
    }
}
