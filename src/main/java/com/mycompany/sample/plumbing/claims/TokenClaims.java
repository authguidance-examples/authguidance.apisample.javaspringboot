package com.mycompany.sample.plumbing.claims;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.errors.ErrorCodes;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import lombok.Getter;

/*
 * Claims included in the JWT
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class TokenClaims {

    @Getter
    private final String subject;

    @Getter
    private final String[] scopes;

    @Getter
    private final int expiry;

    /*
     * Read claims from the claims cache
     */
    public static TokenClaims importData(final JsonNode data) {

        var subjectValue = data.get("subject").asText();
        var scopeValue = data.get("scopes").asText();
        var expiryValue = data.get("expiry").asInt();
        return new TokenClaims(subjectValue, scopeValue.split(" "), expiryValue);
    }

    public TokenClaims(final String subject, final String[] scopes, final int expiry) {

        this.subject = subject;
        this.scopes = scopes;
        this.expiry = expiry;
    }

    /*
     * Write data to the claims cache
     */
    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("subject", this.subject);
        data.put("scopes", String.join(" ", this.scopes));
        data.put("expiry", this.expiry);
        return data;
    }

    /*
     * Verify that we are allowed to access this type of data, via the scopes from the token
     */
    public void verifyScope(final String scope) {

        var found = Arrays.stream(this.scopes).filter(s -> s.contains(scope)).findFirst();
        if (found.isEmpty()) {
            throw ErrorFactory.createClientError(
                    HttpStatus.FORBIDDEN,
                    ErrorCodes.INSUFFICIENT_SCOPE,
                    "Access token does not have a valid scope for this API");
        }
    }
}
