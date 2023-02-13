package io.thoughtcode.springboot3.events;

import java.util.Locale;

import org.springframework.context.ApplicationEvent;

import io.thoughtcode.springboot3.entity.User;
import lombok.Getter;

@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private static final long serialVersionUID = -4408262189534033581L;

    private final String appUrl;

    private final Locale locale;

    private final User user;

    public OnRegistrationCompleteEvent(final User user, final Locale locale, final String appUrl) {
        super(user);
        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
    }
}
