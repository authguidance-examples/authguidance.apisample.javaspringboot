package com.mycompany.sample.framework.api.oauth.configuration;

import lombok.Getter;
import lombok.Setter;

/*
 * OAuth configuration settings
 */
public class OAuthConfiguration {

    @Getter
    @Setter
    private String authority;

    @Getter
    @Setter
    private String clientId;

    @Getter
    @Setter
    private String clientSecret;

    @Getter
    @Setter
    private int maxClaimsCacheMinutes;
}
