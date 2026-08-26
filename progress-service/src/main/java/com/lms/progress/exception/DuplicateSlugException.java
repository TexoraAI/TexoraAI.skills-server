package com.lms.progress.exception;

/**
 * Thrown as a safety net when a save fails a unique-slug database constraint
 * despite the server-side dedup loop (e.g. a race between two concurrent creates
 * for the same title). Expected to be rare since slugs are generated and
 * deduplicated server-side before every insert. Part 2B maps this to
 * 409 CONFLICT.
 */
public class DuplicateSlugException extends RuntimeException {

    public DuplicateSlugException(String message) {
        super(message);
    }

    public DuplicateSlugException(String message, Throwable cause) {
        super(message, cause);
    }
}