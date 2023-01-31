package com.uangel.rmq.util;

import com.uangel.service.ServiceDefine;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswdDecryptor {
    StandardPBEStringEncryptor crypto;

    public PasswdDecryptor(String key,
                           String alg) {
        crypto = new StandardPBEStringEncryptor();
        EnvironmentPBEConfig config = new EnvironmentPBEConfig();
        config.setPassword(key);
        config.setAlgorithm(alg);
        crypto.setConfig(config);
    }

    public static void main(String[] args) throws Exception {
        PasswdDecryptor decryptor = new PasswdDecryptor(ServiceDefine.U_RMQ.getStr(), ServiceDefine.PW_ALG.getStr());
        String tt = decryptor.encrypt("acs.123");
        System.out.println(tt);
        System.out.println(decryptor.decrypt0(tt));
        tt = decryptor.encrypt("mrfp.123");
        System.out.println(tt);
        System.out.println(decryptor.decrypt0(tt));
    }

    public String decrypt0(String encrypted) {
        return crypto.decrypt(encrypted);
    }

    public String encrypt(String pass) {
        return crypto.encrypt(pass);
    }

    public String decrypt(String f) {
        Pattern p = Pattern.compile("(ENC\\((.+?)\\))");
        Matcher m = p.matcher(f);
        String g = new String(f);
        while (m.find()) {
            String enc_ = m.group(1);
            String encryptedPass = m.group(2);
//            System.out.println(enc_);
//            System.out.println(encryptedPass);
            String pass = decrypt0(encryptedPass);
            g = StringUtils.replace(g, enc_, pass);
        }

        return g;
    }
}
