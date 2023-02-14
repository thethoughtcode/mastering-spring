package io.thoughtcode.springboot3.service;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import io.thoughtcode.springboot3.config.MessageSourceHelper;
import io.thoughtcode.springboot3.config.auth.HttpRequestHelper;
import io.thoughtcode.springboot3.entity.DeviceMetadata;
import io.thoughtcode.springboot3.entity.User;
import io.thoughtcode.springboot3.repository.DeviceMetadataRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class DeviceMetadataService {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceMetadataService.class);

    public static final String UNKNOWN = "UNKNOWN";

    private static final String SUBJECT = "New Login Notification";

    private static final String LOCALHOST_IP = "127.0.0.1";

    static final String FROM_EMAIL = "no-reply@test.com";

    private final Environment env;

    private final DeviceMetadataRepository repository;

    private final DatabaseReader geoipReader;

    private final HttpRequestHelper requestHelper;

    private final JavaMailSender mailSender;

    private final MessageSourceHelper messagesHelper;

    // @formatter:off
    DeviceMetadataService(final Environment env,
                          final DeviceMetadataRepository repository,
                          final DatabaseReader geoipReader,
                          final HttpRequestHelper requestHelper,
                          final JavaMailSender mailSender,
                          final MessageSourceHelper messagesHelper) {
        this.env = env;
        this.repository = repository;
        this.geoipReader = geoipReader;
        this.requestHelper = requestHelper;
        this.mailSender = mailSender;
        this.messagesHelper = messagesHelper;
    }
    // @formatter:on

    public void verifyDevice(final User user, final HttpServletRequest request) throws IOException, GeoIp2Exception {

        final String ip = requestHelper.getClientIP(request);
        final String location = getIpLocationCountry(ip);
        final String deviceDetails = requestHelper.getClientDetails(request);

        final Optional<DeviceMetadata> dbDevice = findExistingDevice(user.getId(), deviceDetails, location);

        final DeviceMetadata deviceMetadata;

        if (dbDevice.isEmpty()) {

            sendNewDeviceNotification(deviceDetails, location, ip, user.getEmail(), request.getLocale());

            deviceMetadata = new DeviceMetadata();
            deviceMetadata.setUserId(user.getId());
            deviceMetadata.setLocation(location);
            deviceMetadata.setDeviceDetails(deviceDetails);
            deviceMetadata.setLastLoggedIn(OffsetDateTime.now());

        } else {
            deviceMetadata = dbDevice.get();

            deviceMetadata.setLastLoggedIn(OffsetDateTime.now());
        }

        repository.save(deviceMetadata);
    }

    public DeviceMetadata addDevice(final User user, final String ip, final String deviceDetails) {

        final String location = getIpLocationCountry(ip);

        final DeviceMetadata deviceMetadata = new DeviceMetadata();
        deviceMetadata.setUserId(user.getId());
        deviceMetadata.setLocation(location);
        deviceMetadata.setDeviceDetails(deviceDetails);

        return repository.save(deviceMetadata);
    }

    private Optional<DeviceMetadata> findExistingDevice(final Long userId, final String deviceDetails, final String country) {
        // @formatter:off
        return repository.findByUserId(userId)
                         .stream()
                         .filter(device -> device.getDeviceDetails().equals(deviceDetails) && device.getLocation().equals(country))
                         .findFirst();
        // @formatter:on
    }

    public String getIpLocationCountry(final String ip) {

        if (isLocalhost(ip)) {
            return "localhost";
        }

        try {
            return geoipReader.country(InetAddress.getByName(ip)).getCountry().getName();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getIpLocationCity(final String ip) {

        if (isLocalhost(ip)) {
            return "localhost";
        }

        String location;

        try {

            final CityResponse cityResponse = geoipReader.city(InetAddress.getByName(ip));

            if (nonNull(cityResponse) && nonNull(cityResponse.getCity()) && !StringUtils.hasLength(cityResponse.getCity().getName())) {
                location = cityResponse.getCity().getName();
            } else {
                location = UNKNOWN;
            }

        } catch (final Exception e) {
            location = UNKNOWN;
            LOG.error("Error while resolve IP to City - {}", ip, e);
        }

        return location;
    }

    private boolean isLocalhost(final String ip) {
        return LOCALHOST_IP.equals(ip);
    }

    public boolean isGeoIpLibEnabled() {
        return Boolean.parseBoolean(env.getProperty("spring.geo-ip.enabled"));
    }

    private void sendNewDeviceNotification(final String device, final String country, final String ip, final String email, final Locale locale) {

        final String textDevice = format("%s %s\n", messagesHelper.getMessage("message.login.notification.deviceDetails", locale), device);
        final String textCountry = format("%s %s\n", messagesHelper.getMessage("message.login.notification.location", locale), country);
        final String textIp = format("%s %s\n", messagesHelper.getMessage("message.login.notification.ip", locale), ip);

        final SimpleMailMessage notification = new SimpleMailMessage();
        notification.setTo(email);
        notification.setSubject(SUBJECT);
        notification.setText(textDevice + textCountry + textIp);
        notification.setFrom(FROM_EMAIL);

        mailSender.send(notification);
    }
}
