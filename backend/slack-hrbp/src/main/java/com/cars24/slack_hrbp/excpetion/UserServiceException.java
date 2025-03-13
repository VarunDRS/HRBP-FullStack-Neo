package com.cars24.slack_hrbp.excpetion;

public class UserServiceException extends RuntimeException{
    public UserServiceException(String message){
        super(message);
    }
}
