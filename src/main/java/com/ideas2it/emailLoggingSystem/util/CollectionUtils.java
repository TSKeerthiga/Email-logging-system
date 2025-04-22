package com.ideas2it.emailLoggingSystem.util;

import java.util.*;
import java.util.stream.IntStream;

public class CollectionUtils {

    public static <T> List<List<T>> chunkList(List<T> list, int chunkSize) {
        if (chunkSize <= 0) throw new IllegalArgumentException("Chunk size must be > 0");

        return IntStream.range(0, (list.size() + chunkSize - 1) / chunkSize)
                .mapToObj(i -> list.subList(i * chunkSize, Math.min((i + 1) * chunkSize, list.size())))
                .toList();
    }
}
