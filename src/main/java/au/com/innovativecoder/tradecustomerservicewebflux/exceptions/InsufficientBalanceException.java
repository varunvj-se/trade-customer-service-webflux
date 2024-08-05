package au.com.innovativecoder.tradecustomerservicewebflux.exceptions;

public class InsufficientBalanceException extends RuntimeException {

    private static final String MESSAGE = "Customer [id=%d] does not have enough funds to carry this transaction";

    public InsufficientBalanceException(Integer id) {
        super(MESSAGE.formatted(id));
    }
}
