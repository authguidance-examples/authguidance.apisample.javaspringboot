package com.mycompany.sample.logic.services;

import static java.util.concurrent.CompletableFuture.completedFuture;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.mycompany.sample.logic.claims.SampleClaimsPrincipal;
import com.mycompany.sample.logic.claims.SampleExtraClaims;
import com.mycompany.sample.logic.entities.Company;
import com.mycompany.sample.logic.entities.CompanyTransactions;
import com.mycompany.sample.logic.errors.SampleErrorCodes;
import com.mycompany.sample.logic.repositories.CompanyRepository;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;
import com.mycompany.sample.plumbing.errors.ClientError;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * The service class applies business authorization
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CompanyService {

    private final CompanyRepository repository;
    private final SampleClaimsPrincipal claims;

    public CompanyService(final CompanyRepository repository, final ClaimsPrincipal claims) {

        this.repository = repository;
        this.claims = (SampleClaimsPrincipal) claims;
    }

    /*
     * Forward to the repository to get the company list
     */
    public CompletableFuture<List<Company>> getCompanyList() {

        // Filter on authorized items
        Function<List<Company>, CompletableFuture<List<Company>>> callback = data ->
            completedFuture(data.stream()
                .filter(this::isUserAuthorizedForCompany)
                .collect(Collectors.toList()));

        return this.repository.getCompanyList().thenCompose(callback);
    }

    /*
     * Forward to the repository to get the company transactions
     */
    public CompletableFuture<CompanyTransactions> getCompanyTransactions(final int companyId) {

        // Deny access to individual items
        Function<CompanyTransactions, CompletableFuture<CompanyTransactions>> callback = data -> {

            if (data == null || !this.isUserAuthorizedForCompany(data.getCompany())) {
                throw this.unauthorizedError(companyId);
            }

            return completedFuture(data);
        };

        return this.repository.getCompanyTransactions(companyId).thenCompose(callback);
    }

    /*
     * A simple example of applying domain specific claims to items
     */
    private boolean isUserAuthorizedForCompany(final Company company) {

        var isAdmin = this.claims.getRole().toLowerCase().contains("admin");
        if (isAdmin) {
            return true;
        }

        var extraClaims = (SampleExtraClaims) this.claims.getExtraClaims();
        return Arrays.stream(extraClaims.getRegions()).anyMatch(ur -> ur.equals(company.getRegion()));
    }

    /*
     * Return 404 for both not found items and also those that are not authorized
     */
    private ClientError unauthorizedError(final int companyId) {

        var message = String.format("Transactions for company %d were not found for this user", companyId);
        return ErrorFactory.createClientError(
                HttpStatus.NOT_FOUND,
                SampleErrorCodes.COMPANY_NOT_FOUND,
                message);
    }
}
