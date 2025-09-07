package com.app.boardcraftback.controller;

import com.app.boardcraftback.domain.dto.auth.RegisterRequest;
import com.app.boardcraftback.service.user.UserService;
import com.app.boardcraftback.support.error.FieldValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthViewController {

    private final UserService userService;

    @GetMapping("/signup")
    public String gotoSignup(Model model) {
        model.addAttribute("form", new RegisterRequest("", "", "", false));
        return "auth/signup";
    }

    @GetMapping("/signin")
    public String goto_signin() {
        return "auth/signin";
    }

    @PostMapping("/signup")
    public String signupSubmit(
            @Valid @ModelAttribute("form") RegisterRequest form,
            BindingResult binding,
            Model model
    ) {
        // 벨리데이션 체크
        if (binding.hasErrors()) {
            model.addAttribute("fieldErrors", errorsFromBinding(binding));
            return "auth/signup";
        }

        // 서비스 호출
        try {
            userService.registerUser(
                    form.email(),
                    form.password(),
                    form.nickname(),
                    form.terms()
            );
        } catch (FieldValidationException ve) {
            ve.getErrors().forEach((field, msg) ->
                    binding.rejectValue(field, "business", msg)
            );
            return "auth/signup";
        }

        return "redirect:/auth/signin?registered=1";
    }

    private static HashMap<String, String> errorsFromBinding(BindingResult binding) {
        var map = new HashMap<String, String>();
        binding.getFieldErrors().forEach(fe -> map.put(fe.getField(), fe.getDefaultMessage()));
        return map;
    }
}
