
package com.aurora.note.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    public static void closeQuietly(InputStream input) {
        closeQuietly((Closeable) input);
    }

    public static byte[] toByteArray(InputStream input, int size)
            throws IOException {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
        }

        if (size == 0) {
            return new byte[size];
        }

        byte[] data = new byte[size];
        int offset = 0;
        while (offset < size) {
            int readed = input.read(data, offset, size - offset);
            if (readed == -1) {
                break;
            }
            offset = offset + readed;
        }
        if (offset == size) {
            return data;
        }

        throw new IOException("Unexpected readed size. current: " + offset + ", excepted: " + size);
    }

    public static byte[] toByteArray(InputStream input, long size)
            throws IOException {
        if (size > 0x7fffffff) {
            throw new IllegalArgumentException("Size cannot be greater than Integer max value: "
                    + size);
        }

        return toByteArray(input, (int) size);
    }
}
