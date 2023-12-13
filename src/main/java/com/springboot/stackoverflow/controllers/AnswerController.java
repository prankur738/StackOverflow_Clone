package com.springboot.stackoverflow.controllers;

import com.springboot.stackoverflow.entity.Answer;
import com.springboot.stackoverflow.entity.Question;
import com.springboot.stackoverflow.services.AnswerService;
import com.springboot.stackoverflow.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Controller
public class AnswerController {

    AnswerService answerService;
    QuestionService questionService;

    @Autowired
    public AnswerController(AnswerService answerService, QuestionService questionService) {
        this.answerService = answerService;
        this.questionService = questionService;
    }

    @PostMapping("/addAnswer{questionId}")
    public String processAddAnswer(@RequestParam("imageName") MultipartFile file,
                                   @PathVariable(name = "questionId") Integer questionId,
                                   @RequestParam("content") String content) throws IOException {


        answerService.addAnswer(questionId, content, file);

        return "redirect:/viewQuestion/{questionId}";
    }

    @GetMapping("/editAnswer/{questionId}/{answerId}")
    public String processEditAnswer(@PathVariable(name = "answerId") Integer answerId,
                                    @PathVariable(name = "questionId") Integer questionId, Model model) {
        Answer tempAnswer = answerService.editAnswer(answerId);
        Question question = questionService.findQuestionById(questionId);
        model.addAttribute("question", question);
        model.addAttribute("editAnswer", tempAnswer);

        return "questionPage";
    }

    @GetMapping("/updateAnswer/{questionId}/{answerId}")
    public String processUpdatedAnswer(@ModelAttribute("editedContent") String editedContent,
                                       @PathVariable(name = "answerId") Integer answerId) {
        answerService.updateAnswer(editedContent, answerId);

        return "redirect:/viewQuestion/{questionId}";
    }

    @GetMapping("/deleteAnswer/{questionId}/{answerId}")
    public String processDeleteAnswer(@PathVariable Integer answerId) {
        answerService.deleteAnswer(answerId);

        return "redirect:/viewQuestion/{questionId}";
    }
}
