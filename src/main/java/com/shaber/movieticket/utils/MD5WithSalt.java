package com.shaber.movieticket.utils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class MD5WithSalt {

    // 使用MD5和盐值加密密码
    public static String encryptPasswordWithSalt(String password) throws NoSuchAlgorithmException {
        // 生成盐值
        String salt = generateSalt();

        // 合并密码和盐值
        String saltedPassword = password + salt;

        // 使用MD5加密
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(saltedPassword.getBytes());

        // 将加密后的字节数组转换成十六进制字符串
        String hashString = bytesToHex(hash);

        // 将盐值和加密后的密码结合在一起存储
        return hashString + ":" + salt; // 密码和盐值用冒号分隔
    }

    // 验证密码是否与已加密密码匹配
    public static boolean verifyPassword(String inputPassword, String storedPassword) throws NoSuchAlgorithmException {
        // 从存储的加密密码中分离出加密后的密码和盐值
        String[] parts = storedPassword.split(":");
        String encryptedPassword = parts[0];
        String salt = parts[1];

        // 将输入的密码与盐值结合后加密
        String saltedPassword = inputPassword + salt;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(saltedPassword.getBytes());

        // 将加密后的字节数组转换成十六进制字符串
        String hashString = bytesToHex(hash);

        // 比较加密后的密码是否与存储的密码一致
        return hashString.equals(encryptedPassword);
    }

    // 生成随机盐值
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16]; // 盐值的长度，通常是16字节
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes); // 将盐值编码为Base64字符串
    }

    // 将字节数组转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String password = "123";

        // 加密密码
        String encryptedPassword = encryptPasswordWithSalt(password);
        System.out.println("加密密码: " + encryptedPassword);

        // 验证密码
        String inputPassword = "123";
        boolean isPasswordCorrect = verifyPassword(inputPassword, encryptedPassword);
        System.out.println("验证密码: " + isPasswordCorrect);
    }
}
