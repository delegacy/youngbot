package com.github.delegacy.youngbot.server.line;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

import javax.annotation.Nonnull;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class LineSignatureValidator {
    private static final String HASH_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKeySpec;

    LineSignatureValidator(@Value("${youngbot.line.channel-secret}") String channelSecret) {
        secretKeySpec = new SecretKeySpec(channelSecret.getBytes(StandardCharsets.US_ASCII), HASH_ALGORITHM);
    }

    boolean validateSignature(@Nonnull byte[] content, @Nonnull String headerSignature) {
        final byte[] signature = generateSignature(content);
        final byte[] decodeHeaderSignature = Base64.getDecoder().decode(headerSignature);
        return MessageDigest.isEqual(decodeHeaderSignature, signature);
    }

    byte[] generateSignature(@Nonnull byte[] content) {
        try {
            final Mac mac = Mac.getInstance(HASH_ALGORITHM);
            mac.init(secretKeySpec);
            return mac.doFinal(content);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
