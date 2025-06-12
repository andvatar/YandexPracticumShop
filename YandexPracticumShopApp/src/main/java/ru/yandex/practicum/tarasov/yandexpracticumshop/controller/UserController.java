package ru.yandex.practicum.tarasov.yandexpracticumshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.UserDto;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.UserService;

@Controller
@RequestMapping("/register")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping()
    public Mono<String> registerPage(Model model) {
        model.addAttribute("userDto", new UserDto());
        return Mono.just("register");
    }

    @PostMapping()
    public Mono<String> register(@ModelAttribute("userDto") UserDto userDto) {
        return userService.registerUser(userDto).thenReturn("redirect:/login");
    }
}
