package io.thoughtcode.springboot3.config.auth;

import java.io.IOException;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import io.thoughtcode.springboot3.service.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final MessageSource messages;

    private final LocaleResolver localeResolver;

    private final LoginAttemptService loginAttemptService;

    private final HttpRequestHelper requestHelper;

    // @formatter:off
    CustomAuthenticationFailureHandler(final MessageSource messages,
                                       final LocaleResolver localeResolver,
                                       final LoginAttemptService loginAttemptService,
                                       final HttpRequestHelper requestHelper) {
        this.messages = messages;
        this.localeResolver = localeResolver;
        this.loginAttemptService = loginAttemptService;
        this.requestHelper = requestHelper;
    }
    // @formatter:on

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception)
            throws IOException, ServletException {

        setDefaultFailureUrl("/login?error=true");

        super.onAuthenticationFailure(request, response, exception);

        final Locale locale = localeResolver.resolveLocale(request);
        final String ip = requestHelper.getClientIP(request);

        String errorMessage = messages.getMessage("message.badCredentials", null, locale);

        if (loginAttemptService.isBlocked(ip)) {
            errorMessage = messages.getMessage("auth.message.blocked", null, locale);
        }

        if (exception.getMessage().equalsIgnoreCase("User is disabled")) {
            errorMessage = messages.getMessage("auth.message.disabled", null, locale);
        } else if (exception.getMessage().equalsIgnoreCase("User account has expired")) {
            errorMessage = messages.getMessage("auth.message.expired", null, locale);
        } else if (exception.getMessage().equalsIgnoreCase("blocked")) {
            errorMessage = messages.getMessage("auth.message.blocked", null, locale);
        } else if (exception.getMessage().equalsIgnoreCase("unusual location")) {
            errorMessage = messages.getMessage("auth.message.unusual.location", null, locale);
        }

        request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, errorMessage);
    }
}
