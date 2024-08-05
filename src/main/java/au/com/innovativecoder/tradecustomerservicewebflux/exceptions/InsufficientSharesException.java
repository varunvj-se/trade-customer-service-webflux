package au.com.innovativecoder.tradecustomerservicewebflux.exceptions;

public class InsufficientSharesException extends RuntimeException {

    private static final String MESSAGE = "Customer [id=%d] does not have enough shares to complete this transaction";

    public InsufficientSharesException(Integer customerId){
        super(MESSAGE.formatted(customerId));
    }

}
