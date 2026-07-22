package exception;

public class RecurringAccountNotFoundException extends ApplicationException {

    public RecurringAccountNotFoundException(Long id) {
        super("Conta recorrente com id " + id + " não encontrada.");
    }
}
