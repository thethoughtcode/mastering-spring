package io.thoughtcode.springboot3.config.auth;

import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import io.thoughtcode.springboot3.config.dto.ActiveUserStore;
import io.thoughtcode.springboot3.config.dto.LoggedUser;
import io.thoughtcode.springboot3.entity.User;
import io.thoughtcode.springboot3.service.DeviceMetadataService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Configuration
public class UserLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UserLoginSuccessHandler.class);

    private static final int SESSION_TIMEOUT = 30 * 60; // 30 minutes

    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 30; // 30 days

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private final ActiveUserStore activeUserStore;

    private final DeviceMetadataService deviceMetadataService;

    public UserLoginSuccessHandler(final ActiveUserStore activeUserStore, final DeviceMetadataService deviceMetadataService) {
        this.activeUserStore = activeUserStore;
        this.deviceMetadataService = deviceMetadataService;
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication)
            throws IOException, ServletException {

        handle(request, response, authentication);

        addWelcomeCookie(getUserName(authentication), response);

        final HttpSession session = request.getSession(false);

        String username = "";

        if (session != null) {

            session.setMaxInactiveInterval(SESSION_TIMEOUT);

            if (authentication.getPrincipal() instanceof User) {
                username = ((User) authentication.getPrincipal()).getEmail();
            } else {
                username = authentication.getName();
            }

            final LoggedUser user = new LoggedUser(username, activeUserStore);

            session.setAttribute("user", user);
        }

        clearAuthenticationAttributes(request);

        loginNotification(authentication, request);
    }

    protected void handle(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication)
            throws IOException {

        final String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            LOG.error("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(final Authentication authentication) {

        boolean isUser = false;
        boolean isAdmin = false;

        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (final GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals("READ_PRIVILEGE")) {
                isUser = true;
            } else if (grantedAuthority.getAuthority().equals("WRITE_PRIVILEGE")) {
                isAdmin = true;
                isUser = false;
                break;
            }
        }

        if (isUser) {
            final String username;
            if (authentication.getPrincipal() instanceof User) {
                username = getUserName(authentication);
            } else {
                username = authentication.getName();
            }

            return "/homepage.html?user=" + username;

        } else if (isAdmin) {
            return "/console";
        } else {
            throw new IllegalStateException();
        }
    }

    private void loginNotification(Authentication authentication, HttpServletRequest request) {
        try {
            if (authentication.getPrincipal() instanceof User && deviceMetadataService.isGeoIpLibEnabled()) {
                deviceMetadataService.verifyDevice(((User) authentication.getPrincipal()), request);
            }
        } catch (final Exception e) {
            LOG.error("An error occurred while verifying device or location", e);
            throw new RuntimeException(e);
        }

    }

    private String getUserName(final Authentication authentication) {
        return ((User) authentication.getPrincipal()).getEmail();
    }

    private void addWelcomeCookie(final String user, final HttpServletResponse response) {
        final Cookie welcomeCookie = getWelcomeCookie(user);
        response.addCookie(welcomeCookie);
    }

    private Cookie getWelcomeCookie(final String user) {
        final Cookie welcomeCookie = new Cookie("welcome", user);
        welcomeCookie.setMaxAge(COOKIE_MAX_AGE);
        return welcomeCookie;
    }

    private void clearAuthenticationAttributes(final HttpServletRequest request) {

        final HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}
