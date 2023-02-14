package io.thoughtcode.springboot3.web.controller;

import static io.thoughtcode.springboot3.service.UserService.TOKEN_VALID;
import static java.util.stream.Collectors.toList;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import io.thoughtcode.springboot3.config.auth.HttpRequestHelper;
import io.thoughtcode.springboot3.entity.Privilege;
import io.thoughtcode.springboot3.entity.Role;
import io.thoughtcode.springboot3.entity.User;
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
    // @formatter:on

    @GetMapping("/login")
    public ModelAndView login(final HttpServletRequest request, final ModelMap model, @RequestParam("messageKey") final Optional<String> messageKey,
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

    @GetMapping("/registrationConfirm")
    public ModelAndView confirmRegistration(final HttpServletRequest request, final ModelMap model, @RequestParam("token") final String token)
            throws UnsupportedEncodingException {

        final Locale locale = request.getLocale();
        final String ip = requestHelper.getClientIP(request);
        final String deviceDetails = requestHelper.getClientDetails(request);

        model.addAttribute("lang", locale.getLanguage());

        final String result = userService.validateVerificationToken(token, ip, deviceDetails);

        final String viewName;

        if (result.equals(TOKEN_VALID)) {

            final User user = userService.getUser(token);

            if (user.isUsing2FA()) {
                model.addAttribute("qr", userService.generateQRUrl(user));
                viewName = "redirect:/qrcode.html?lang=" + locale.getLanguage();
            } else {
                authWithoutPassword(user);
                model.addAttribute("messageKey", "message.accountVerified");
                viewName = "redirect:/console";
            }

        } else {

            model.addAttribute("messageKey", "auth.message." + result);
            model.addAttribute("expired", "expired".equals(result));
            model.addAttribute("token", token);

            viewName = "redirect:/bad_user";
        }

        return new ModelAndView(viewName, model);
    }

    @GetMapping("/console")
    public ModelAndView console(final HttpServletRequest request, final ModelMap model,
            @RequestParam("messageKey") final Optional<String> messageKey) {

        final Locale locale = request.getLocale();

        messageKey.ifPresent(key -> {
            final String message = messages.getMessage(key, null, locale);
            model.addAttribute("message", message);
        });

        return new ModelAndView("console", model);
    }

    @GetMapping("/bad_user")
    public ModelAndView badUser(final HttpServletRequest request, final ModelMap model, @RequestParam("messageKey") final Optional<String> messageKey,
            @RequestParam("expired") final Optional<String> expired, @RequestParam("token") final Optional<String> token) {

        final Locale locale = request.getLocale();

        messageKey.ifPresent(key -> {
            final String message = messages.getMessage(key, null, locale);
            model.addAttribute("message", message);
        });

        expired.ifPresent(e -> model.addAttribute("expired", e));
        token.ifPresent(t -> model.addAttribute("token", t));

        return new ModelAndView("bad_user", model);
    }

    private void authWithoutPassword(final User user) {

        final List<Privilege> privileges = user.getRoles().stream().map(Role::getPrivileges).flatMap(Collection::stream).distinct().collect(toList());

        final List<GrantedAuthority> authorities = privileges.stream().map(p -> new SimpleGrantedAuthority(p.getName())).collect(toList());

        final Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
