package app.vividfinance.vivid_finance_api.integration

import app.vividfinance.vivid_finance_api.dto.AuthenticationRequestDTO
import app.vividfinance.vivid_finance_api.dto.AuthenticationResponseDTO
import app.vividfinance.vivid_finance_api.dto.RefreshTokenRequestDTO
import app.vividfinance.vivid_finance_api.dto.TokenResponseDTO
import app.vividfinance.vivid_finance_api.integration.config.PostgresTestcontainersConfig
import app.vividfinance.vivid_finance_api.service.TokenService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.Test


@Import(PostgresTestcontainersConfig::class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Value("\${jwt.expiredToken}")
    private lateinit var oldToken: String

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoSpyBean
    private lateinit var tokenService: TokenService

    @MockitoSpyBean
    private lateinit var userDetailsService: UserDetailsService

    @Test
    fun `access secured endpoint with new token from the refresh token after token expiration`() {
        val authRequest = AuthenticationRequestDTO("email-1@gmail.com", "pass1")
        var jsonRequest = jacksonObjectMapper().writeValueAsString(authRequest)

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        ).andExpect(status().isCreated)

        var response = mockMvc.perform(
            post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty).andReturn().response.contentAsString

        val authResponse = jacksonObjectMapper().readValue(response, AuthenticationResponseDTO::class.java)

        // access secured endpoint
        mockMvc.perform(
            get("/api/hello")
                .header("Authorization", "Bearer ${authResponse.accessToken}")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("Hello, authorized user!"))

        // simulate access token expiration
        `when`(tokenService.extractUsername(authResponse.accessToken))
            .thenThrow(ExpiredJwtException::class.java)

        mockMvc.perform(
            get("/api/hello")
                .header("Authorization", "Bearer ${authResponse.accessToken}")
        )
            .andExpect(status().isForbidden)

        // create a new access token from the refresh token
        val refreshTokenRequest = RefreshTokenRequestDTO(authResponse.refreshToken)
        jsonRequest = jacksonObjectMapper().writeValueAsString(refreshTokenRequest)

        response = mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty).andReturn().response.contentAsString

        val newAccessToken = jacksonObjectMapper().readValue(response, TokenResponseDTO::class.java)

        reset(tokenService)

        // access secured endpoint with the new token
        mockMvc.perform(
            get("/api/hello")
                .header("Authorization", "Bearer ${newAccessToken.token}")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("Hello, authorized user!"))
    }

    @Test
    fun `should return forbidden for unauthenticated user`() {
        val authRequest = AuthenticationRequestDTO("some-user", "pass1")
        val jsonRequest = jacksonObjectMapper().writeValueAsString(authRequest)

        mockMvc.perform(
            post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `refresh token with invalid refresh token should return unauthorized`() {
        val jsonRequest = jacksonObjectMapper().writeValueAsString(
            RefreshTokenRequestDTO(
                oldToken
            )
        )

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should return forbidden for tampered refresh token`() {
        val authRequest = AuthenticationRequestDTO("email-2@gmail.com", "pass2")
        var jsonRequest = jacksonObjectMapper().writeValueAsString(authRequest)

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        ).andExpect(status().isCreated)

        val response = mockMvc.perform(
            post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty).andReturn().response.contentAsString

        val authResponse = jacksonObjectMapper().readValue(response, AuthenticationResponseDTO::class.java)

        val refreshTokenRequest = RefreshTokenRequestDTO(authResponse.refreshToken)
        jsonRequest = jacksonObjectMapper().writeValueAsString(refreshTokenRequest)

        `when`(userDetailsService.loadUserByUsername("email-2@gmail.com"))
            .thenReturn(User("email-1@gmail.com", "pass1", ArrayList()))

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should return forbidden for tampered token`() {
        val authRequest = AuthenticationRequestDTO("email-3@gmail.com", "pass3")
        val jsonRequest = jacksonObjectMapper().writeValueAsString(authRequest)

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        ).andExpect(status().isCreated)

        val response = mockMvc.perform(
            post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty).andReturn().response.contentAsString

        val authResponse = jacksonObjectMapper().readValue(response, AuthenticationResponseDTO::class.java)

        `when`(userDetailsService.loadUserByUsername("email-3@gmail.com"))
            .thenReturn(User("email-2@gmail.com", "pass2", ArrayList()))

        mockMvc.perform(
            get("/api/hello")
                .header("Authorization", "Bearer ${authResponse.accessToken}")
        )
            .andExpect(status().isForbidden)
    }
}