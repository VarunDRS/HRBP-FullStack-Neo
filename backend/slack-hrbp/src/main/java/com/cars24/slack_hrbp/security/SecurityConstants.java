
package com.cars24.slack_hrbp.security;

import io.github.cdimascio.dotenv.Dotenv;

public class SecurityConstants {

    static Dotenv dotenv = Dotenv.load();

    public static final long EXPIRATION_TIME=864000000; // 10 days
    public static final String TOKEN_PREFIX="Bearer ";
    public static final String HEADER_STRING="Authorization";
    public static final String SIGN_UP_URL="/users/signup";
    public static final String TOKEN_SECRET= dotenv.get("TOKEN_SECRET");

}


