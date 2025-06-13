package com.sha.ecommerce_backend.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class StringGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length() - 1)));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(generateRandomString(255));
    }
}
