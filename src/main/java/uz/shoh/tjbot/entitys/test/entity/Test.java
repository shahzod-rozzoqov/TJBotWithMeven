package uz.shoh.tjbot.entitys.test.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.shoh.tjbot.entitys.testSubmission.entity.TestSubmission;
import uz.shoh.tjbot.entitys.user.entiry.User;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String subjectName;
    private String keys;
    private boolean status;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "bloc_test_id")
    private BlocTest blocTest;
    @OneToMany(mappedBy = "test", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<TestSubmission> submissions;
}
