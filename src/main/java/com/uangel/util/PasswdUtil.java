package com.uangel.util;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswdUtil {
    StandardPBEStringEncryptor crypto = new StandardPBEStringEncryptor();

    private PasswdUtil(String key, String alg) {
        EnvironmentPBEConfig config = new EnvironmentPBEConfig();
        config.setPassword(key);
        config.setAlgorithm(alg);
        this.crypto.setConfig(config);
    }

    public static String decriptString(String str) {
        return (new PasswdUtil("skt_acs", "PBEWITHMD5ANDDES")).decrypt(str);
    }

    public static String encriptString(String str) {
        return (new PasswdUtil("skt_acs", "PBEWITHMD5ANDDES")).encrypt(str);
    }

    private String decrypt0(String encrypted) {
        return this.crypto.decrypt(encrypted);
    }

    private String encrypt0(String decrypted) {
        return this.crypto.encrypt(decrypted);
    }

    private String decrypt(String f) {
        Pattern p = Pattern.compile("(ENC\\((.+?)\\))");
        Matcher m = p.matcher(f);

        String g;
        String enc;
        String pass;
        for(g = f; m.find(); g = StringUtils.replace(g, enc, pass)) {
            enc = m.group(1);
            String encryptedPass = m.group(2);
            pass = this.decrypt0(encryptedPass);
        }

        return g;
    }

    private String encrypt(String f) {
        Pattern p = Pattern.compile("(DEC\\((.+?)\\))");
        Matcher m = p.matcher(f);

        String g;
        String dec;
        String pass;
        for(g = f; m.find(); g = StringUtils.replace(g, dec, pass)) {
            dec = m.group(1);
            String decryptedPass = m.group(2);
            pass = this.encrypt0(decryptedPass);
        }

        return g;
    }
}
