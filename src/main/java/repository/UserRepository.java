package repository;

import model.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    UserEntity save(UserEntity user);

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findFirst();

    List<UserEntity> findAll();
}
