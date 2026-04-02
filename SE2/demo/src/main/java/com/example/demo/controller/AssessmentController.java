package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/assessments")
public class AssessmentController {

    private final TestRepository testRepository;
    private final CourseRepository courseRepository;
    private final StudentResultRepository resultRepository;
    private final QuestionRepository questionRepository;

    public AssessmentController(TestRepository testRepository, CourseRepository courseRepository,
                                StudentResultRepository resultRepository, QuestionRepository questionRepository) {
        this.testRepository = testRepository;
        this.courseRepository = courseRepository;
        this.resultRepository = resultRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping
    public String list(Model model, @RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String type) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedType", type);

        AssessmentType selectedType = parseType(type);
        List<Test> tests;
        if (courseId != null && selectedType != null) {
            tests = testRepository.findByCourseIdAndAssessmentType(courseId, selectedType);
        } else if (courseId != null) {
            tests = testRepository.findByCourseId(courseId);
        } else if (selectedType != null) {
            tests = testRepository.findByAssessmentType(selectedType);
        } else {
            tests = testRepository.findAll();
        }

        if (search != null && !search.isBlank()) {
            final String q = search.toLowerCase();
            tests = tests.stream().filter(t ->
                    safe(t.getTitle()).contains(q)
                            || safe(t.getDescription()).contains(q)
                            || safe(t.getCourse() != null ? t.getCourse().getName() : null).contains(q))
                    .toList();
        }

        tests = tests.stream()
                .sorted(Comparator
                        .comparing(Test::getAssessmentType)
                        .thenComparing(Test::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Map<Long, Long> qCounts = new HashMap<>();
        for (Test t : tests) {
            if (t.getId() != null) {
                qCounts.put(t.getId(), questionRepository.countByTest(t));
            }
        }

        model.addAttribute("tests", tests);
        model.addAttribute("questionCounts", qCounts);
        model.addAttribute("search", search);
        return "assessments/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("assessmentTypes", AssessmentType.values());
        return "assessments/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam Integer duration,
                         @RequestParam Long courseId,
                         @RequestParam String assessmentType,
                         @RequestParam(required = false) List<String> questionTypes,
                         @RequestParam(required = false) List<String> questionContents,
                         @RequestParam(required = false) List<String> correctAnswers,
                         @RequestParam(required = false) List<String> optionAs,
                         @RequestParam(required = false) List<String> optionBs,
                         @RequestParam(required = false) List<String> optionCs,
                         @RequestParam(required = false) List<String> optionDs,
                         RedirectAttributes ra) {

        Optional<Course> course = courseRepository.findById(courseId);
        if (course.isEmpty()) {
            ra.addFlashAttribute("error", "Course khong ton tai.");
            return "redirect:/assessments/create";
        }

        Test test = new Test();
        test.setTitle(title);
        test.setDescription(description);
        test.setDuration(duration);
        test.setCourse(course.get());
        test.setAssessmentType(AssessmentType.valueOf(assessmentType));
        testRepository.save(test);

        if (questionContents != null) {
            for (int i = 0; i < questionContents.size(); i++) {
                if (!questionContents.get(i).isBlank()) {
                    Question q = new Question();
                    q.setTest(test);
                    q.setContent(questionContents.get(i));

                    String type = (questionTypes != null && i < questionTypes.size()) ? questionTypes.get(i) : "SHORT_ANSWER";
                    q.setQuestionType(QuestionType.valueOf(type));
                    q.setCorrectAnswer(correctAnswers != null && i < correctAnswers.size() ? correctAnswers.get(i) : "");

                    if (QuestionType.MULTIPLE_CHOICE.name().equals(type)) {
                        q.setOptionA(getVal(optionAs, i));
                        q.setOptionB(getVal(optionBs, i));
                        q.setOptionC(getVal(optionCs, i));
                        q.setOptionD(getVal(optionDs, i));
                    }
                    questionRepository.save(q);
                }
            }
        }

        ra.addFlashAttribute("success", "Tao assessment '" + title + "' thanh cong.");
        return "redirect:/assessments";
    }

    private String getVal(List<String> list, int i) {
        return (list != null && i < list.size()) ? list.get(i) : "";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<Test> opt = testRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Test khong ton tai.");
            return "redirect:/assessments";
        }
        model.addAttribute("test", opt.get());
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("assessmentTypes", AssessmentType.values());
        model.addAttribute("questions", questionRepository.findByTestId(id));
        long resultCount = resultRepository.countByTest(opt.get());
        model.addAttribute("resultCount", resultCount);
        return "assessments/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam Integer duration,
                         @RequestParam Long courseId,
                         @RequestParam String assessmentType,
                         @RequestParam(required = false) List<String> questionTypes,
                         @RequestParam(required = false) List<String> questionContents,
                         @RequestParam(required = false) List<String> correctAnswers,
                         @RequestParam(required = false) List<String> optionAs,
                         @RequestParam(required = false) List<String> optionBs,
                         @RequestParam(required = false) List<String> optionCs,
                         @RequestParam(required = false) List<String> optionDs,
                         RedirectAttributes ra) {
        Optional<Test> opt = testRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Test khong ton tai.");
            return "redirect:/assessments";
        }

        Test test = opt.get();
        courseRepository.findById(courseId).ifPresent(test::setCourse);
        test.setTitle(title);
        test.setDescription(description);
        test.setDuration(duration);
        test.setAssessmentType(AssessmentType.valueOf(assessmentType));
        testRepository.save(test);

        questionRepository.deleteAll(questionRepository.findByTestId(id));
        if (questionContents != null) {
            for (int i = 0; i < questionContents.size(); i++) {
                if (!questionContents.get(i).isBlank()) {
                    Question q = new Question();
                    q.setTest(test);
                    q.setContent(questionContents.get(i));

                    String type = (questionTypes != null && i < questionTypes.size()) ? questionTypes.get(i) : "SHORT_ANSWER";
                    q.setQuestionType(QuestionType.valueOf(type));
                    q.setCorrectAnswer(correctAnswers != null && i < correctAnswers.size() ? correctAnswers.get(i) : "");

                    if (QuestionType.MULTIPLE_CHOICE.name().equals(type)) {
                        q.setOptionA(getVal(optionAs, i));
                        q.setOptionB(getVal(optionBs, i));
                        q.setOptionC(getVal(optionCs, i));
                        q.setOptionD(getVal(optionDs, i));
                    }
                    questionRepository.save(q);
                }
            }
        }

        ra.addFlashAttribute("success", "Cap nhat test thanh cong.");
        return "redirect:/assessments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        questionRepository.deleteAll(questionRepository.findByTestId(id));
        testRepository.deleteById(id);
        ra.addFlashAttribute("success", "Da xoa test.");
        return "redirect:/assessments";
    }

    @GetMapping("/{id}/results")
    public String results(@PathVariable Long id, Model model,
                          @RequestParam(required = false) String bloomFilter,
                          @RequestParam(required = false) String passFilter,
                          @RequestParam(required = false) String studentSearch,
                          RedirectAttributes ra) {
        Optional<Test> opt = testRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Test khong ton tai.");
            return "redirect:/assessments";
        }

        Test test = opt.get();
        List<StudentResult> allResults = resultRepository.findByTestId(id);
        List<StudentResult> results = allResults.stream()
                .filter(r -> matchesBloomFilter(r, bloomFilter))
                .filter(r -> matchesPassFilter(r, passFilter))
                .filter(r -> matchesStudentSearch(r, studentSearch))
                .sorted(Comparator
                        .comparing(StudentResult::isPassed)
                        .thenComparing(StudentResult::getScore, Comparator.nullsLast(Double::compareTo))
                        .thenComparing(r -> displayStudentName(r.getStudent()), String.CASE_INSENSITIVE_ORDER))
                .toList();

        long passCount = results.stream().filter(StudentResult::isPassed).count();
        long failCount = results.size() - passCount;
        Double avg = results.isEmpty()
                ? null
                : results.stream().map(StudentResult::getScore).filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0);

        model.addAttribute("test", test);
        model.addAttribute("results", results);
        model.addAttribute("avgScore", avg != null ? String.format("%.1f", avg) : "N/A");
        model.addAttribute("totalCount", results.size());
        model.addAttribute("passCount", passCount);
        model.addAttribute("failCount", failCount);
        model.addAttribute("allResultCount", allResults.size());
        model.addAttribute("bloomFilter", bloomFilter);
        model.addAttribute("passFilter", passFilter);
        model.addAttribute("studentSearch", studentSearch);
        return "assessments/results";
    }

    @GetMapping("/{testId}/results/{resultId}")
    public String resultDetail(@PathVariable Long testId,
                               @PathVariable Long resultId,
                               Model model,
                               RedirectAttributes ra) {
        Optional<Test> testOpt = testRepository.findById(testId);
        Optional<StudentResult> resultOpt = resultRepository.findById(resultId);
        if (testOpt.isEmpty() || resultOpt.isEmpty() || resultOpt.get().getTest() == null
                || !Objects.equals(resultOpt.get().getTest().getId(), testId)) {
            ra.addFlashAttribute("error", "Khong tim thay ket qua can xem.");
            return "redirect:/assessments";
        }

        StudentResult result = resultOpt.get();
        List<ResultQuestionDetail> details = result.getAnswerDetails();
        List<ResultQuestionDetail> wrongDetails = details.stream().filter(detail -> !detail.isCorrect()).toList();

        model.addAttribute("test", testOpt.get());
        model.addAttribute("result", result);
        model.addAttribute("details", details);
        model.addAttribute("wrongDetails", wrongDetails);
        model.addAttribute("correctCount", details.stream().filter(ResultQuestionDetail::isCorrect).count());
        model.addAttribute("wrongCount", wrongDetails.size());
        return "assessments/result-detail";
    }

    private AssessmentType parseType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return AssessmentType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean matchesBloomFilter(StudentResult result, String bloomFilter) {
        return bloomFilter == null || bloomFilter.isBlank() || result.getBloomLevel().equalsIgnoreCase(bloomFilter);
    }

    private boolean matchesPassFilter(StudentResult result, String passFilter) {
        if (passFilter == null || passFilter.isBlank()) {
            return true;
        }
        if ("pass".equalsIgnoreCase(passFilter)) {
            return result.isPassed();
        }
        if ("fail".equalsIgnoreCase(passFilter)) {
            return !result.isPassed();
        }
        return true;
    }

    private boolean matchesStudentSearch(StudentResult result, String studentSearch) {
        if (studentSearch == null || studentSearch.isBlank()) {
            return true;
        }
        String q = studentSearch.trim().toLowerCase();
        User student = result.getStudent();
        if (student == null) {
            return false;
        }
        return String.valueOf(student.getId()).contains(q)
                || safe(student.getUsername()).contains(q)
                || safe(student.getFullName()).contains(q);
    }

    private String displayStudentName(User student) {
        if (student == null) {
            return "";
        }
        return student.getFullName() != null && !student.getFullName().isBlank()
                ? student.getFullName()
                : safe(student.getUsername());
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
