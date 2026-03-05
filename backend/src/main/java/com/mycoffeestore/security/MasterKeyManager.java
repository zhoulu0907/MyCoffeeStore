package com.mycoffeestore.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 主密钥管理器
 * 负责管理和提供加密服务的主密钥
 * 支持多种密钥来源：环境变量、本地文件、自动生成
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Slf4j
@Component
public class MasterKeyManager {

    /**
     * AES-256 密钥长度：32 字节
     */
    private static final int KEY_SIZE = 32;

    /**
     * 密钥来源类型
     */
    public enum KeySource {
        ENV,        // 环境变量
        FILE,       // 本地文件
        GENERATED   // 自动生成
    }

    @Value("${llm.encryption.master-key-env:LLM_MASTER_KEY}")
    private String masterKeyEnvName;

    @Value("${llm.encryption.master-key-file:${user.home}/.mycoffeestore/master.key}")
    private String masterKeyFilePath;

    private SecretKey masterKey;
    private KeySource keySource;

    /**
     * 初始化主密钥
     */
    public void init() {
        if (loadFromEnvironment()) {
            log.info("主密钥从环境变量加载成功");
            return;
        }

        if (loadFromFile()) {
            log.info("主密钥从文件加载成功: {}", masterKeyFilePath);
            return;
        }

        if (generateAndSaveKey()) {
            log.info("主密钥自动生成并保存到文件: {}", masterKeyFilePath);
            return;
        }

        throw new IllegalStateException("无法初始化主密钥");
    }

    /**
     * 从环境变量加载密钥
     */
    private boolean loadFromEnvironment() {
        try {
            String keyBase64 = System.getenv(masterKeyEnvName);
            if (keyBase64 == null || keyBase64.isEmpty()) {
                return false;
            }

            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            if (keyBytes.length != KEY_SIZE) {
                log.warn("环境变量中的密钥长度不正确，应为 {} 字节", KEY_SIZE);
                return false;
            }

            masterKey = new SecretKeySpec(keyBytes, "AES");
            keySource = KeySource.ENV;
            return true;
        } catch (Exception e) {
            log.debug("从环境变量加载密钥失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从文件加载密钥
     */
    private boolean loadFromFile() {
        try {
            File keyFile = new File(masterKeyFilePath);
            if (!keyFile.exists()) {
                return false;
            }

            String keyBase64 = Files.readString(Paths.get(masterKeyFilePath), StandardCharsets.UTF_8)
                    .trim();
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);

            if (keyBytes.length != KEY_SIZE) {
                log.warn("文件中的密钥长度不正确，应为 {} 字节", KEY_SIZE);
                return false;
            }

            masterKey = new SecretKeySpec(keyBytes, "AES");
            keySource = KeySource.FILE;
            return true;
        } catch (Exception e) {
            log.debug("从文件加载密钥失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 生成新密钥并保存到文件
     */
    private boolean generateAndSaveKey() {
        try {
            // 生成随机密钥
            byte[] keyBytes = new byte[KEY_SIZE];
            new SecureRandom().nextBytes(keyBytes);
            masterKey = new SecretKeySpec(keyBytes, "AES");

            // 确保目录存在
            File keyFile = new File(masterKeyFilePath);
            File parentDir = keyFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 保存密钥到文件
            String keyBase64 = Base64.getEncoder().encodeToString(keyBytes);
            Files.writeString(Paths.get(masterKeyFilePath), keyBase64, StandardCharsets.UTF_8);

            // 设置文件权限（仅所有者可读写）
            keyFile.setReadable(false, false);
            keyFile.setReadable(true, true);
            keyFile.setWritable(false, false);
            keyFile.setWritable(true, true);

            keySource = KeySource.GENERATED;
            return true;
        } catch (IOException e) {
            log.error("生成并保存密钥失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取主密钥
     */
    public SecretKey getMasterKey() {
        if (masterKey == null) {
            synchronized (this) {
                if (masterKey == null) {
                    init();
                }
            }
        }
        return masterKey;
    }

    /**
     * 获取密钥来源
     */
    public KeySource getKeySource() {
        if (masterKey == null) {
            getMasterKey();
        }
        return keySource;
    }

    /**
     * 验证密钥是否已初始化
     */
    public boolean isInitialized() {
        return masterKey != null;
    }
}
