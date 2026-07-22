package util;

/**
 * Password hashing via jBCrypt ({@code org.mindrot:jbcrypt}).
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Senha não pode ser nula.");
        }
        return org.mindrot.jbcrypt.BCrypt.hashpw(raw, org.mindrot.jbcrypt.BCrypt.gensalt());
    }

    public static boolean matches(String raw, String hash) {
        if (raw == null || hash == null || hash.isBlank()) {
            return false;
        }
        return org.mindrot.jbcrypt.BCrypt.checkpw(raw, hash);
    }
}
