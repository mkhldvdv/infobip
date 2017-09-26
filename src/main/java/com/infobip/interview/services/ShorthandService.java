package com.infobip.interview.services;

import com.infobip.interview.db.ShorthandDao;
import com.infobip.interview.models.Shorthand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by mikhail.davydov on 26.09.2017.
 */

@Slf4j
@Service
public class ShorthandService {

    private final ShorthandDao dao;

    @Autowired
    public ShorthandService(ShorthandDao dao) {
        this.dao = dao;
    }

    public UserDetails createUser(String username) {
        log.info("Creating account for {}", username);
        if (dao.userExists(username)) {
            log.info("Account for {} already exists", username);
            return null;
        }
        String password = RandomStringUtils.randomAlphanumeric(8);
        UserDetails user = dao.createUser(username, password);
        log.info("Account for {} created", username);
        return user;
    }

    public Shorthand createShorthand(String username, Shorthand shorthand) {
        log.info("Creating shorthand for {}", shorthand.getUrl());

        List<Shorthand> shorts = dao.getUserShorts(username);
        if (shorts == null) {
            shorts = new ArrayList<>();
        }

        // check if exists
        Optional<Shorthand> existingShort = shorts.stream()
                .filter(s -> s.getUrl().equals(shorthand.getUrl()))
                .findFirst();
        if (existingShort.isPresent()) {
            log.info("Shorthand {} exists for {}", existingShort.get().getShortUrl(), shorthand.getUrl());
            return existingShort.get();
        }

        shorthand.setShortUrl(RandomStringUtils.randomAlphanumeric(6));
        shorthand.setCount(0);
        shorts.add(shorthand);
        dao.insertShorthand(username, shorts);
        log.info("Shorthand {} for {} created", shorthand.getShortUrl(), shorthand.getUrl());
        return shorthand;
    }

    public List<Shorthand> getUserStats(String username) {
        log.info("Getting stats for {}", username);
        return dao.getUserShorts(username);
    }

    public Shorthand hitCount(String username, String url) {
        log.info("Incrementing hitCount for {}", url);
        Optional<Shorthand> shorthand = dao.getUserShorts(username).stream()
                .filter(s -> s.getShortUrl().equals(url))
                .findFirst();
        shorthand.ifPresent(s -> s.setCount(s.getCount() + 1));
        return shorthand.orElse(null);
    }
}
