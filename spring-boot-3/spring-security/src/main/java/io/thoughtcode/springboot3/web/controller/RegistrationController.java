package io.thoughtcode.springboot3.web.controller;

import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import io.thoughtcode.springboot3.config.auth.HttpRequestHelper;
import io.thoughtcode.springboot3.service.UserSecurityService;
import io.thoughtcode.springboot3.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class RegistrationController {

    private final Logger LOG = LoggerFactory.getLogger(RegistrationController.class);

    private final UserService userService;

    private final UserSecurityService userSecurityService;

    private final HttpRequestHelper requestHelper;

    private final MessageSource messages;

    // @formatter:off
    RegistrationController(final UserService userService,
                           final UserSecurityService userSecurityService,
                           final HttpRequestHelper requestHelper,
                           final MessageSource messages) {
        this.userService = userService;
        this.userSecurityService = userSecurityService;
        this.requestHelper = requestHelper;
        this.messages = messages;
    }

    @GetMapping("/login")
    public ModelAndView login(final HttpServletRequest request,
                              final ModelMap model,
                              @RequestParam("messageKey") final Optional<String> messageKey,
                              @RequestParam("error") final Optional<String> error) {

        final Locale locale = request.getLocale();

        model.addAttribute("lang", locale.getLanguage());

        messageKey.ifPresent(key -> {
            final String message = messages.getMessage(key, null, locale);
            model.addAttribute("message", message);
        });

        error.ifPresent(e -> model.addAttribute("error", e));

        return new ModelAndView("login", model);
    }
    // @formatter:on
}
