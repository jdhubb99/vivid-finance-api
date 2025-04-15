package app.vividfinance.vivid_finance_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@EnableWebSecurity
@SpringBootApplication
class VividFinanceApiApplication

fun main(args: Array<String>) {
    runApplication<VividFinanceApiApplication>(*args)
}
