package com.example.gateway_service.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class InputSanitizer {

    private static final Pattern DANGEROUS_PATTERN =
            Pattern.compile("(<script>|</script>|'\\s*or\\s*'1'='1|--|;)", Pattern.CASE_INSENSITIVE);

    public boolean isSafe(Map<String, Object> body) {
        if (body == null) {
            return true;
        }

        for (Object value : body.values()) {
            if (value instanceof String str) {
                if (DANGEROUS_PATTERN.matcher(str).find()) {
                    return false;
                }
            }
        }
        return true;
    }
}