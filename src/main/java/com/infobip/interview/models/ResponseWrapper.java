package com.infobip.interview.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Data;

/**
 * Created by mikhail.davydov on 26.09.2017.
 */
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class ResponseWrapper {
    private Boolean success;
    private String description;
    private String password;
    private String shortUrl;
}
