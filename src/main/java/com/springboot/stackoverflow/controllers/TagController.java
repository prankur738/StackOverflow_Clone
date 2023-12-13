package com.springboot.stackoverflow.controllers;

import com.springboot.stackoverflow.entity.Question;
import com.springboot.stackoverflow.entity.Tag;
import com.springboot.stackoverflow.services.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TagController {
    TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/tags")
    public String processAllTags(
            @RequestParam(value = "page", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "limit", defaultValue = "8") Integer pageSize,
            @RequestParam(value = "sortField", defaultValue = "popular") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(value = "search", required = false) String search,
            Model model) {
        Page<Tag> page = tagService.customTags(pageNo, pageSize, sortField, sortDir, search);

        List<Tag> tags = page.getContent();
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("search", search);

        model.addAttribute("tags", tags);

        return "Tags";
    }

    @GetMapping("/questions/tagged/{tagId}")
    public String processAllQuestions(@PathVariable(name = "tagId") int tagId, Model model) {
        List<Question> tempQuestions = tagService.findAllQuestionsByTag(tagId);
        model.addAttribute("questions", tempQuestions);
        return "QuestionsTagged";
    }


}
