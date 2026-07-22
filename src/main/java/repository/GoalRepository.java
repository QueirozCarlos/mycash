package repository;

import model.GoalEntity;

import java.util.List;
import java.util.Optional;

public interface GoalRepository {

    GoalEntity save(GoalEntity goal);

    Optional<GoalEntity> findById(Long id);

    List<GoalEntity> findAll();

    void deleteById(Long id);
}
