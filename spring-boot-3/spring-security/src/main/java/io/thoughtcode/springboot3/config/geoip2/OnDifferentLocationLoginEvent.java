package io.thoughtcode.springboot3.config.geoip2;

import java.util.Locale;

import org.springframework.context.ApplicationEvent;

import io.thoughtcode.springboot3.entity.NewLocationToken;
import lombok.Getter;

@Getter
public class OnDifferentLocationLoginEvent extends ApplicationEvent {

    private static final long serialVersionUID = -2366944270319063979L;

    private final Locale locale;

    private final String username;

    private final String ip;

    private final NewLocationToken token;

    private final String appUrl;

    // @formatter:off
    public OnDifferentLocationLoginEvent(final Locale locale,
                                         final String username,
                                         final String ip,
                                         final NewLocationToken token,
                                         final String appUrl) {
        super(token);
        this.locale = locale;
        this.username = username;
        this.ip = ip;
        this.token = token;
        this.appUrl = appUrl;
    }
    // @formatter:on
}
