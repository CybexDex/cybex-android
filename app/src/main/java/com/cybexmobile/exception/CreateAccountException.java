package com.cybexmobile.exception;

public class CreateAccountException extends Exception {

    public CreateAccountException(String strMessage) {
        super(strMessage);
    }

    public CreateAccountException(Throwable throwable) {
        super(throwable);
    }
}
