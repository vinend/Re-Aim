package io.github.some_example_name.server.service;

import io.github.some_example_name.server.model.Player;
import io.github.some_example_name.server.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Service to load user-specific data for authentication.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private PlayerRepository playerRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Player player = playerRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        // Return Spring Security User object with player credentials and empty authorities
        return new User(
            player.getUsername(),
            player.getPassword(),
            new ArrayList<>()  // Empty authorities as we're not using role-based access
        );
    }
}
