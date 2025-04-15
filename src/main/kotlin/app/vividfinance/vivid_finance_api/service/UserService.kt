package app.vividfinance.vivid_finance_api.service

import app.vividfinance.vivid_finance_api.model.User
import app.vividfinance.vivid_finance_api.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {
    fun registerUser(username: String, rawPassword: String): User {
        // if the user already exists by username
        if (userRepository.findByUsername(username) != null) {
            throw IllegalArgumentException("Username already exists!")
        }
        val hashedPassword = passwordEncoder.encode(rawPassword)
        val user = User(
            username = username,
            passwordHash = hashedPassword,
            registrationDate = OffsetDateTime.now()
        )
        return userRepository.save(user)
    }
}