package uz.shoh.tjbot.entitys.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.shoh.tjbot.entitys.user.entiry.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
