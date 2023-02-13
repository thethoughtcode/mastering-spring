package io.thoughtcode.springboot3.config.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.stereotype.Component;

import io.thoughtcode.springboot3.entity.NewLocationToken;
import io.thoughtcode.springboot3.events.OnDifferentLocationLoginEvent;
import io.thoughtcode.springboot3.service.UserService;
import io.thoughtcode.springboot3.web.error.UnusualLocationException;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class DifferentLocationChecker implements UserDetailsChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DifferentLocationChecker.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ApplicationEventPublisher eventBus;

    @Autowired
    private HttpRequestHelper requestHelper;

    @Override
    public void check(final UserDetails userDetails) {

        final String ip = requestHelper.getClientIP(request);

        LOG.info("Request Object -> {} | Ip - {}", request, ip);

        final NewLocationToken token = userService.isNewLoginLocation(userDetails.getUsername(), ip);

        if (token != null) {

            final String appUrl = requestHelper.getAppUrl(request);

            eventBus.publishEvent(new OnDifferentLocationLoginEvent(request.getLocale(), userDetails.getUsername(), ip, token, appUrl));

            throw new UnusualLocationException("unusual location");
        }
    }
}
