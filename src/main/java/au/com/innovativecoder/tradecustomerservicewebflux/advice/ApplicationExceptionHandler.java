package au.com.innovativecoder.tradecustomerservicewebflux.advice;

import au.com.innovativecoder.tradecustomerservicewebflux.exceptions.CustomerNotFoundException;
import au.com.innovativecoder.tradecustomerservicewebflux.exceptions.InsufficientBalanceException;
import au.com.innovativecoder.tradecustomerservicewebflux.exceptions.InsufficientSharesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.util.function.Consumer;

@ControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleException(CustomerNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex, problem -> {
            problem.setType(URI.create("http://innovativecoder.com.au/problems/customer-not-found"));
            problem.setTitle("Customer Not Found");
        });
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ProblemDetail handleException(InsufficientBalanceException ex) {
        return build(HttpStatus.BAD_REQUEST, ex, problem -> {
            problem.setType(URI.create("http://innovativecoder.com.au/problems/insufficient-balance"));
            problem.setTitle("Insufficient Balance");
        });
    }

    @ExceptionHandler(InsufficientSharesException.class)
    public ProblemDetail handleException(InsufficientSharesException ex) {
        return build(HttpStatus.BAD_REQUEST, ex, problem -> {
            problem.setType(URI.create("http://innovativecoder.com.au/problems/insufficient-shares"));
            problem.setTitle("Insufficient Shares");
        });
    }

    private ProblemDetail build(HttpStatus status, Exception ex, Consumer<ProblemDetail> consumer) {
        var problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        consumer.accept(problem);
        return problem;
    }
}
