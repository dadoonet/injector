package com.swiftype.appsearch;

import com.google.gson.Gson;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

class Jwt {
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  static String sign(String key, Map<String, Object> payload) throws InvalidKeyException {
    Map<String, Object> headers = new HashMap<>();
    headers.put("typ", "JWT");
    headers.put("alg", "HS256");

    Gson gson = new Gson();
    return sign(key, gson.toJson(headers), gson.toJson(payload));
  }

  static String sign(String key, String headerJson, String payloadJson) throws InvalidKeyException {
    Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();
    String headerAndPayload = String.format(
      "%s.%s",
      base64Encoder.encodeToString(headerJson.getBytes(UTF_8)),
      base64Encoder.encodeToString(payloadJson.getBytes(UTF_8))
     );
    String signature = base64Encoder.encodeToString(hmac256(key, headerAndPayload));
    return String.format("%s.%s", headerAndPayload, signature);
  }

  static Map<String, Object> verify(String key, String jwtToken) throws SignatureException, InvalidKeyException {
    Base64.Decoder base64Decoder = Base64.getUrlDecoder();

    String[] parts = jwtToken.split("\\.");

    String headerBase64Json = parts[0];
    String payloadBase64Json = parts[1];
    String signatureBase64 = parts[2];

    byte[] signature = base64Decoder.decode(signatureBase64);

    ensureSignatureMatches(key, String.format("%s.%s", headerBase64Json, payloadBase64Json), signature);

    String payloadJson = new String(base64Decoder.decode(payloadBase64Json), UTF_8);
    return new Gson().fromJson(payloadJson, JsonTypes.OBJECT.getType());
  }

  private static void ensureSignatureMatches(String key, String input, byte[] signature) throws SignatureException, InvalidKeyException {
    byte[] generatedSignature = hmac256(key, input);
    if (!MessageDigest.isEqual(generatedSignature, signature)) {
      throw new SignatureException("Signature does not match");
    }
  }

  private static byte[] hmac256(String key, String input) throws InvalidKeyException {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(key.getBytes(UTF_8), HMAC_ALGORITHM));
      return mac.doFinal(input.getBytes(UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("HmacSHA256 is unsupported by your Java platform. This should never happen.", e);
    }
  }
}
