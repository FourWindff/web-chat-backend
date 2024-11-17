package org.webchat.webchatbackend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class AESUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding"; // Adjust if needed

    // Method to decrypt the data
    public String decrypt(String encryptedData, String base64Key) throws Exception {
        try {

            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);


            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);


            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("Decryption error: ", e);
            throw e;
        }
    }
}
