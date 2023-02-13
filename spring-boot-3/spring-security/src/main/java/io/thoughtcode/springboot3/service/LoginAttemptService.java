package io.thoughtcode.springboot3.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPT = 3;

    private final LoadingCache<String, Integer> attemptsCache;

    // @formatter:off
    public LoginAttemptService() {
        this.attemptsCache = CacheBuilder.newBuilder()
                                         .expireAfterWrite(1, TimeUnit.DAYS)
                                         .build(
                                                 new CacheLoader<String, Integer>() {
                                                     @Override
                                                     public Integer load(final String key) {
                                                         return 0;
                                                     }
                                                 }
                                         );
    }
    // @formatter:on

    public void loginFailed(final String key) {

        int attempts;

        try {
            attempts = attemptsCache.get(key);
        } catch (final ExecutionException e) {
            attempts = 0;
        }

        attempts++;

        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(final String ip) {
        try {
            return attemptsCache.get(ip) >= MAX_ATTEMPT;
        } catch (final ExecutionException e) {
            return false;
        }
    }
}
