package app.vividfinance.vivid_finance_api.filter

import app.vividfinance.vivid_finance_api.service.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val userDetailsService: UserDetailsService,
    private val tokenService: TokenService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader: String? = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (authHeader != null && authHeader.startsWith(OAuth2AccessToken.TokenType.BEARER.value)) {
            try {
                val token: String = authHeader.substringAfter(OAuth2AccessToken.TokenType.BEARER.value).trimStart()
                val username: String = tokenService.extractUsername(token)

                if (SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(username)

                    if (username == userDetails.username) {
                        val authToken = UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.authorities
                        )
                        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authToken
                    }
                }
            } catch (ex: Exception) {
                response.writer.write(
                    """{"error": "Filter Authorization error:
                        |${ex.message ?: "unknown error"}"}""".trimMargin()
                )
            }
        }

        filterChain.doFilter(request, response)
    }
}