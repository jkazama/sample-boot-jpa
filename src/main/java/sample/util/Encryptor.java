package sample.util;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * Represents an encryption utility.
 */
public class Encryptor {
    private final Cipher encryptor;
    private final Cipher decryptor;
    private final String encoding;

    public Encryptor(Cipher encryptor, Cipher decryptor, String encoding) {
        this.encryptor = encryptor;
        this.decryptor = decryptor;
        this.encoding = encoding;
    }

    /** Perform encryption */
    public String encrypt(String raw) {
        try {
            return Base64.getEncoder().encodeToString(encryptor.doFinal(raw.getBytes(encoding)));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** Perform decryption */
    public String decrypt(String encrypted) {
        try {
            byte[] v = Base64.getDecoder().decode(encrypted);
            return new String(decryptor.doFinal(v), encoding);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** Automatic private key generation */
    public static String generateSecret(String transformation) {
        try {
            int size = Cipher.getInstance(transformation).getBlockSize();
            var random = new SecureRandom();
            return Base64.getEncoder().encodeToString(random.generateSeed(size));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** Automatic generation of initialization vectors */
    public static String generateIvs(String transformation, String algorithm, String secret) {
        try {
            var cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(Base64.getDecoder().decode(secret), algorithm));
            return Base64.getEncoder().encodeToString(cipher.getIV());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** Generates a random string. */
    public static String generateRandom(int count) {
        return RandomStringUtils.randomAlphabetic(count);
    }

    /** Generates a random string. */
    public static String generateRandom(int count, String chars) {
        return RandomStringUtils.random(count, chars);
    }

    /** Encryption Utility Generation Builder */
    public static class EncryptorBuilder {
        public static final String DefaultAlgorithm = "AES";
        public static final String DefaultTransformation = "AES/CBC/PKCS5PADDING";
        /** Base64 encoded private key (algorithm compliant) */
        private final String secret;
        /** Base64 encoded initialization vector */
        private final String ivs;
        private String algorithm = DefaultAlgorithm;
        private String transformation = DefaultTransformation;
        private String encoding = "UTF-8";

        public EncryptorBuilder(String secret, String ivs) {
            this.secret = secret;
            this.ivs = ivs;
        }

        private Cipher encryptor(SecretKeySpec keySpec, IvParameterSpec iv) throws Exception {
            var encryptor = Cipher.getInstance(transformation);
            encryptor.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            return encryptor;
        }

        private Cipher decryptor(SecretKeySpec keySpec, IvParameterSpec iv) throws Exception {
            var decryptor = Cipher.getInstance(transformation);
            decryptor.init(Cipher.DECRYPT_MODE, keySpec, iv);
            return decryptor;
        }

        public EncryptorBuilder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public EncryptorBuilder transformation(String transformation) {
            this.transformation = transformation;
            return this;
        }

        public EncryptorBuilder encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public Encryptor build() {
            try {
                var iv = new IvParameterSpec(Base64.getDecoder().decode(ivs));
                var keySpec = new SecretKeySpec(Base64.getDecoder().decode(secret), algorithm);
                return new Encryptor(encryptor(keySpec, iv), decryptor(keySpec, iv), this.encoding);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        public static EncryptorBuilder of(String secret, String ivs) {
            return new EncryptorBuilder(secret, ivs);
        }

    }

}
