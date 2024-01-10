package com.springboot.stackoverflow.controllers;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.springboot.stackoverflow.entity.Question;
import com.springboot.stackoverflow.entity.User;
import com.springboot.stackoverflow.services.QuestionService;
import com.springboot.stackoverflow.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.List;

@Controller
public class UserController {
    private UserService userService;

    private QuestionService questionService;

    @Autowired
    public UserController(UserService userService, QuestionService questionService) {
        this.userService = userService;
        this.questionService = questionService;
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/processUser")
    public String processUser(@Valid @ModelAttribute("username") String userName, BindingResult result,
                              @Valid @ModelAttribute("email") String email,BindingResult result2,
                              @Valid @ModelAttribute("password") String password,BindingResult result3,
                              @Valid @ModelAttribute("confirm-password") String confirmPassword ) {
        String response = userService.processUser(userName, email, password, confirmPassword);



        if (response.equals("errorPassword"))
            return "redirect:/signup?errorPassword";
        else if (response.equals("error"))
            return "redirect:/signup?error";
        else
            return "redirect:/login?success";
    }

    @GetMapping("/userProfile")
    public String userProfile(Model model, @RequestParam(value = "userId", required = false) Integer userId) throws IOException {
        User user = userService.findUserByUserId(userId);

        if (user.getPhotoName() != null) {
            System.out.println("in profile");
            String fileName = user.getPhotoName();
            // Download file from Firebase Storage
            Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("./serviceAccountKey.json"));
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            Blob blob = storage.get(BlobId.of("stack-overflow-clone-857f4.appspot.com", fileName));

            String contentType = user.getPhotoType();
            String base64Image = Base64.getEncoder().encodeToString(blob.getContent());

            user.setPhotoType(contentType);
            user.setPhoto(base64Image);
        }


        model.addAttribute("user", user);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("loggedUser", userService.findByEmail(authentication.getName()));

        return "UserProfile";
    }

    @GetMapping("/users")
    public String showAllUsers(Model model) throws IOException {
        List<User> users = null;
        users = userService.findAllUsers();

        for (User user : users) {
            if (user.getPhotoName() != null) {
                String fileName = user.getPhotoName();
                // Download file from Firebase Storage
                Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("./serviceAccountKey.json"));
                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                Blob blob = storage.get(BlobId.of("stack-overflow-clone-857f4.appspot.com", fileName));

                String contentType = user.getPhotoType();
                String base64Image = Base64.getEncoder().encodeToString(blob.getContent());

                user.setPhotoType(contentType);
                user.setPhoto(base64Image);
            }
        }

        model.addAttribute("users", users);

        return "Users";

    }

    @GetMapping("/editProfile")
    public String editProfile(Model model) throws IOException {
        User user = userService.editUser();

        if (user.getPhotoName() != null) {
            String fileName = user.getPhotoName();
            // Download file from Firebase Storage
            Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("./serviceAccountKey.json"));
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            Blob blob = storage.get(BlobId.of("stack-overflow-clone-857f4.appspot.com", fileName));

            String contentType = user.getPhotoType();
            String base64Image = Base64.getEncoder().encodeToString(blob.getContent());

            user.setPhotoType(contentType);
            user.setPhoto(base64Image);
        }


        model.addAttribute("user", user);
        return "editProfile";

    }

    @GetMapping("/users/saves")
    public String processBookmarkQuestions(Model model) {
        List<Question> bookmarkQuestions = userService.getBookmarkQuestionsByUser();

        model.addAttribute("questions", bookmarkQuestions);
        return "BookmarkQuestion";

    }

    @PostMapping("/updateProfile")
    public String updateProfile(@RequestParam String userName,
                                @RequestParam String country, @RequestParam String title,
                                @RequestParam String about) {
        userService.updateUser(userName, country, title, about);
        return "redirect:/userProfile";
    }

    @GetMapping("/follow")
    public String follow(@RequestParam("follower") String follower,
                         @RequestParam("following") String following) {
        userService.follow(follower, following);

        return "redirect:/userProfile?userId=" + userService.findByEmail(following).getId();
    }

    @GetMapping("/unfollow")
    public String unfollow(@RequestParam("follower") String follower,
                           @RequestParam("following") String following) {
        userService.unfollow(follower, following);

        return "redirect:/userProfile?userId=" + userService.findByEmail(following).getId();
    }

    @GetMapping("/followers")
    public String showFollower(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("user", userService.findByEmail(authentication.getName()));
        model.addAttribute("type", "followers");

        return "follow";
    }

    @GetMapping("/following")
    public String showFollowing(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("user", userService.findByEmail(authentication.getName()));
        model.addAttribute("type", "following");

        return "follow";
    }

    @PostMapping("/saveProfilePic{userId}")
    public String saveProfilePic(@RequestParam("imageName") MultipartFile file,
                                 @PathVariable(value = "userId") int userId) throws IOException {
        userService.saveProfilePic(file, userId);
        return "redirect:/editProfile";
    }

    @GetMapping("/searchUser")
    public String searchUser(@RequestParam(value = "search", required = false) String search, Model model) throws IOException {
        List<User> users = userService.searchUser(search);
        for (User user : users) {
            if (user.getPhotoName() != null) {
                String fileName = user.getPhotoName();
                // Download file from Firebase Storage
                Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("./serviceAccountKey.json"));
                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                Blob blob = storage.get(BlobId.of("stack-overflow-clone-857f4.appspot.com", fileName));

                String contentType = user.getPhotoType();
                String base64Image = Base64.getEncoder().encodeToString(blob.getContent());

                user.setPhotoType(contentType);
                user.setPhoto(base64Image);
            }
        }
        model.addAttribute("users", users);
        return "Users";
    }
}

