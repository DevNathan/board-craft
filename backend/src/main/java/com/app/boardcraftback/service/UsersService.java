package com.app.boardcraftback.service;

import com.app.boardcraftback.domain.entity.user.Users;

public interface UsersService {
    Users registerUser(String rawEmail, String rawPassword, String nickname, boolean terms);
}
