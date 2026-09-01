package com.yu.blog.config;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClientIpResolver {
    private final TrustedProxyProperties properties;

    public ClientIpResolver(TrustedProxyProperties properties) {
        this.properties = properties;
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddress = normalize(request.getRemoteAddr());
        if (!isTrustedProxy(remoteAddress)) {
            return remoteAddress;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            String first = forwardedFor.split(",", 2)[0].trim();
            if (StringUtils.hasText(first)) {
                return normalize(first);
            }
        }
        return normalize(request.getHeader("X-Real-IP"), remoteAddress);
    }

    private boolean isTrustedProxy(String address) {
        if (!StringUtils.hasText(address)) {
            return false;
        }
        try {
            InetAddress candidate = InetAddress.getByName(address);
            return properties.getCidrs().stream().anyMatch(cidr -> matches(candidate, cidr));
        } catch (UnknownHostException exception) {
            return false;
        }
    }

    private boolean matches(InetAddress candidate, String cidr) {
        String[] parts = cidr.split("/", 2);
        try {
            InetAddress network = InetAddress.getByName(parts[0]);
            if (candidate.getAddress().length != network.getAddress().length) {
                return false;
            }
            int prefixLength = parts.length == 1
                    ? network.getAddress().length * 8
                    : Integer.parseInt(parts[1]);
            if (prefixLength < 0 || prefixLength > network.getAddress().length * 8) {
                return false;
            }
            byte[] candidateBytes = candidate.getAddress();
            byte[] networkBytes = network.getAddress();
            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;
            for (int index = 0; index < fullBytes; index++) {
                if (candidateBytes[index] != networkBytes[index]) {
                    return false;
                }
            }
            if (remainingBits == 0) {
                return true;
            }
            int mask = 0xFF << (8 - remainingBits);
            return (candidateBytes[fullBytes] & mask) == (networkBytes[fullBytes] & mask);
        } catch (UnknownHostException | NumberFormatException exception) {
            return false;
        }
    }

    private String normalize(String value) {
        return normalize(value, "unknown");
    }

    private String normalize(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        return value.trim();
    }
}
