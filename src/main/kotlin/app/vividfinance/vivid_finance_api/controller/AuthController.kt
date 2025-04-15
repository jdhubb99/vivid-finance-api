package app.vividfinance.vivid_finance_api.controller

import app.vividfinance.vivid_finance_api.dto.AuthenticationRequestDTO
import app.vividfinance.vivid_finance_api.dto.AuthenticationResponseDTO
import app.vividfinance.vivid_finance_api.dto.RefreshTokenRequestDTO
import app.vividfinance.vivid_finance_api.dto.TokenResponseDTO
import app.vividfinance.vivid_finance_api.service.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping
    fun authenticate(
        @RequestBody request: AuthenticationRequestDTO
    ): AuthenticationResponseDTO =
        authService.authentication(request)

    @PostMapping("/refresh")
    fun refreshAccessToken(
        @RequestBody request: RefreshTokenRequestDTO
    ): TokenResponseDTO = TokenResponseDTO(token = authService.refreshAccessToken(request.token))
}
