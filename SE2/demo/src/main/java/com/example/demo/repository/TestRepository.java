package com.example.demo.repository;

import com.example.demo.model.Course;
import com.example.demo.model.AssessmentType;
import com.example.demo.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByCourse(Course course);
    List<Test> findByCourseId(Long courseId);
    List<Test> findByAssessmentType(AssessmentType assessmentType);
    List<Test> findByCourseIdAndAssessmentType(Long courseId, AssessmentType assessmentType);
    long countByCourse(Course course);
}
