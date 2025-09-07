package com.app.boardcraftback.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account")
public class AccountViewController {

    @GetMapping
    public String goto_account() {
        return "account/index";
    }
}
