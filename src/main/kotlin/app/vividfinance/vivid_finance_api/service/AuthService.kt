package app.vividfinance.vivid_finance_api.service

import app.vividfinance.vivid_finance_api.dto.AuthenticationRequestDTO
import app.vividfinance.vivid_finance_api.dto.AuthenticationResponseDTO
import app.vividfinance.vivid_finance_api.repository.RefreshTokenRepository


import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
    private val authManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val tokenService: TokenService,
    private val refreshTokenRepository: RefreshTokenRepository,
    @Value("\${jwt.accessTokenExpiration}") private val accessTokenExpiration: Long = 0,
    @Value("\${jwt.refreshTokenExpiration}") private val refreshTokenExpiration: Long = 0,
) {
    fun authentication(authenticationRequest: AuthenticationRequestDTO): AuthenticationResponseDTO {
        authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authenticationRequest.username,
                authenticationRequest.password
            )
        )

        val user = userDetailsService.loadUserByUsername(authenticationRequest.username)

        val accessToken = createAccessToken(user)
        val refreshToken = createRefreshToken(user)

        refreshTokenRepository.save(refreshToken, user)

        return AuthenticationResponseDTO(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun refreshAccessToken(refreshToken: String): String {
        val username = tokenService.extractUsername(refreshToken)

        return username.let { user ->
            val currentUserDetails = userDetailsService.loadUserByUsername(user)
            val refreshTokenDetails = refreshTokenRepository.findUserDetailsByToken(refreshToken)

            if (currentUserDetails.username == refreshTokenDetails?.username)
                createAccessToken(currentUserDetails)
            else
                throw AuthenticationServiceException("Invalid refresh token")
        }
    }

    private fun createAccessToken(user: UserDetails): String {
        return tokenService.generateToken(
            subject = user.username,
            expiration = Date(System.currentTimeMillis() + accessTokenExpiration)
        )
    }

    private fun createRefreshToken(user: UserDetails): String {
        return tokenService.generateToken(
            subject = user.username,
            expiration = Date(System.currentTimeMillis() + refreshTokenExpiration)
        )
    }
}