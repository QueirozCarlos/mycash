package repository;

import config.JpaSupport;
import model.GoalEntity;

import java.util.List;
import java.util.Optional;

public class JpaGoalRepository implements GoalRepository {

    @Override
    public GoalEntity save(GoalEntity goal) {
        return JpaSupport.inTransaction(em -> {
            return em.merge(goal);
        });
    }

    @Override
    public Optional<GoalEntity> findById(Long id) {
        return JpaSupport.readOnly(em -> Optional.ofNullable(em.find(GoalEntity.class, id)));
    }

    @Override
    public List<GoalEntity> findAll() {
        return JpaSupport.readOnly(em -> em.createQuery(
                        "select g from GoalEntity g order by g.deadline asc nulls last, g.name asc",
                        GoalEntity.class)
                .getResultList());
    }

    @Override
    public void deleteById(Long id) {
        JpaSupport.inTransaction(em -> {
            GoalEntity goal = em.find(GoalEntity.class, id);
            if (goal != null) {
                em.remove(goal);
            }
        });
    }
}
