package app.vividfinance.vivid_finance_api.controller

import app.vividfinance.vivid_finance_api.dto.AuthenticationRequestDTO
import app.vividfinance.vivid_finance_api.dto.AuthenticationResponseDTO
import app.vividfinance.vivid_finance_api.dto.RefreshTokenRequestDTO
import app.vividfinance.vivid_finance_api.dto.TokenResponseDTO
import app.vividfinance.vivid_finance_api.service.AuthService
import app.vividfinance.vivid_finance_api.service.UserService
import io.jsonwebtoken.security.SignatureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService
) {
    @PostMapping("/refresh")
    fun refreshAccessToken(
        @RequestBody request: RefreshTokenRequestDTO
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(RefreshTokenRequestDTO(token = authService.refreshAccessToken(request.token)))
        } catch (ex: SignatureException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("There was a problem with your token")
        }
    }

    @PostMapping("/register")
    fun registerUser(
        @RequestBody request: AuthenticationRequestDTO
    ): ResponseEntity<Any> {
        userService.registerUser(username = request.username, rawPassword = request.password)
        return ResponseEntity("Registration Successful", HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun authenticate(
        @RequestBody request: AuthenticationRequestDTO
    ): ResponseEntity<AuthenticationResponseDTO> =
        ResponseEntity.ok(authService.authentication(request))
}
