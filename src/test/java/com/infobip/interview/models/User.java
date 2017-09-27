package com.infobip.interview.models;

import lombok.Builder;
import lombok.Data;

/**
 * Created by mikhail.davydov on 27.09.2017.
 */
@Data
@Builder
public class User {
    private String username;
    private String password;
}
