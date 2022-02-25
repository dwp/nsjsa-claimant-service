package uk.gov.dwp.jsa.claimant.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.claimant.service.services.ResponseBuilder;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(ClaimantByIdNotFoundException.class)
    public final @ResponseBody
    ResponseEntity<ApiResponse<String>> handleClaimantByIdNotFoundException(
            final Exception ex,
            final WebRequest request
    ) {
        return new ResponseBuilder<String>()
                .withStatus(HttpStatus.NOT_FOUND)
                .withApiError(
                        ClaimantByIdNotFoundException.CODE,
                        ClaimantByIdNotFoundException.MESSAGE
                ).build();
    }

    @ExceptionHandler(ClaimantAlreadyExistsException.class)
    public final @ResponseBody
    ResponseEntity<ApiResponse<String>> handleClaimantAlreadyExistsException(
            final Exception ex,
            final WebRequest request
    ) {
        return new ResponseBuilder<String>()
                .withStatus(HttpStatus.CONFLICT)
                .withApiError(
                        ClaimantAlreadyExistsException.CODE,
                        ClaimantAlreadyExistsException.MESSAGE
                ).build();
    }

}
