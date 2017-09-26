package com.infobip.interview.web;

import com.infobip.interview.models.RequestWrapper;
import com.infobip.interview.models.ResponseWrapper;
import com.infobip.interview.models.Shorthand;
import com.infobip.interview.models.User;
import com.infobip.interview.services.ShorthandService;
import com.infobip.interview.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by mikhail.davydov on 26.09.2017.
 */

@Slf4j
@RestController
public class ShorthandController {

    private static final String SLASH = "/";
    private static final int DEFAULT_PORT = 80;
    private final ShorthandService service;

    @Autowired
    public ShorthandController(ShorthandService service) {
        this.service = service;
    }

    @RequestMapping(value = "/username", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity username(@RequestBody RequestWrapper request) {
        // check input
        if (!(request.getUsername() != null && Utils.isValidString(request.getUsername()))) {
            return ResponseEntity.badRequest().body(Utils.response(false, "incorrect username"));
        }

        log.info("POST request on creating user for {}", request.getUsername());
        UserDetails response = service.createUser(request.getUsername());
        if (response == null) {
            return ResponseEntity.badRequest().body(Utils.response(false, "Account with that ID already exists"));
        }
        return ResponseEntity.ok(Utils.response(true, "Your account is opened", response.getPassword()));
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity register(HttpServletRequest request,
                                   @RequestBody RequestWrapper wrapper) {
        Shorthand shorthand = Shorthand.builder().build();
        // check input
        if (!(wrapper.getUrl() != null && Utils.isValidUrl(wrapper.getUrl()))) {
            return ResponseEntity.badRequest().body(Utils.response(false, "incorrect url"));
        }
        shorthand.setUrl(wrapper.getUrl());
        // redirectType
        if (wrapper.getRedirectType() != null) {
            try {
                int redirectType = Integer.parseInt(wrapper.getRedirectType());
                shorthand.setRedirectType(redirectType);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Utils.response(false, "incorrect redirectType"));
            }
        } else {
            shorthand.setRedirectType(HttpServletResponse.SC_MOVED_TEMPORARILY);
        }
        if (!isValidRedirectType(shorthand.getRedirectType())) {
            return ResponseEntity.badRequest().body(Utils.response(false, "incorrect redirectType"));
        }

        log.info("POST request on register shorthand for {}", wrapper.getUrl());
        String username = request.getUserPrincipal().getName();
        log.info("get base url: {}", getBaseUrl(request));
        String shortUrl = getBaseUrl(request) + SLASH + service.createShorthand(username, shorthand).getShortUrl();
        return ResponseEntity.ok(Utils.response(shortUrl));
    }

    @RequestMapping(value = "/statistic/{AccountId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity statistic(HttpServletRequest request,
                                    @PathVariable(value = "AccountId") String username) {
        //check input
        String principal = request.getUserPrincipal().getName();
        log.info("{} requested stats for {}", principal, username);
        if (!principal.equals(username)) {
            return ResponseEntity.badRequest().body(Utils.response(false, "incorrect username"));
        }

        Map<String, Integer> response = service.getUserStats(username).stream()
                .collect(Collectors.toMap(Shorthand::getUrl, Shorthand::getCount));
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{url}", method = RequestMethod.GET)
    public ResponseEntity redirect(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @PathVariable String url) {
        //check input
        String username = request.getUserPrincipal().getName();
        log.info("{} requested redirect with the shorthand {}", username, url);
        Shorthand shorthand = service.hitCount(username, url);
        if (shorthand == null) {
            return ResponseEntity.badRequest().body(Utils.response(false, "incorrect shorthand"));
        }

        log.info("redirect from {} to {}", url, shorthand.getUrl());
        response.setStatus(shorthand.getRedirectType());
        response.setHeader("Location", shorthand.getUrl());
        return null;
    }

    @RequestMapping(value = "/help", method = RequestMethod.GET)
    public ResponseEntity help() {
        return ResponseEntity.ok("help");
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme() + "://";
        String serverName = request.getServerName();
        String serverPort = (request.getServerPort() == DEFAULT_PORT) ? "" : ":" + request.getServerPort();
        return scheme + serverName + serverPort;
    }

    private boolean isValidRedirectType(int redirectType) {
        return redirectType == HttpServletResponse.SC_MOVED_PERMANENTLY || redirectType == HttpServletResponse.SC_MOVED_TEMPORARILY;
    }
}
