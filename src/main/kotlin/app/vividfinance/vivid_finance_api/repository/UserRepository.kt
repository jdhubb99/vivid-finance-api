package app.vividfinance.vivid_finance_api.repository

import app.vividfinance.vivid_finance_api.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
}