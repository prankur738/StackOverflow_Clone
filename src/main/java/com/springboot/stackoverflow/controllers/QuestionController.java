package com.springboot.stackoverflow.controllers;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.springboot.stackoverflow.entity.Answer;
import com.springboot.stackoverflow.entity.Question;
import com.springboot.stackoverflow.entity.Tag;
import com.springboot.stackoverflow.entity.User;
import com.springboot.stackoverflow.services.QuestionService;
import com.springboot.stackoverflow.services.UserService;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Controller
public class QuestionController {

    QuestionService questionService;

    UserService userService;

    @Autowired
    public QuestionController(QuestionService questionService, UserService userService) {
        this.questionService = questionService;
        this.userService = userService;
    }

    @GetMapping("/questions/ask")
    public String processAskQuestions(Model model) {
        Question question = new Question();
        Tag tag = new Tag();
        model.addAttribute("question", question);
        model.addAttribute("tag", tag);
        return "AskQuestion";
    }

    @PostMapping("/saveQuestion")
    public String processSaveQuestion(@RequestParam("imageName") MultipartFile file,
                                      @ModelAttribute("question") Question newQuestion,
                                      @ModelAttribute("tag") Tag newTag) throws IOException {
        questionService.saveQuestion(newQuestion, newTag, file);

        return "redirect:/";
    }

    @GetMapping("/viewQuestion/{questionId}")
    public String viewQuestion(@PathVariable("questionId") Integer questionId, Model model) throws IOException {
        Question question = questionService.findQuestionById(questionId);
        List<Answer> answers = question.getAnswers();

        for (Answer answer : answers) {
            if (answer.getPhotoName() != null) {
                String fileName = answer.getPhotoName();
                // Download file from Firebase Storage
                Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("./serviceAccountKey.json"));
                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                Blob blob = storage.get(BlobId.of("stack-overflow-clone-857f4.appspot.com", fileName));

                String contentType = answer.getPhotoType();
                String base64Image = Base64.getEncoder().encodeToString(blob.getContent());

                answer.setPhotoType(contentType);
                answer.setPhoto(base64Image);
            }
        }


        if (question.getPhotoName() != null) {
            String fileName = question.getPhotoName();
            // Download file from Firebase Storage
            Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("./serviceAccountKey.json"));
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            Blob blob = storage.get(BlobId.of("stack-overflow-clone-857f4.appspot.com", fileName));

            String contentType = question.getPhotoType();
            String base64Image = Base64.getEncoder().encodeToString(blob.getContent());

            question.setPhotoType(contentType);
            question.setPhoto(base64Image);

        }

        model.addAttribute("question", question);
        if (question == null) return "error";
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String convertedHtml = renderer.render(parser.parse(question.getContent()));
        question.setContent(convertedHtml);
        model.addAttribute("question", question);
        String add = "true";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        List<Question> questionList = user.getSavedQuestions();
        for (Question tempQuestion : questionList) {
            if (tempQuestion.getId() == questionId) {
                add = "false";
                break;
            }
        }
        model.addAttribute("add", add);

        return "questionPage";
    }

    @GetMapping("/editQuestion/{questionId}")
    public String processEditQuestion(@PathVariable("questionId") Integer questionId, Model model) {
        Question question = questionService.editQuestion(questionId);
        model.addAttribute("question", question);
        model.addAttribute("isEdited", "true");

        return "questionPage";
    }

    @GetMapping("/updateQuestion/{questionId}")
    public String processUpdatedQuestion(@PathVariable("questionId") Integer questionId,
                                         @ModelAttribute("editedContent") String editedContent) {
        questionService.updateQuestion(questionId, editedContent);

        return "redirect:/viewQuestion/{questionId}";
    }

    @GetMapping("/deleteQuestion/{questionId}")
    public String deleteQuestion(@PathVariable("questionId") Integer questionId) {
        questionService.deleteQuestion(questionId);

        return "redirect:/";
    }

    @GetMapping("/processVote")
    public String processVote(@RequestParam("vote") Integer vote,
                              @RequestParam("type") String type,
                              @RequestParam("questionId") Integer questionId,
                              @RequestParam(value = "answerId", required = false) Integer answerId) {
        System.out.println(vote);
        System.out.println(type);
        System.out.println(questionId);
        System.out.println(answerId);
        questionService.votingSystem(vote, type, questionId, answerId);

        return "redirect:/viewQuestion/" + questionId;
    }

    @GetMapping("/question/bookmark/{questionId}/add/{add}")
    public String processBookmarkQuestion(@PathVariable(value = "questionId") Integer questionId,
                                          @PathVariable(value = "add") String add) {
        if (add.equals("true")) {
            questionService.bookmarkQuestion(questionId);
        } else {
            questionService.removeBookmarkQuestion(questionId);
        }
        return "redirect:/viewQuestion/{questionId}";
    }

    @GetMapping("/acceptAnswer")
    public String acceptAnswer(@RequestParam(value = "questionId") Integer questionId,
                               @RequestParam(value = "answerId") Integer answerId) {
        questionService.acceptAnswer(questionId, answerId);

        return "redirect:/viewQuestion/" + questionId;
    }

    @GetMapping("/question/{question_id}/activity")
    public String showQuestionActivity(@PathVariable("question_id") Integer question_id,
                                       Model model) {
        System.out.println("enter controller");
        List<Object> list = questionService.getSortedCommentsAndAnswers(question_id);
        model.addAttribute("list", list);
        System.out.println("abc" + list);

        System.out.println("exit controller");

        return "questionActivity";
    }
}
