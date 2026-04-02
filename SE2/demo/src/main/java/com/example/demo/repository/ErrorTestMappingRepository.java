package com.example.demo.repository;

import com.example.demo.model.ErrorTestMapping;
import com.example.demo.model.ErrorType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ErrorTestMappingRepository extends JpaRepository<ErrorTestMapping, Long> {
    Optional<ErrorTestMapping> findByErrorType(ErrorType errorType);
    Optional<ErrorTestMapping> findByErrorTypeId(Long errorTypeId);
    List<ErrorTestMapping> findByTestId(Long testId);
    List<ErrorTestMapping> findAll();
}
