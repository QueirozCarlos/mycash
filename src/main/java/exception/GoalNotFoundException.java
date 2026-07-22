package exception;

public class GoalNotFoundException extends ApplicationException {

    public GoalNotFoundException(Long id) {
        super("Meta com id " + id + " não encontrada.");
    }
}
