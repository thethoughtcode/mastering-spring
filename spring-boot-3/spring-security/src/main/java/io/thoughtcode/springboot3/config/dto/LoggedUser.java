package io.thoughtcode.springboot3.config.dto;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

@Component
public class LoggedUser implements HttpSessionBindingListener, Serializable {

    private static final long serialVersionUID = 9146373828636137225L;

    private String username;

    private ActiveUserStore activeUserStore;

    public LoggedUser(final String username, final ActiveUserStore activeUserStore) {
        this.username = username;
        this.activeUserStore = activeUserStore;
    }

    public LoggedUser() {
    }

    @Override
    public void valueBound(final HttpSessionBindingEvent event) {

        final List<String> users = activeUserStore.getUsers();

        final LoggedUser user = (LoggedUser) event.getValue();

        if (!users.contains(user.getUsername())) {
            users.add(user.getUsername());
        }
    }

    @Override
    public void valueUnbound(final HttpSessionBindingEvent event) {

        final List<String> users = activeUserStore.getUsers();

        final LoggedUser user = (LoggedUser) event.getValue();

        users.remove(user.getUsername());
    }

    public String getUsername() {
        return username;
    }
}
