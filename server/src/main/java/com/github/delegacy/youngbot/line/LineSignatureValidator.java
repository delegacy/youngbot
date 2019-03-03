package com.github.delegacy.youngbot.line;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

import javax.annotation.Nonnull;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LineSignatureValidator {
    private static final String HASH_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKeySpec;

    @Inject
    public LineSignatureValidator(@Value("${youngbot.line.channel-secret}") String channelSecret) {
        secretKeySpec = new SecretKeySpec(channelSecret.getBytes(StandardCharsets.US_ASCII), HASH_ALGORITHM);
    }

    public boolean validateSignature(@Nonnull byte[] content, @Nonnull String headerSignature) {
        final byte[] signature = generateSignature(content);
        final byte[] decodeHeaderSignature = Base64.getDecoder().decode(headerSignature);
        return MessageDigest.isEqual(decodeHeaderSignature, signature);
    }

    public byte[] generateSignature(@Nonnull byte[] content) {
        try {
            final Mac mac = Mac.getInstance(HASH_ALGORITHM);
            mac.init(secretKeySpec);
            return mac.doFinal(content);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
