package com.infobip.interview.utils;

import com.infobip.interview.models.ResponseWrapper;

import java.util.regex.Pattern;

/**
 * Created by mikhail.davydov on 26.09.2017.
 */
public class Utils {

    public static ResponseWrapper response(boolean success, String description) {
        return response(success, description, null);
    }

    public static ResponseWrapper response(boolean success, String description, String password) {
        return ResponseWrapper.builder()
                .success(success)
                .description(description)
                .password(password)
                .build();
    }

    public static ResponseWrapper response(String shortUrl) {
        return ResponseWrapper.builder()
                .shortUrl(shortUrl)
                .build();
    }

    public static boolean isValidString(String string) {
        String stringRegexp = "^[a-zA-Z][a-zA-Z0-9_-]{3,30}$";
        return Pattern.matches(stringRegexp, string);
    }

    public static boolean isValidUrl(String url) {
        // taken from https://mathiasbynens.be/demo/url-regex
        String urlRegexp = "^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!10(?:\\.\\d{1,3}){3})(?!127(?:\\.\\d{1,3}){3})(?!169\\.254(?:\\.\\d{1,3}){2})(?!192\\.168(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)*(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}]{2,})))(?::\\d{2,5})?(?:/[^\\s]*)?$";
        return Pattern.matches(urlRegexp, url);
    }
}

