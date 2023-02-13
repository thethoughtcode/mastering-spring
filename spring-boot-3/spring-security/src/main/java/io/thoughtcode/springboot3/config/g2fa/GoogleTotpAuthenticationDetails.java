package io.thoughtcode.springboot3.config.g2fa;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

public class GoogleTotpAuthenticationDetails extends WebAuthenticationDetails {

    private static final long serialVersionUID = 5310939649597466738L;

    private final String verificationCode;

    public GoogleTotpAuthenticationDetails(final HttpServletRequest request) {
        super(request);
        verificationCode = request.getParameter("code");
    }

    public String getVerificationCode() {
        return verificationCode;
    }
}
