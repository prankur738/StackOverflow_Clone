package com.springboot.stackoverflow.controllers;

import com.springboot.stackoverflow.entity.Question;
import com.springboot.stackoverflow.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {
    private final QuestionService questionService;

    @Autowired
    public HomeController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @RequestMapping("/")
    public String showHome(Model model,
                           @RequestParam(name = "search", required = false) String search,
                           @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                           @RequestParam(name = "pageLimit", defaultValue = "5") int pageLimit) {
        Page<Question> questionsList;
        if (search != null && !search.isEmpty()) {
            questionsList = questionService.searchProducts(search, pageNumber - 1, pageLimit);
        } else {
            questionsList = questionService.findAllQuestions(pageNumber - 1, pageLimit);
        }
        model.addAttribute("questionsList", questionsList.getContent());

        model.addAttribute("totalPages", questionsList.getTotalPages());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("pageLimit", pageLimit);

        model.addAttribute("search", search);

        return "home";
    }

    @GetMapping("/aboutUs")
    public String aboutUs() {
        return "Aboutus";
    }
}
