package org.webchat.webchatbackend.util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
public class AESUtil {

    @Value("${security.secret-key}")
    private String SECRET_KEY;

    // AES 解密方法
    // 解密方法
    public String decrypt(String encryptedMessage) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

}
