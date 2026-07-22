package repository;

import config.JpaSupport;
import model.UserEntity;

import java.util.List;
import java.util.Optional;

public class JpaUserRepository implements UserRepository {

    @Override
    public UserEntity save(UserEntity user) {
        return JpaSupport.inTransaction(em -> {
            return em.merge(user);
        });
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        return JpaSupport.readOnly(em -> Optional.ofNullable(em.find(UserEntity.class, id)));
    }

    @Override
    public Optional<UserEntity> findFirst() {
        return JpaSupport.readOnly(em -> em.createQuery(
                        "select u from UserEntity u order by u.id asc", UserEntity.class)
                .setMaxResults(1)
                .getResultStream()
                .findFirst());
    }

    @Override
    public List<UserEntity> findAll() {
        return JpaSupport.readOnly(em -> em.createQuery(
                        "select u from UserEntity u order by u.id asc", UserEntity.class)
                .getResultList());
    }
}
