package app.vividfinance.vivid_finance_api.dto

data class AuthenticationResponseDTO(
    val accessToken: String,
    val refreshToken: String
)
