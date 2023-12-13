package com.springboot.stackoverflow.controllers;

import com.springboot.stackoverflow.services.BadgeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BadgeController {
    BadgeService badgeService;

    BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping("/createBadge")
    public String createBadge() {
        return "createBadge";
    }

    @PostMapping("/processBadge")
    public String processBadge(@RequestParam String badgeName, @RequestParam String badgeDescription,
                               @RequestParam int reputationRequired) {
        badgeService.createBadge(badgeName, badgeDescription, reputationRequired);
        return "redirect:/";
    }

    @GetMapping("/achievement")
    public String showAchievement(Model model) {
        model.addAttribute("badgeList", badgeService.getBadgeList());
        return "achievement";
    }
}
