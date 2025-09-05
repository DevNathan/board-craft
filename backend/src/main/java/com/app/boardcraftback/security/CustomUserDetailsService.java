package com.app.boardcraftback.security;

import com.app.boardcraftback.domain.entity.user.Users;
import com.app.boardcraftback.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {
    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String rawEmail) throws UsernameNotFoundException {
        String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase();

        Users user = usersRepository.findByEmailIgnoreCase(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email)
        );

        return new CustomUserDetails(user);
    }
}
