package uz.shoh.tjbot.entitys.test.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.shoh.tjbot.entitys.testSubmission.entity.BlocTestSubmission;
import uz.shoh.tjbot.entitys.user.entiry.User;

import java.util.List;

/**
 * @author shahzod-rozzoqov
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlocTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private boolean status;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "blocTest", cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Test> tests;
    @OneToMany(mappedBy = "blocTest", cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<BlocTestSubmission> blocTestSubmissions;
}