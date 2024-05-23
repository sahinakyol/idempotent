package org.idempotent.ds;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 * Implementation of a Bloom filter for probabilistic membership testing.
 */
public class BloomFilter {
    private final BitSet bitSet;
    private final int bitSetSize;
    private final int numberOfHashFunctions;

    /**
     * Constructor for BloomFilter.
     *
     * @param bitSetSize the size of the bit set.
     * @param numberOfHashFunctions the number of hash functions to use.
     */
    public BloomFilter(int bitSetSize, int numberOfHashFunctions) {
        this.bitSetSize = bitSetSize;
        this.bitSet = new BitSet(bitSetSize);
        this.numberOfHashFunctions = numberOfHashFunctions;
    }

    /**
     * Adds an element to the Bloom filter.
     *
     * @param element the element to add.
     */
    public void add(String element) {
        byte[] bytes = element.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < numberOfHashFunctions; i++) {
            int hash = hash(bytes, i);
            bitSet.set(Math.abs(hash % bitSetSize), true);
        }
    }

    /**
     * Checks if the Bloom filter might contain the given element.
     *
     * @param element the element to check.
     * @return true if the element might be in the filter, false otherwise.
     */
    public boolean mightContain(String element) {
        byte[] bytes = element.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < numberOfHashFunctions; i++) {
            int hash = hash(bytes, i);
            if (!bitSet.get(Math.abs(hash % bitSetSize))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Computes a hash value for the given byte array and seed.
     *
     * @param bytes the byte array to hash.
     * @param seed the seed for the hash function.
     * @return the computed hash value.
     */
    private int hash(byte[] bytes, int seed) {
        return byteArrayToInt(bytes) + seed * 0x5bd1e995;
    }

    /**
     * Converts a byte array to an integer.
     *
     * @param bytes the byte array to convert.
     * @return the converted integer.
     */
    private int byteArrayToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value <<= 8;
            value |= (bytes[i] & 0xFF);
        }
        return value;
    }
}
