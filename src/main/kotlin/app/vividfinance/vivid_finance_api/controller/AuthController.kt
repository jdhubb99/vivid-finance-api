package app.vividfinance.vivid_finance_api.controller

import app.vividfinance.vivid_finance_api.dto.AuthenticationRequestDTO
import app.vividfinance.vivid_finance_api.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: AuthenticationRequestDTO): ResponseEntity<Any> {
        return try {
            userService.registerUser(
                request.username,
                request.password
            )
            ResponseEntity.ok(mapOf("message" to "User registered successfully"))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }

    // TODO: Implement login endpoint with JWT token generation
}
