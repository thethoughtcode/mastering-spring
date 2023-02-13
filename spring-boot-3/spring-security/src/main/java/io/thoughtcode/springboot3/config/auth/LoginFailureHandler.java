package io.thoughtcode.springboot3.config.auth;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import io.thoughtcode.springboot3.config.dto.ActiveUserStore;
import io.thoughtcode.springboot3.config.dto.LoggedUser;
import io.thoughtcode.springboot3.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Configuration
public class LoginFailureHandler implements AuthenticationSuccessHandler {

    private static final int SESSION_TIMEOUT = 30 * 60; // 30 minutes
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 30; // 30 days

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private final ActiveUserStore activeUserStore;

    public LoginFailureHandler(final ActiveUserStore activeUserStore) {
        this.activeUserStore = activeUserStore;
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication)
            throws IOException, ServletException {

        addWelcomeCookie(gerUserName(authentication), response);

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

        redirectStrategy.sendRedirect(request, response, "/homepage.html?user=" + username);

        clearAuthenticationAttributes(request);
    }

    private String gerUserName(final Authentication authentication) {
        return ((User) authentication.getPrincipal()).getFirstName();
    }

    private void addWelcomeCookie(final String user, final HttpServletResponse response) {
        final Cookie welcomeCookie = getWelcomeCookie(user);
        response.addCookie(welcomeCookie);
    }

    private Cookie getWelcomeCookie(final String user) {
        Cookie welcomeCookie = new Cookie("welcome", user);
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
