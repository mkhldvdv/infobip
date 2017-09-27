package com.infobip.interview.models;

import lombok.Builder;
import lombok.Data;

/**
 * Created by mikhail.davydov on 28.09.2017.
 */
@Data
@Builder
public class HelpResponse {
    String installation;
    String launching;
    String usage;
}
