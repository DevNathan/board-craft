package com.app.boardcraftback.service;

import com.app.boardcraftback.domain.entity.user.User;

public interface UserService {
    User registerUser(String rawEmail, String rawPassword, String nickname, boolean terms);
}
