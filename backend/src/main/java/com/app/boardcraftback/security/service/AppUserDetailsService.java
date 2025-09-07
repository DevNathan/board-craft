package com.app.boardcraftback.security.service;

import com.app.boardcraftback.domain.entity.user.User;
import com.app.boardcraftback.repository.UserRepository;
import com.app.boardcraftback.security.userDetail.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String rawEmail) throws UsernameNotFoundException {
        String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email)
        );

        return new AppUserDetails(user);
    }
}
