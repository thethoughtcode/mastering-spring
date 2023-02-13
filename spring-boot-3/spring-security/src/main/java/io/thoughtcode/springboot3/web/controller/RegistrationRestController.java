package io.thoughtcode.springboot3.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.thoughtcode.springboot3.config.auth.HttpRequestHelper;
import io.thoughtcode.springboot3.entity.User;
import io.thoughtcode.springboot3.events.OnRegistrationCompleteEvent;
import io.thoughtcode.springboot3.service.UserSecurityService;
import io.thoughtcode.springboot3.service.UserService;
import io.thoughtcode.springboot3.web.dto.UserDto;
import io.thoughtcode.springboot3.web.util.GenericResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
public class RegistrationRestController {

    private final Logger LOG = LoggerFactory.getLogger(RegistrationRestController.class);

    private final Environment env;

    private final MessageSource messages;

    private final ApplicationEventPublisher eventBus;

    private final UserService userService;

    private final UserSecurityService userSecurityService;

    private final JavaMailSender mailSender;

    private final HttpRequestHelper requestHelper;

    // @formatter:off
    RegistrationRestController(final Environment env,
                               final MessageSource messages,
                               final ApplicationEventPublisher eventBus,
                               final UserService userService,
                               final UserSecurityService userSecurityService,
                               final JavaMailSender mailSender,
                               final HttpRequestHelper requestHelper) {
        this.env = env;
        this.messages = messages;
        this.eventBus = eventBus;
        this.userService = userService;
        this.userSecurityService = userSecurityService;
        this.mailSender = mailSender;
        this.requestHelper = requestHelper;
    }
    // @formatter:on

    @PostMapping("/api/user/registration")
    public GenericResponse registerUserAccount(@Valid final UserDto dto, final HttpServletRequest request) {

        LOG.debug("Registering user account with information: {}", dto);

        final User registered = userService.registerNewUserAccount(dto);

        userService.addUserLocation(registered, requestHelper.getClientIP(request));

        eventBus.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), requestHelper.getAppUrl(request)));

        return new GenericResponse("success");
    }
}
