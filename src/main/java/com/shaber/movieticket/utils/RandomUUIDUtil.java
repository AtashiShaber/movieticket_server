package com.shaber.movieticket.utils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Arrays;
import java.util.Random;

public class RandomUUIDUtil {
    public static String getUUID(int n) {
        Random r = new Random();
        char[] uuid = new char[n];
        for (int i = 0; i < n; i++) {
            //true生成数字
            if (r.nextBoolean()){
                //如果第一次为true，则再进行一次随机判断是生成大写字母还是小写字母
                //true就生成小写字母
                if (r.nextBoolean()){
                    uuid[i] = (char) Math.round(r.nextInt(26) + 97);
                }else {
                    uuid[i] = (char) Math.round(r.nextInt(26) + 65);
                }
            }else {
                //如果第一次随机判断为false就生成数字
                uuid[i] = (char) Math.round(r.nextInt(10) + 48);
            }
        }
        return Arrays.toString(uuid).replaceAll("[\\[\\], ]", "");
    }

    public static String getPhone() {
        Random r = new Random();
        char[] uuid = new char[11];
        for (int i = 0; i < 11; i++) {
            if (i == 0){
                uuid[i] = (char) 49;
            }else if (i == 1){
                uuid[i] = (char) 51;
            }else {
                uuid[i] = (char) Math.round(r.nextInt(10) + 48);
            }
        }
        return Arrays.toString(uuid).replaceAll("[\\[\\], ]", "");
    }

    public static void main(String[] args) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String base64Key = Encoders.BASE64.encode(key.getEncoded());
        System.out.println(base64Key);
    }
}
