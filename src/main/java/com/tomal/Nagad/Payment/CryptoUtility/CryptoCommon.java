package com.tomal.Nagad.Payment.CryptoUtility;

import com.tomal.Nagad.Payment.exception.AsymmetricEncryptionFailure;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class CryptoCommon {
    private final String privateKey = "src/main/java/com/tomal/Nagad/Payment/CryptoUtility/marchent_private.pem";
    private final String publicKey = "src/main/java/com/tomal/Nagad/Payment/CryptoUtility/marchent_public.pem";

    public byte[] sign(PrivateKey merchantPrivateKey, byte[] bytes) {
        Object var4 = null;
        

        try {
            Signature instance = Signature.getInstance("SHA256withRSA");
            instance.initSign(merchantPrivateKey);
            instance.update(bytes);
            byte[] sign = instance.sign();
            return sign;
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException var6) {
            throw new AsymmetricEncryptionFailure(var6);
        }
    }

    public byte[] encrypt(PublicKey pgPublicKey, byte[] rawData) throws Throwable {
        Cipher cipher = null;
        Object var4 = null;

        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(1, pgPublicKey);
            byte[] result = cipher.doFinal(rawData);
            return result;
        } catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException var6) {
            throw new Throwable();
        }
    }

    public String generateRandomString(int size, byte[] seed) {
        SecureRandom secureRandom = new SecureRandom(seed);
        byte[] secure = new byte[size];
        secureRandom.nextBytes(secure);
        return DatatypeConverter.printHexBinary(secure);
    }

    public PublicKey getPublic() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(publicKey));
        String content = reader.readLine();
        System.out.println(content);

        content = content.replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(content);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public PrivateKey getPrivate() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(privateKey));
        String content = reader.readLine();
        System.out.println(content);

        content = content.replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public byte[] decrypt(PrivateKey aPrivate, byte[] decode) {

        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(2, aPrivate);
            return cipher.doFinal(decode);
        } catch (NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException var6) {
            throw new AsymmetricEncryptionFailure(var6);
        }
    }

    public Boolean verifySign(PublicKey aPublic, byte[] rawDecryptedBytes, byte[] decode) {


        try {
            Signature instance = Signature.getInstance("SHA256withRSA");
            instance.initVerify(aPublic);
            instance.update(rawDecryptedBytes);
            return instance.verify(decode);
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException var7) {
            throw new AsymmetricEncryptionFailure(var7);
        }
    }
}
