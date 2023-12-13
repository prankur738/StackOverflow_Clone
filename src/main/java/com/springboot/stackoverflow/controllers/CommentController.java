package com.springboot.stackoverflow.controllers;

import com.springboot.stackoverflow.entity.Answer;
import com.springboot.stackoverflow.entity.Comment;
import com.springboot.stackoverflow.entity.Question;
import com.springboot.stackoverflow.entity.User;
import com.springboot.stackoverflow.services.AnswerService;
import com.springboot.stackoverflow.services.CommentService;
import com.springboot.stackoverflow.services.QuestionService;
import com.springboot.stackoverflow.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CommentController {

    private final CommentService commentService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, QuestionService questionService, AnswerService answerService, UserService userService) {
        this.commentService = commentService;
        this.questionService = questionService;
        this.answerService = answerService;
        this.userService = userService;
    }

    @GetMapping("/addComment/{questionId}")
    public String addComment(@PathVariable("questionId") int questionId, Model model) {
        Question question = questionService.findQuestionById(questionId);
        model.addAttribute("comments", new Comment());
        model.addAttribute("addComment", "true");
        if (question == null) return "error";
        model.addAttribute("question", question);
        return "questionPage";
    }

    @PostMapping("/saveComment")
    public String saveComment(@ModelAttribute("comments") Comment comments, @RequestParam("questionId") int questionId) {
        commentService.saveComment(comments, questionId);

        return "redirect:/viewQuestion/" + questionId;
    }

    @GetMapping("/addAnswerComment/{questionId}/{answerID}")
    public String addAnswerComment(@PathVariable("questionId") int questionId,
                                   @PathVariable("answerID") int answerID, Model model) {
        Question question = questionService.findQuestionById(questionId);
        model.addAttribute("comments", new Comment());
        model.addAttribute("addAnswerComment", answerID);
        if (question == null) return "error";
        model.addAttribute("question", question);
        return "questionPage";
    }

    @PostMapping("/saveAnswerComment")
    public String saveAnswerComment(@ModelAttribute("comments") Comment comments,
                                    @RequestParam("answerId") int answerId, @RequestParam("questionId") int questionId) {
        commentService.saveAnswerComment(comments, answerId);

        return "redirect:/viewQuestion/" + questionId;
    }

    @GetMapping("/deleteComment/{commentId}")
    public String deleteComment(@PathVariable("commentId") int commentId) {

        int questionId = commentService.deleteCommentById(commentId);

        return "redirect:/viewQuestion/" + questionId;
    }

    @GetMapping("/editComment/{commentId}")
    public String editComment(@PathVariable("commentId") int commentId, Model model) {

        Comment comment = commentService.findById(commentId);
        Question question = comment.getQuestion();

        model.addAttribute("comments", comment);
        model.addAttribute("editComment", commentId);
        if (question == null) return "error";
        model.addAttribute("question", question);
        return "questionPage";
    }

    @GetMapping("/editAnswerComment/{commentId}/{questionId}")
    public String editAnswerComment(@PathVariable("commentId") int commentId,
                                    @PathVariable("questionId") int questionId, Model model) {

        Comment comment = commentService.findById(commentId);
        Question question = questionService.findQuestionById(questionId);

        model.addAttribute("comments", comment);
        model.addAttribute("editAnswerComment", commentId);
        if (question == null) return "error";
        model.addAttribute("question", question);
        return "questionPage";
    }

    @PostMapping("/updateComment/{commentId}")
    public String updateComment(@ModelAttribute("comments") Comment comments,
                                @RequestParam("questionId") int questionId, @PathVariable("commentId") int commentId) {
        commentService.update(comments, commentId);
        return "redirect:/viewQuestion/" + questionId;
    }

    @PostMapping("/updateAnswerComment/{commentId}")
    public String updateAnswerComment(@ModelAttribute("comments") Comment comments,
                                      @RequestParam("questionId") int questionId,
                                      @PathVariable("commentId") int commentId) {
        commentService.updateCommentOfAnswer(comments, commentId);
        return "redirect:/viewQuestion/" + questionId;
    }
}
