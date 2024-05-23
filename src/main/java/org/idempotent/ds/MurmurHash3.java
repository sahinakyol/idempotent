package org.idempotent.ds;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for computing MurmurHash3 hashes.
 */
public final class MurmurHash3 {

    /**
     * Computes a 128-bit MurmurHash3 hash for the given input string.
     *
     * @param input the input string.
     * @return the computed hash as a hexadecimal string.
     */
    public static String hash128(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        int length = data.length;
        final int seed = 0;
        final int c1 = 0x239b961b;
        final int c2 = 0xab0e9789;
        final int c3 = 0x38b34ae5;
        final int c4 = 0xa1e38b93;
        final int r1 = 15;
        final int r2 = 19;
        final int m = 5;
        final int n = 0x561ccd1b;

        int h1 = seed;
        int h2 = seed;
        int h3 = seed;
        int h4 = seed;

        ByteBuffer buffer = ByteBuffer.wrap(data);

        while (buffer.remaining() >= 16) {
            int k1 = buffer.getInt();
            int k2 = buffer.getInt();
            int k3 = buffer.getInt();
            int k4 = buffer.getInt();

            k1 *= c1;
            k1 = Integer.rotateLeft(k1, r1);
            k1 *= c2;
            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, r2);
            h1 += h2;
            h1 = h1 * m + n;

            k2 *= c2;
            k2 = Integer.rotateLeft(k2, r1);
            k2 *= c3;
            h2 ^= k2;
            h2 = Integer.rotateLeft(h2, r2);
            h2 += h3;
            h2 = h2 * m + n;

            k3 *= c3;
            k3 = Integer.rotateLeft(k3, r1);
            k3 *= c4;
            h3 ^= k3;
            h3 = Integer.rotateLeft(h3, r2);
            h3 += h4;
            h3 = h3 * m + n;

            k4 *= c4;
            k4 = Integer.rotateLeft(k4, r1);
            k4 *= c1;
            h4 ^= k4;
            h4 = Integer.rotateLeft(h4, r2);
            h4 += h1;
            h4 = h4 * m + n;
        }

        int k1 = 0;
        int k2 = 0;
        int k3 = 0;
        int k4 = 0;

        switch (buffer.remaining()) {
            case 15:
                k4 ^= (buffer.get(14) & 0xff) << 16;
            case 14:
                k4 ^= (buffer.get(13) & 0xff) << 8;
            case 13:
                k4 ^= (buffer.get(12) & 0xff);
                k4 *= c4;
                k4 = Integer.rotateLeft(k4, r1);
                k4 *= c1;
                h4 ^= k4;
            case 12:
                k3 ^= (buffer.get(11) & 0xff) << 24;
            case 11:
                k3 ^= (buffer.get(10) & 0xff) << 16;
            case 10:
                k3 ^= (buffer.get(9) & 0xff) << 8;
            case 9:
                k3 ^= (buffer.get(8) & 0xff);
                k3 *= c3;
                k3 = Integer.rotateLeft(k3, r1);
                k3 *= c4;
                h3 ^= k3;
            case 8:
                k2 ^= (buffer.get(7) & 0xff) << 24;
            case 7:
                k2 ^= (buffer.get(6) & 0xff) << 16;
            case 6:
                k2 ^= (buffer.get(5) & 0xff) << 8;
            case 5:
                k2 ^= (buffer.get(4) & 0xff);
                k2 *= c2;
                k2 = Integer.rotateLeft(k2, r1);
                k2 *= c3;
                h2 ^= k2;
            case 4:
                k1 ^= (buffer.get(3) & 0xff) << 24;
            case 3:
                k1 ^= (buffer.get(2) & 0xff) << 16;
            case 2:
                k1 ^= (buffer.get(1) & 0xff) << 8;
            case 1:
                k1 ^= (buffer.get(0) & 0xff);
                k1 *= c1;
                k1 = Integer.rotateLeft(k1, r1);
                k1 *= c2;
                h1 ^= k1;
        }

        h1 ^= length;
        h2 ^= length;
        h3 ^= length;
        h4 ^= length;

        h1 += h2;
        h1 += h3;
        h1 += h4;
        h2 += h1;
        h3 += h1;
        h4 += h1;

        h1 = fmix(h1);
        h2 = fmix(h2);
        h3 = fmix(h3);
        h4 = fmix(h4);

        h1 += h2;
        h1 += h3;
        h1 += h4;
        h2 += h1;
        h3 += h1;
        h4 += h1;

        return Long.toHexString(h1) + Long.toHexString(h2) + Long.toHexString(h3) + Long.toHexString(h4);
    }
    /**
     * Finalization mix function for MurmurHash3.
     *
     * @param h the hash value.
     * @return the mixed hash value.
     */
    private static int fmix(int h) {
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }
}
