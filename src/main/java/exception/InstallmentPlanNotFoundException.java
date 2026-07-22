package exception;

public class InstallmentPlanNotFoundException extends ApplicationException {

    public InstallmentPlanNotFoundException(Long id) {
        super("Parcelamento com id " + id + " não encontrado.");
    }
}
