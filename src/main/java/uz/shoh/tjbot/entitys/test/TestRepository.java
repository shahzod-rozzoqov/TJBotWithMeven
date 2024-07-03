package uz.shoh.tjbot.entitys.test;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.shoh.tjbot.entitys.test.entity.Test;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Integer> {
    Test findByIdAndUserId(Integer testId, String userId);
}
