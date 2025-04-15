package app.vividfinance.vivid_finance_api.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class TokenService(
    @Value("\${jwt.secret}") private val secret: String,
) {
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(subject: String, expiration: Date, additionalClaims: Map<String, Any> = emptyMap()): String {
        return Jwts.builder()
            .claims(additionalClaims)
            .subject(subject)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expiration)
            .signWith(signingKey)
            .compact()
    }

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}