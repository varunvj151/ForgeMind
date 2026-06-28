package com.forgemind.modules.auth.security;

import com.forgemind.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security {@link UserDetailsService} implementation.
 *
 * <p>Loads a {@link com.forgemind.modules.auth.entity.User} from the database by
 * username. The returned {@link UserDetails} is used by:
 * <ul>
 *   <li>{@link JwtAuthenticationFilter} — to hydrate the {@link org.springframework.security.core.Authentication}</li>
 *   <li>{@link org.springframework.security.authentication.dao.DaoAuthenticationProvider} — to verify credentials on login</li>
 * </ul>
 *
 * <p>Placing this in a separate {@code @Service} (rather than an inline {@code @Bean}
 * in {@link com.forgemind.config.SecurityConfig}) breaks the circular dependency:
 * {@code SecurityConfig → JwtAuthenticationFilter → UserDetailsService → SecurityConfig}.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));
    }
}
