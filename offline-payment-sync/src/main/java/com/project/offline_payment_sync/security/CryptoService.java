

package com.project.offline_payment_sync.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;

@Service
public class CryptoService {

    
    @Value("${HMAC_SECRET}")
private String hmacSecret;

    
  
    public String generateHmac(String data) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key =
                  
         new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC signature", e);
        }
    }

    
    public boolean verifyHmac(String data, String signature) {
        String computed = generateHmac(data);
        return computed.equals(signature);
    }
}
