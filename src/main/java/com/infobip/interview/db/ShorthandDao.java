package com.infobip.interview.db;

import com.infobip.interview.models.Shorthand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mikhail.davydov on 26.09.2017.
 */

@Slf4j
@Repository
public class ShorthandDao {

    private final InMemoryUserDetailsManager manager;
    private final Map<String, List<Shorthand>> userShorts = new HashMap<>();

    @Autowired
    public ShorthandDao(InMemoryUserDetailsManager manager) {
        this.manager = manager;
    }

    public boolean userExists(String username) {
        return manager.userExists(username);
    }

    public UserDetails getUser(String username) {
        return manager.loadUserByUsername(username);
    }

    public UserDetails createUser(String username, String password) {
        manager.createUser(User.withUsername(username).password(password).roles("USER").build());
        return manager.loadUserByUsername(username);
    }

    public void insertShorthand(String username, List<Shorthand> shorts) {
        userShorts.put(username, shorts);
    }

    public List<Shorthand> getUserShorts(String username) {
        return userShorts.get(username);
    }
}
