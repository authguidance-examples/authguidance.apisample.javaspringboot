package com.mycompany.sample.plumbing.oauth.tokenvalidation;

import java.net.URI;
import com.mycompany.sample.plumbing.claims.ClaimsPayload;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.nimbusds.oauth2.sdk.TokenIntrospectionErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/*
 * An implementation that validates access tokens by introspecting them
 */
@Component
@Scope(value = CustomRequestScope.NAME)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class IntrospectionValidator implements TokenValidator {

    private final OAuthConfiguration configuration;

    public IntrospectionValidator(final OAuthConfiguration configuration) {
        this.configuration = configuration;
    }

    /*
     * The entry point for validating a token via introspection and returning its claims
     */
    @Override
    public ClaimsPayload validateToken(final String accessToken) {

        try {

            // Supply the API's introspection credentials
            var introspectionClientId = new ClientID(this.configuration.getIntrospectClientId());
            var introspectionClientSecret = new Secret(this.configuration.getIntrospectClientSecret());
            var credentials = new ClientSecretBasic(introspectionClientId, introspectionClientSecret);

            // Set up the request
            var request = new TokenIntrospectionRequest(
                    new URI(this.configuration.getIntrospectEndpoint()),
                    credentials,
                    new BearerAccessToken(accessToken))
                    .toHTTPRequest();
            request.setAccept("application/json");

            // Make the request and get the response
            HTTPResponse httpResponse = request.send();

            // Handle errors returned in the response body and return an understandable error
            var introspectionResponse = TokenIntrospectionResponse.parse(httpResponse);
            if (!introspectionResponse.indicatesSuccess()) {
                var errorResponse = TokenIntrospectionErrorResponse.parse(httpResponse);
                throw ErrorUtils.fromIntrospectionError(
                        errorResponse.getErrorObject(),
                        this.configuration.getIntrospectEndpoint());
            }

            // Get token claims from the response and return a 401 if the token is invalid or expired
            var data = introspectionResponse.toSuccessResponse();
            if (!data.isActive()) {
                throw ErrorFactory.createClient401Error("Access token is expired and failed introspection");
            }

            // Return a payload object that will be read later
            var payload = new ClaimsPayload(data);
            payload.setStringClaimCallback(this::getStringClaim);
            payload.setExpirationClaimCallback(this::getExpirationClaim);
            return payload;

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromIntrospectionError(e, this.configuration.getIntrospectEndpoint().toString());
        }
    }

    /*
     * Get a string claim from the introspection object
     */
    private String getStringClaim(final Object data, final String name) {

        var claimsSet = (TokenIntrospectionSuccessResponse)data;

        var claim = claimsSet.getStringParameter(name);
        if (StringUtils.hasLength(claim)) {
            return claim;
        }

        throw ErrorUtils.fromMissingClaim(name);
    }

    /*
     * Get the expiration claims from the introspection object
     */
    private long getExpirationClaim(final Object data) {

        var claimsSet = (TokenIntrospectionSuccessResponse)data;
        return claimsSet.getExpirationTime().toInstant().getEpochSecond();
    }
}
