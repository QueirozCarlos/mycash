package exception;

public class MovementNotFoundException extends ApplicationException {

    public MovementNotFoundException(Long id) {
        super("Movimentação com id " + id + " não encontrada.");
    }
}
