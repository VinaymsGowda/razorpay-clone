package com.vinayms.razorpayclone.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomizerUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String randomBase64(int length) {
      byte[] buf=new byte[length];

        // generates random bytes inside given buffer
        SECURE_RANDOM.nextBytes(buf);

        // uses url encoder and removes padding(xyz==) at end from generated value
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);


    }
}
