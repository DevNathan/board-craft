package com.app.boardcraftback.controller;

import com.app.boardcraftback.api.v1.dto.auth.RegisterRequest;
import com.app.boardcraftback.service.UsersService;
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

    private final UsersService usersService;

    @GetMapping("/signup")
    public String signup() {
        return "auth/signup";
    }

    @GetMapping("/signin")
    public String signin() {
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

        // 2) 서비스 호출
        try {
            usersService.registerUser(
                    form.email(),
                    form.password(),
                    form.nickname(),
                    form.terms()
            );
        } catch (FieldValidationException ve) {

            // 서비스에서 올린 필드별 에러 맵(email/nickname/terms 등)
            model.addAttribute("fieldErrors", ve.getErrors());
            return "auth/signup";
        } catch (IllegalArgumentException iae) {
            // 약관 등 비즈 규칙 일반 예외
            var map = new HashMap<String, String>();
            map.put("terms", iae.getMessage());
            model.addAttribute("fieldErrors", map);
            return "auth/signup";
        }

        // 3) 성공: 로그인 페이지로 유도 (자동 로그인 원하면 아래 보너스 참고)
        return "redirect:/auth/signin?registered=1";
    }

    private static HashMap<String, String> errorsFromBinding(BindingResult binding) {
        var map = new HashMap<String, String>();
        binding.getFieldErrors().forEach(fe -> map.put(fe.getField(), fe.getDefaultMessage()));
        return map;
    }
}
