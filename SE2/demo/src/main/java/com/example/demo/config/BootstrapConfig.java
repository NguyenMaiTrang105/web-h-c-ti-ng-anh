package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

@Configuration
public class BootstrapConfig {

    private final Random random = new Random();

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               CourseRepository courseRepository,
                               EnrollmentRepository enrollmentRepository,
                               ErrorTypeRepository errorTypeRepository,
                               ErrorTestMappingRepository errorTestMappingRepository,
                               TestRepository testRepository,
                               QuestionRepository questionRepository,
                               StudentResultRepository studentResultRepository,
                               StudentErrorRepository studentErrorRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@university.edu");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setStatus("ACTIVE");
                admin.setFullName("Le Quang Que (Admin)");
                admin.setPhone("0901234567");
                userRepository.save(admin);

                User staff = new User();
                staff.setUsername("staff");
                staff.setEmail("staff@university.edu");
                staff.setPassword(passwordEncoder.encode("staff123"));
                staff.setRole(Role.ACADEMIC_STAFF);
                staff.setStatus("ACTIVE");
                staff.setFullName("Nguyen Van Nhan");
                userRepository.save(staff);

                String[] names = {"Tran Anh Tuan", "Pham Thi Mai", "Le Gia Hung", "Vu Nhat Minh", "Hoang Kim Chi", "Do Bao Ngoc"};
                for (int i = 1; i <= 6; i++) {
                    User student = new User();
                    student.setUsername("student" + i);
                    student.setEmail("student" + i + "@university.edu");
                    student.setPassword(passwordEncoder.encode("student123"));
                    student.setRole(Role.STUDENT);
                    student.setStatus("ACTIVE");
                    student.setFullName(names[i - 1]);
                    userRepository.save(student);
                }
            }

            if (courseRepository.count() == 0) {
                Course c1 = new Course();
                c1.setName("IELTS Academic Writing Task 2");
                c1.setDescription("Hoc cach viet luan Academic IELTS dat band 7.0+");
                c1.setStartDate(LocalDate.now().minusWeeks(2));
                c1.setEndDate(LocalDate.now().plusMonths(3));
                courseRepository.save(c1);

                Course c2 = new Course();
                c2.setName("Communicative Business English");
                c2.setDescription("Tieng Anh giao tiep chuyen sau cho moi truong van phong quoc te.");
                c2.setStartDate(LocalDate.now().plusWeeks(1));
                c2.setEndDate(LocalDate.now().plusMonths(6));
                courseRepository.save(c2);

                Course c3 = new Course();
                c3.setName("TOEIC Intensive 800+");
                c3.setDescription("Luyen de TOEIC than toc, nam vung meo thi dat diem cao.");
                c3.setStartDate(LocalDate.now().minusMonths(1));
                c3.setEndDate(LocalDate.now().plusMonths(2));
                courseRepository.save(c3);

                List<User> students = userRepository.findByRole(Role.STUDENT);
                for (User s : students) {
                    Enrollment e = new Enrollment();
                    e.setStudent(s);
                    e.setCourse(c1);
                    e.setStatus("APPROVED");
                    e.setEnrolledAt(LocalDateTime.now().minusDays(random.nextInt(10)));
                    enrollmentRepository.save(e);

                    if (random.nextBoolean()) {
                        Enrollment e2 = new Enrollment();
                        e2.setStudent(s);
                        e2.setCourse(c3);
                        e2.setStatus(random.nextBoolean() ? "PENDING" : "APPROVED");
                        e2.setEnrolledAt(LocalDateTime.now().minusDays(random.nextInt(5)));
                        enrollmentRepository.save(e2);
                    }
                }

                ErrorType et1 = new ErrorType();
                et1.setName("Grammar: Subject-Verb Agreement");
                et1.setDescription("Mac loi chia dong tu khong khop voi chu ngu so it hoac so nhieu.");
                errorTypeRepository.save(et1);

                ErrorType et2 = new ErrorType();
                et2.setName("Pronunciation: Final Sounds /s/ & /z/");
                et2.setDescription("Mat am duoi hoac phat am sai am gio.");
                errorTypeRepository.save(et2);

                Test t1 = new Test();
                t1.setTitle("Midterm Writing Challenge");
                t1.setDescription("Kiem tra ky nang viet luan hoc thuat.");
                t1.setDuration(60);
                t1.setCourse(c1);
                t1.setAssessmentType(AssessmentType.COURSE_ASSESSMENT);
                testRepository.save(t1);

                Test remedial = new Test();
                remedial.setTitle("Grammar Recovery Drill");
                remedial.setDescription("Bai test bo sung cho hoc sinh dang gap loi ngu phap lap lai.");
                remedial.setDuration(25);
                remedial.setCourse(c1);
                remedial.setAssessmentType(AssessmentType.REMEDIAL_TEST);
                testRepository.save(remedial);

                seedQuestion(questionRepository, t1, QuestionType.SHORT_ANSWER,
                        "Chia dong tu: The team (be) _______ winning.", "is",
                        null, null, null, null);
                seedQuestion(questionRepository, t1, QuestionType.MULTIPLE_CHOICE,
                        "Choose the sentence with correct agreement.", "B",
                        "The students studies hard.", "The students study hard.", "The students studying hard.", "The students studys hard.");
                seedQuestion(questionRepository, t1, QuestionType.SHORT_ANSWER,
                        "Complete: Each of the students _____ responsible.", "is",
                        null, null, null, null);
                seedQuestion(questionRepository, t1, QuestionType.MULTIPLE_CHOICE,
                        "Pick the best verb: My advice _____ practical.", "A",
                        "is", "are", "be", "being");
                seedQuestion(questionRepository, remedial, QuestionType.MULTIPLE_CHOICE,
                        "Choose the correct sentence.", "C",
                        "She go to class every day.", "The team are winning.", "Each student is ready.", "My friends studies late.");
                seedQuestion(questionRepository, remedial, QuestionType.SHORT_ANSWER,
                        "Fill in the blank: The list of items _____ on the desk.", "is",
                        null, null, null, null);

                ErrorTestMapping mapping = new ErrorTestMapping();
                mapping.setErrorType(et1);
                mapping.setTest(remedial);
                errorTestMappingRepository.save(mapping);

                for (int i = 0; i < Math.min(3, students.size()); i++) {
                    StudentResult res = new StudentResult();
                    res.setStudent(students.get(i));
                    res.setTest(t1);
                    res.setScore(7.5 + i * 0.5);
                    res.setSubmittedAt(LocalDateTime.now().minusHours(random.nextInt(48)));
                    studentResultRepository.save(res);

                    StudentError se = new StudentError();
                    se.setStudent(students.get(i));
                    se.setErrorType(et1);
                    se.setCreatedAt(LocalDateTime.now().minusHours(i * 4L));
                    studentErrorRepository.save(se);
                }
            }

            backfillLegacyResultDetails(studentResultRepository, questionRepository);
        };
    }

    private void seedQuestion(QuestionRepository questionRepository,
                              Test test,
                              QuestionType type,
                              String content,
                              String correctAnswer,
                              String optionA,
                              String optionB,
                              String optionC,
                              String optionD) {
        Question question = new Question();
        question.setTest(test);
        question.setQuestionType(type);
        question.setContent(content);
        question.setCorrectAnswer(correctAnswer);
        question.setOptionA(optionA);
        question.setOptionB(optionB);
        question.setOptionC(optionC);
        question.setOptionD(optionD);
        questionRepository.save(question);
    }

    private void backfillLegacyResultDetails(StudentResultRepository studentResultRepository,
                                             QuestionRepository questionRepository) {
        for (StudentResult result : studentResultRepository.findAll()) {
            if (result.hasAnswerDetails()) {
                continue;
            }
            Long testId = result.getTest() != null ? result.getTest().getId() : null;
            if (testId == null) {
                continue;
            }
            List<Question> questions = questionRepository.findByTestId(testId);
            if (questions.isEmpty()) {
                continue;
            }
            result.setAnswerDetails(buildSampleDetails(questions, result.getScore()));
            studentResultRepository.save(result);
        }
    }

    private List<ResultQuestionDetail> buildSampleDetails(List<Question> questions, Double score) {
        List<ResultQuestionDetail> details = new ArrayList<>();
        int totalQuestions = questions.size();
        int expectedCorrect = totalQuestions == 0 || score == null
                ? 0
                : Math.max(0, Math.min(totalQuestions, (int) Math.round((score / 10.0) * totalQuestions)));

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            boolean isCorrect = i < expectedCorrect;
            ResultQuestionDetail detail = new ResultQuestionDetail();
            detail.setQuestionId(question.getId());
            detail.setQuestionNumber(i + 1);
            detail.setQuestionType(question.getQuestionType().name());
            detail.setQuestionContent(question.getContent());
            detail.setCorrectAnswer(question.getCorrectAnswer());
            detail.setStudentAnswer(generateSampleAnswer(question, isCorrect));
            detail.setCorrect(isCorrect);
            detail.setOptions(Stream.of(
                            optionLabel("A", question.getOptionA()),
                            optionLabel("B", question.getOptionB()),
                            optionLabel("C", question.getOptionC()),
                            optionLabel("D", question.getOptionD()))
                    .filter(java.util.Objects::nonNull)
                    .toList());
            details.add(detail);
        }
        return details;
    }

    private String generateSampleAnswer(Question question, boolean correct) {
        if (correct) {
            return question.getCorrectAnswer();
        }
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            for (String choice : List.of("A", "B", "C", "D")) {
                if (!choice.equalsIgnoreCase(question.getCorrectAnswer())) {
                    return choice;
                }
            }
        }
        return "Sample incorrect answer";
    }

    private String optionLabel(String key, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return key + ". " + value;
    }
}
