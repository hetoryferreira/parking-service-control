package com.estapar.parking.application.support;

import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class MdcUtils {
    private MdcUtils() {}

    public static <T> T withMdc(Map<String, String> ctx, Supplier<T> action) {
        Map<String, String> old = putAll(ctx);
        try {
            return action.get();
        } finally {
            restore(old, ctx.keySet());
        }
    }

    private static Map<String, String> putAll(Map<String, String> ctx) {
        Map<String, String> old = new HashMap<>();
        if (ctx != null) {
            ctx.forEach((k, v) -> {
                old.put(k, MDC.get(k));
                if (v != null) MDC.put(k, v); else MDC.remove(k);
            });
        }
        return old;
    }

    private static void restore(Map<String, String> old, Set<String> keys) {
        for (String k : keys) {
            String prev = old.get(k);
            if (prev == null) MDC.remove(k); else MDC.put(k, prev);
        }
    }
}
