package com.vinayms.razorpayclone.vault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class VaultEncryption {

    @Value("${DEK_ENCRYPTION_KEY}")
    private String masterKey;

    public static BytesEncryptor panEncryptor(byte[] dek){
        SecretKeySpec secretKey = new SecretKeySpec(dek, "AES");

        BytesKeyGenerator salt= KeyGenerators.secureRandom(16);
        return new AesBytesEncryptor(secretKey, salt, AesBytesEncryptor.CipherAlgorithm.GCM);
    }

    @Bean
    public BytesEncryptor dekEncryptor(){
        byte[] masterKeyBytes= Base64.getDecoder().decode(masterKey);
        SecretKeySpec secretKey = new SecretKeySpec(masterKeyBytes, "AES");
        BytesKeyGenerator salt= KeyGenerators.secureRandom(16);

        return new AesBytesEncryptor(
                secretKey, salt, AesBytesEncryptor.CipherAlgorithm.GCM
        );
    }
}
