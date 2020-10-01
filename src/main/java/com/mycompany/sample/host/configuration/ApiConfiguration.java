package com.mycompany.sample.host.configuration;

import lombok.Getter;
import lombok.Setter;

/*
 * API specific properties
 */
public class ApiConfiguration {

    @Getter
    @Setter
    private String sslCertificateFileName;

    @Getter
    @Setter
    private String sslCertificatePassword;

    @Getter
    @Setter
    private int sslPort;

    @Getter
    @Setter
    private String proxyUrl;

    @Getter
    @Setter
    private boolean useProxy;

    @Getter
    @Setter
    private String[] webTrustedOrigins;
}
