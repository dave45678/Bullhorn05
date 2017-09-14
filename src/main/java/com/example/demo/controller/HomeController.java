package com.example.demo.controller;

import com.cloudinary.utils.ObjectUtils;
import com.example.demo.config.CloudinaryConfig;
import com.example.demo.model.Message;
import com.example.demo.model.Role;
import com.example.demo.repository.MessageRepository;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    CloudinaryConfig cloudConfig;

    @Autowired
    private UserService userService;

    @Autowired
    UserRepository userRepository;

    @RequestMapping("/")
    public String listMessages(Model model) {
        model.addAttribute("messages", messageRepository.findAll());
        return "list";
    }

    public User getCurrentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName());
    }

    @GetMapping("/add")
    public String messageForm(Model model) {
        model.addAttribute("message", new Message());
        return "messageform";
    }

    @PostMapping("/process")
    public String processForm(@Valid Message message, BindingResult result, @RequestParam("file")MultipartFile file) {
        if (result.hasErrors() || file.isEmpty()) {
            return "messageform";
        }
        try {
            Map uploadResult = cloudConfig.upload(file.getBytes(),
                    ObjectUtils.asMap("resourcetype", "auto"));
            message.setPhoto(uploadResult.get("url").toString());
            User currentUser = getCurrentUser();
            System.out.println("This is the current user " + currentUser.toString());
            message.setUser(currentUser);
            //message.setPostDate(new Date());
            currentUser.addMessage(message);
            messageRepository.save(message);
        }
        catch (IOException e) {
            System.out.println("BEFORE after clause... ");
            e.printStackTrace();
            return "messageform";
        }
        messageRepository.save(message);
        return "redirect:/";
    }

    @RequestMapping("/detail/{id}")
    public String showMessage(@PathVariable("id") long id, Model model) {
        model.addAttribute("message", messageRepository.findOne(id));
        return "show";
    }

    @RequestMapping("/login")
    public String login(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("errornotif", "Your username and password is invalid");
        }
        if (logout != null) {
            model.addAttribute("logoutnotif", "You have been logout successfully.");
        }
        return "login";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String processRegistrationPage(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        model.addAttribute("user", user);
        if (result.hasErrors()) {
            return "registration";
        }
        else {
            userService.saveUser(user);
            model.addAttribute("registernotif", "User " + user.getUsername() + " successfully created!");
        }
        return "list";
    }

}
