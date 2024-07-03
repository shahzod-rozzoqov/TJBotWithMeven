package uz.shoh.tjbot.entitys.test;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.shoh.tjbot.entitys.test.entity.BlocTest;

/**
 * @author shahzod-rozzoqov
 */
@Repository
public interface BlocTestRepository extends JpaRepository<BlocTest, Integer> {

}
