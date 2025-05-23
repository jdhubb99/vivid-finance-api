package app.vividfinance.vivid_finance_api.model

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "users", schema = "vivid_data")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id", unique = true, nullable = false, insertable = false, updatable = false)
    val userId: Int? = null,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(unique = true)
    val email: String? = null,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Column(name = "registration_date", nullable = false)
    val registrationDate: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "default_currency", length = 3)
    val defaultCurrency: String = "USD",

    @Column(name = "default_language", length = 10)
    val defaultLanguage: String = "en",

    @Type(JsonBinaryType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notification_preferences", columnDefinition = "JSONB")
    val notificationPreferences: String? = null // JSON stored as String
)
