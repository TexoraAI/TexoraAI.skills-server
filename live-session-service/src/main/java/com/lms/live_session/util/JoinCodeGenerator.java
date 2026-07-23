package com.lms.live_session.util;

import java.security.SecureRandom;

/**
 * Generates Google-Meet-style join codes, e.g. "zjo-fenz-cef".
 * Segments: 3-4-3 lowercase letters. Never UUIDs, never numeric IDs.
 */
public final class JoinCodeGenerator {

    // Excludes 'l' and 'o' to avoid visual confusion — letters only, no digits.
    private static final String ALPHABET = "abcdefghijkmnpqrstuvwxyz";
    private static final int[] SEGMENT_LENGTHS = {3, 4, 3};
    private static final SecureRandom RANDOM = new SecureRandom();

    private JoinCodeGenerator() {}

    public static String generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SEGMENT_LENGTHS.length; i++) {
            if (i > 0) sb.append('-');
            for (int j = 0; j < SEGMENT_LENGTHS[i]; j++) {
                sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }
        }
        return sb.toString();
    }
}