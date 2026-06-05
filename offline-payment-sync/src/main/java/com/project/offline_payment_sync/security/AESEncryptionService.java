


package com.project.offline_payment_sync.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;


@Service
public class AESEncryptionService {

    
    
    @Value("${AES_SECRET}")
private String secret;

    private static final String ALGORITHM = "AES";

    private SecretKey getKey() {
       
        byte[] decodedKey = secret.getBytes();
        
        // Validate key length
        if (decodedKey.length != 16 && decodedKey.length != 24 && decodedKey.length != 32) {
            throw new IllegalArgumentException(
                "Invalid AES key length: " + decodedKey.length + " bytes. " +
                "Must be 16 (128-bit), 24 (192-bit), or 32 (256-bit) bytes."
            );
        }
        
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey());

            byte[] encrypted = cipher.doFinal(data.getBytes());

            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed: " + e.getMessage(), e);
        }
    }

    
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKey());

            byte[] decoded = Base64.getDecoder().decode(encryptedData);

            return new String(cipher.doFinal(decoded));

        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed: " + e.getMessage(), e);
        }
    }
}