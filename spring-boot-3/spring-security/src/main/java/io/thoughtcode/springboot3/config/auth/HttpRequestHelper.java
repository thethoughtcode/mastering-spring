package io.thoughtcode.springboot3.config.auth;

import static io.thoughtcode.springboot3.service.DeviceMetadataService.UNKNOWN;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import ua_parser.Client;
import ua_parser.Parser;

@Component
public class HttpRequestHelper {

    private final Parser userAgentParser;

    HttpRequestHelper(final Parser userAgentParser) {
        this.userAgentParser = userAgentParser;
    }

    public String getClientIP(final HttpServletRequest request) {

        final String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0];
    }

    public String getAppUrl(final HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    public String getClientDetails(final HttpServletRequest request) {

        final String deviceDetails;

        final Client client = userAgentParser.parse(request.getHeader("user-agent"));

        if (nonNull(client)) {
            deviceDetails = format("%s %s.%s - %s %s.%s", client.userAgent.family, client.userAgent.major, client.userAgent.minor, client.os.family,
                    client.os.major, client.os.minor);
        } else {
            deviceDetails = UNKNOWN;
        }

        return deviceDetails;
    }
}
