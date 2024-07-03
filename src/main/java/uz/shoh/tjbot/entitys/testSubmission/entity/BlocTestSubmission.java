package uz.shoh.tjbot.entitys.testSubmission.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.shoh.tjbot.entitys.test.entity.BlocTest;
import uz.shoh.tjbot.entitys.user.entiry.User;

import java.time.LocalDateTime;

/**
 * @author shahzod-rozzoqov
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class BlocTestSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private boolean answerIsCollected;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "bloc_test_id")
    private BlocTest blocTest;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "first_compulsory_subject_correct_answers_count")
    private int firstCompulsorySubjectCorrectAnswersCount;
    @Column(name = "second_compulsory_subject_correct_answers_count")
    private int secondCompulsorySubjectCorrectAnswersCount;
    @Column(name = "third_compulsory_subject_correct_answers_count")
    private int thirdCompulsorySubjectCorrectAnswersCount;
    @Column(name = "first_major_subject_correct_answers_count")
    private int firstMajorSubjectCorrectAnswersCount;
    @Column(name = "second_major_subject_correct_answers_count")
    private int secondMajorSubjectCorrectAnswersCount;

    @Column(name = "first_compulsory_subject_total_answers_count")
    private int firstCompulsorySubjectTotalAnswersCount;
    @Column(name = "second_compulsory_subject_total_answers_count")
    private int secondCompulsorySubjectTotalAnswersCount;
    @Column(name = "third_compulsory_subject_total_answers_count")
    private int thirdCompulsorySubjectTotalAnswersCount;
    @Column(name = "first_major_subject_total_answers_count")
    private int firstMajorSubjectTotalAnswersCount;
    @Column(name = "second_major_subject_total_answers_count")
    private int secondMajorSubjectTotalAnswersCount;

    private String firstCompulsorySubjectWrongAnswers;
    private String secondCompulsorySubjectWrongAnswers;
    private String thirdCompulsorySubjectWrongAnswers;
    private String firstMajorSubjectWrongAnswers;
    private String secondMajorSubjectWrongAnswers;
    private LocalDateTime submissionTime;
}
