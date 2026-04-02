package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

public class ResultQuestionDetail {
    private Long questionId;
    private Integer questionNumber;
    private String questionType;
    private String questionContent;
    private String correctAnswer;
    private String studentAnswer;
    private boolean correct;
    private List<String> options = new ArrayList<>();

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Integer getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options != null ? options : new ArrayList<>(); }
}
