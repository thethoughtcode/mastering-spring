package io.thoughtcode.springboot3.events;

import java.util.UUID;

import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import io.thoughtcode.springboot3.entity.User;
import io.thoughtcode.springboot3.service.UserService;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    static final String SUBJECT = "Registration Confirmation";

    static final String FROM_EMAIL = "no-reply@test.com";

    private final UserService service;

    private final MessageSource messages;

    private final JavaMailSender mailSender;

    RegistrationListener(final UserService service, final MessageSource messages, final JavaMailSender mailSender) {
        this.service = service;
        this.messages = messages;
        this.mailSender = mailSender;
    }

    @Override
    public void onApplicationEvent(final OnRegistrationCompleteEvent event) {
        confirmRegistration(event);
    }

    private void confirmRegistration(final OnRegistrationCompleteEvent event) {

        final User user = event.getUser();

        final String token = UUID.randomUUID().toString();

        service.createVerificationTokenForUser(user, token);

        final SimpleMailMessage email = constructEmailMessage(event, user, token);

        mailSender.send(email);
    }

    private SimpleMailMessage constructEmailMessage(final OnRegistrationCompleteEvent event, final User user, final String token) {

        final String recipientAddress = user.getEmail();
        final String confirmationUrl = event.getAppUrl() + "/registrationConfirm?token=" + token;
        final String message = messages.getMessage("message.regSuccLink", null, event.getLocale()) + "\n\n" + confirmationUrl;

        final SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(SUBJECT);
        email.setText(message);
        email.setFrom(FROM_EMAIL);

        return email;
    }
}
