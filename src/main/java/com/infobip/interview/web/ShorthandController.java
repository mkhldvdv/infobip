package com.infobip.interview.web;

import com.infobip.interview.models.HelpResponse;
import com.infobip.interview.models.RequestWrapper;
import com.infobip.interview.models.Shorthand;
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

    @RequestMapping(value = "/account", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity username(@RequestBody RequestWrapper request) {
        // check input
        if (!(request.getUsername() != null && Utils.isValidString(request.getUsername()))) {
            return ResponseEntity.badRequest().body(Utils.response(false, "incorrect AccountId"));
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
        // check input
        if (!(wrapper.getUrl() != null && Utils.isValidUrl(wrapper.getUrl()))) {
            return ResponseEntity.badRequest().body(Utils.response(false, "incorrect url"));
        }

        // redirectType
        int redirectType = 0;
        if (wrapper.getRedirectType() != null) {
            try {
                redirectType = Integer.parseInt(wrapper.getRedirectType());
            } catch (NumberFormatException e) {
                // nothing to do here
            }
        } else {
            redirectType = HttpServletResponse.SC_MOVED_TEMPORARILY;
        }
        if (!isValidRedirectType(redirectType)) {
            return ResponseEntity.badRequest().body(Utils.response(false, "incorrect redirectType"));
        }

        log.info("POST request on register shorthand for {}", wrapper.getUrl());
        String username = request.getUserPrincipal().getName();
        log.info("get base url: {}", getBaseUrl(request));
        String shortUrl = getBaseUrl(request) + SLASH + service.createShorthand(username, wrapper.getUrl(), redirectType).getShortUrl();
        return ResponseEntity.ok(Utils.response(shortUrl));
    }

    @RequestMapping(value = "/statistic/{AccountId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity statistic(HttpServletRequest request,
                                    @PathVariable(value = "AccountId") String username) {
        //check input
        String principal = request.getUserPrincipal().getName();
        log.info("{} requested stats for {}", principal, username);
        if (!principal.equals(username)) {
            return ResponseEntity.badRequest().body(Utils.response(false, "incorrect AccountId"));
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
        String installation = "prerequisites:\n" +
                "installed Java 8 and maven\n" +
                "1. download project with the link https://github.com/mkhldvdv/infobip/archive/master.zip and unzip \n" +
                "2. step into the unzipped directory and in the command line execute: clean package -Dmaven.test.skip=true";
        String launching = "step into the directory with the jar file and execute in the command line:\n" +
                "java -jar interview-0.0.1-SNAPSHOT.jar";
        String usage = "usage:\n" +
                "\n" +
                "Opening of accounts:\n" +
                "POST to /account with body { AccountId : 'myAccountId'}\n" +
                "\n" +
                "Registration of URLs:\n" +
                "POST to /register with body {url: 'http://stackoverflow.com/questions/1567929/website-safe-data-access-architecture-question?rq=1',redirectType : 301}\n" +
                "\n" +
                "Retrieval of statistics:\n" +
                "GET to /statistic/{AccountId}\n" +
                "\n" +
                "Redirecting with a shorthand:\n" +
                "GET to /{url}\n" +
                "\n" +
                "Help:\n" +
                "GET to /help";
        return ResponseEntity.ok(HelpResponse.builder().installation(installation).launching(launching).usage(usage).build());
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
