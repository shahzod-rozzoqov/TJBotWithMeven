package uz.shoh.tjbot.entitys.testSubmission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.shoh.tjbot.entitys.testSubmission.entity.BlocTestSubmission;

/**
 * @author shahzod-rozzoqov
 */
@Repository
public interface BlockTestSubmissionRepository extends JpaRepository<BlocTestSubmission, Integer> {
    BlocTestSubmission findByUserIdAndBlocTestId(String userId, Integer blockTestId);

}
