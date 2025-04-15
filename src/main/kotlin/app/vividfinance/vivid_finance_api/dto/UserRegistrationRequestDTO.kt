package app.vividfinance.vivid_finance_api.dto

data class UserRegistrationRequestDTO(
    val username: String,
    val email: String,
    val password: String
)
