package app.vividfinance.vivid_finance_api.service


import app.vividfinance.vivid_finance_api.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class JwtUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user =
            userRepository.findByUsername(username) ?: throw UsernameNotFoundException("User $username not found!")

        return User.builder()
            .username(user.username)
            .password(user.passwordHash)
            .build()
    }
}