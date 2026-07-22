package service;

import model.UserEntity;
import repository.JpaUserRepository;
import repository.UserRepository;
import util.PasswordHasher;

public class UserService {

    private static final String DEFAULT_USER_NAME = "Usuário Local";
    private static final String DEFAULT_PASSWORD = "financeiro";

    private final UserRepository userRepository;

    public UserService() {
        this(new JpaUserRepository());
    }

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity ensureDefaultUser() {
        return userRepository.findFirst()
                .orElseGet(() -> userRepository.save(new UserEntity(
                        DEFAULT_USER_NAME,
                        null,
                        PasswordHasher.hash(DEFAULT_PASSWORD))));
    }

    public UserEntity findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário com id " + id + " não encontrado."));
    }
}
