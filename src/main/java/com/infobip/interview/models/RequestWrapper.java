package com.infobip.interview.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Created by mikhail.davydov on 27.09.2017.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestWrapper {
    @JsonProperty("AccountId")
    private String username;
    private String url;
    private String redirectType;
}
