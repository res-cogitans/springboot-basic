package org.programmers.springbootbasic.voucher.converter;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.util.UUID;

@UtilityClass
public final class UuidByteArrayConverter {

    public static byte[] uuidToBytes(UUID id) {
        String uuidToHexString = id.toString().replace("-", "");
        return hexStringToByteArray(uuidToHexString);
    }

    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] converted = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            converted[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return converted;
    }

    public static UUID bytesToUuid(byte[] bytes) {
        var byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }
}