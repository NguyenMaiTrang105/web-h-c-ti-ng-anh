package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "student_results")
public class StudentResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(nullable = false)
    private Double score;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Lob
    @Column(name = "answer_details_json", columnDefinition = "LONGTEXT")
    private String answerDetailsJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public String getAnswerDetailsJson() { return answerDetailsJson; }
    public void setAnswerDetailsJson(String answerDetailsJson) { this.answerDetailsJson = answerDetailsJson; }

    public List<ResultQuestionDetail> getAnswerDetails() {
        return ResultSnapshotSupport.readDetails(answerDetailsJson);
    }

    public void setAnswerDetails(List<ResultQuestionDetail> details) {
        this.answerDetailsJson = ResultSnapshotSupport.writeDetails(details);
    }

    public boolean hasAnswerDetails() {
        return answerDetailsJson != null && !answerDetailsJson.isBlank();
    }

    public long getCorrectCount() {
        return getAnswerDetails().stream().filter(ResultQuestionDetail::isCorrect).count();
    }

    public int getWrongCount() {
        return Math.max(0, getAnswerDetails().size() - (int) getCorrectCount());
    }
    
    public String getBloomLevel() {
        if (score == null) return "N/A";
        if (score <= 3) return "Remember";
        if (score <= 5) return "Understand";
        if (score <= 7) return "Apply";
        if (score <= 9) return "Analyze";
        return "Create";
    }
    
    public String getBloomColor() {
        if (score == null) return "gray";
        if (score <= 3) return "red";
        if (score <= 5) return "orange";
        if (score <= 7) return "yellow";
        if (score <= 9) return "blue";
        return "purple";
    }
    
    public boolean isPassed() { return score != null && score >= 6; }
}
