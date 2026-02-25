package com.decoapps.wearotp.shared.data;

import java.lang.reflect.UndeclaredThrowableException;
import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;


/**
 * This is an example implementation of the OATH
 * TOTP algorithm.
 * Visit www.openauthentication.org for more information.
 *
 * @author Johan Rydell, PortWise, Inc.
 */

public class TOTP {

    private TOTP() {}

/**
 * This method uses the JCE to provide the crypto algorithm.
 * HMAC computes a Hashed Message Authentication Code with the
 * crypto hash algorithm as a parameter.
 *
 * @param crypto: the crypto algorithm (HmacSHA1, HmacSHA256,
 *                             HmacSHA512)
 * @param keyBytes: the bytes to use for the HMAC key
 * @param text: the message or text to be authenticated
 */
private static byte[] hmac_sha(String crypto, byte[] keyBytes,
                               byte[] text){
    try {
        Mac hmac;
        hmac = Mac.getInstance(crypto);
        SecretKeySpec macKey =
                new SecretKeySpec(keyBytes, "RAW");
        hmac.init(macKey);
        return hmac.doFinal(text);
    } catch (GeneralSecurityException gse) {
        throw new UndeclaredThrowableException(gse);
    }
}


    /**
     * This method converts a HEX string to Byte[]
     *
     * @param hex: the HEX string
     *
     * @return : a byte array
     */

    private static byte[] hexStr2Bytes(String hex){
        // Adding one byte to get the right conversion
        // Values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex,16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    private static final int[] DIGITS_POWER
            // 0 1  2   3    4     5      6       7        8          9
            = {1,10,100,1000,10000,100000,1000000,10000000,100000000, 1000000000 };

    /**
     * This method generates a TOTP value for the given
     * set of parameters.
     *
     * @param key: the shared secret, HEX encoded
     * @param time: a value that reflects a time
     * @param returnDigits: number of digits to return
     * @param crypto: the crypto function to use
     *
     * @return a numeric String in base 10 that includes 6 digits
     */

    public static String generateTOTP(String key,
                                      String time,
                                      String returnDigits,
                                      String crypto){
        int codeDigits = Integer.decode(returnDigits);
        StringBuilder result;

        // Using the counter
        // First 8 bytes are for the movingFactor
        // Compliant with base RFC 4226 (HOTP)
        StringBuilder timeBuilder = new StringBuilder(time);
        while (timeBuilder.length() < 16 )
            timeBuilder.insert(0, "0");
        time = timeBuilder.toString();

        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);
        byte[] k;
        // Start of modified code from original specification
        try {
            k = hexStr2Bytes(key);
        } catch (NumberFormatException e) {
            var b32 = com.decoapps.wearotp.shared.data.Base32ConverterKt.base32ToHex(key);
            k = hexStr2Bytes(b32);
        }
        // End of modified code from original specification
        byte[] hash = hmac_sha(crypto, k, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                ((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        result = new StringBuilder(Integer.toString(otp));
        while (result.length() < codeDigits) {
            result.insert(0, "0");
        }
        return result.toString();
    }

}

