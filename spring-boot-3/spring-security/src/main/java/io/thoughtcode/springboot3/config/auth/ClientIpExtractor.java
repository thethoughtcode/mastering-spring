package io.thoughtcode.springboot3.config.auth;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ClientIpExtractor {

    public String getClientIP(final HttpServletRequest request) {

        final String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0];
    }
}
