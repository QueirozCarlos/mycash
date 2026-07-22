package exception;

public class CreditCardNotFoundException extends ApplicationException {

    public CreditCardNotFoundException(Long id) {
        super("Cartão com id " + id + " não encontrado.");
    }
}
