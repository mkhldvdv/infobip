package com.infobip.interview.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by mikhail.davydov on 26.09.2017.
 */
@Data
public class User {
    @JsonProperty("AccountId")
    private String username;
}
