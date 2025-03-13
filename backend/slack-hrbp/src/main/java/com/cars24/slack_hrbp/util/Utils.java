package com.cars24.slack_hrbp.util;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
public class Utils {

    private String alpha = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Random random = new SecureRandom();

    public String generateUserId(int length){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i<length; i++)
            stringBuilder.append(alpha.charAt(random.nextInt(alpha.length())));

        return stringBuilder.toString();
    }
}