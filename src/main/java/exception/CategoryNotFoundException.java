package exception;

public class CategoryNotFoundException extends ApplicationException {

    public CategoryNotFoundException(Long id) {
        super("Categoria com id " + id + " não encontrada.");
    }
}