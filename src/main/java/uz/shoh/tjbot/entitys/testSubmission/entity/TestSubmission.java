package uz.shoh.tjbot.entitys.testSubmission.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.shoh.tjbot.entitys.test.entity.Test;
import uz.shoh.tjbot.entitys.user.entiry.User;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TestSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "test_id")
    private Test test;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "correct_answers_count")
    private int correctAnswersCount;
    @Column(name = "total_answers_count")
    private int totalAnswersCount;
    private String wrongAnswers;
    private LocalDateTime submissionTime;
}
