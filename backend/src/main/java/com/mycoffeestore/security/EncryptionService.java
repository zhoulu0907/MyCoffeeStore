package com.mycoffeestore.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * AES-256-GCM 加密服务
 * 用于加密存储 API Key 等敏感信息
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EncryptionService {

    private final MasterKeyManager masterKeyManager;

    /**
     * AES-GCM 参数
     */
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // 位
    private static final int GCM_IV_LENGTH = 12;   // 字节
    private static final int KEY_LENGTH = 32;      // 字节（AES-256）

    /**
     * 加密字符串
     *
     * @param plaintext 明文
     * @return 加密结果，包含 Base64 编码的密文和 IV
     */
    public EncryptionResult encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("明文不能为空");
        }

        try {
            SecretKey key = masterKeyManager.getMasterKey();

            // 生成随机 IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // 初始化加密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            // 加密
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // 返回加密结果（Base64 编码）
            String encryptedBase64 = Base64.getEncoder().encodeToString(ciphertext);
            String ivBase64 = Base64.getEncoder().encodeToString(iv);

            return new EncryptionResult(encryptedBase64, ivBase64);
        } catch (Exception e) {
            log.error("加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密字符串
     *
     * @param encryptedBase64 Base64 编码的密文
     * @param ivBase64        Base64 编码的 IV
     * @return 明文
     */
    public String decrypt(String encryptedBase64, String ivBase64) {
        if (encryptedBase64 == null || encryptedBase64.isEmpty()) {
            throw new IllegalArgumentException("密文不能为空");
        }
        if (ivBase64 == null || ivBase64.isEmpty()) {
            throw new IllegalArgumentException("IV 不能为空");
        }

        try {
            SecretKey key = masterKeyManager.getMasterKey();

            // 解码 Base64
            byte[] ciphertext = Base64.getDecoder().decode(encryptedBase64);
            byte[] iv = Base64.getDecoder().decode(ivBase64);

            // 初始化解密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            // 解密
            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密失败: {}", e.getMessage(), e);
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 哈希字符串（用于验证，不可逆）
     *
     * @param input 输入字符串
     * @return SHA-256 哈希值（Base64 编码）
     */
    public String hash(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("输入不能为空");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("哈希计算失败: {}", e.getMessage(), e);
            throw new RuntimeException("哈希计算失败", e);
        }
    }

    /**
     * 验证哈希值
     *
     * @param input    输入字符串
     * @param hashBase64 期望的哈希值
     * @return 是否匹配
     */
    public boolean verifyHash(String input, String hashBase64) {
        String computedHash = hash(input);
        return computedHash.equals(hashBase64);
    }

    /**
     * 生成 API Key 掩码（用于显示）
     *
     * @param apiKey API Key
     * @return 掩码后的字符串（如 sk-***abc123）
     */
    public String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "";
        }

        int length = apiKey.length();
        if (length <= 8) {
            return "***";
        }

        // 保留前缀（通常是 sk- 或类似）和后缀
        String prefix = apiKey.substring(0, Math.min(3, length));
        String suffix = apiKey.substring(Math.max(length - 4, 0));
        return prefix + "***" + suffix;
    }

    /**
     * 加密结果
     */
    public record EncryptionResult(
            String encryptedData,
            String iv
    ) {
    }
}
