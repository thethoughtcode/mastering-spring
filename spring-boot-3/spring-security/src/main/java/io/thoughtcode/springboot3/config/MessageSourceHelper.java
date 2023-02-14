package io.thoughtcode.springboot3.config;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageSourceHelper {

    private final MessageSource messages;

    MessageSourceHelper(final MessageSource messages) {
        this.messages = messages;
    }

    public String getMessage(final String messageKey, final Locale locale) {
        try {
            return messages.getMessage(messageKey, null, locale);
        } catch (final NoSuchMessageException ex) {
            return messages.getMessage(messageKey, null, Locale.ENGLISH);
        }
    }
}
