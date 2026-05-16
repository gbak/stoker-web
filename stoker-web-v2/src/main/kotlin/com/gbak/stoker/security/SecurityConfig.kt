package com.gbak.stoker.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            authorizeHttpRequests {
                // Public: static resources, WebSocket, and read-only API
                authorize("/", permitAll)
                authorize("/index.html", permitAll)
                authorize("/ws/**", permitAll)
                authorize("/css/**", permitAll)
                authorize("/js/**", permitAll)
                authorize("/assets/**", permitAll)
                authorize(HttpMethod.GET, "/api/v1/status", permitAll)
                authorize(HttpMethod.GET, "/api/v1/devices", permitAll)
                authorize(HttpMethod.GET, "/api/v1/devices/**", permitAll)
                authorize(HttpMethod.GET, "/api/v1/configuration", permitAll)
                authorize(HttpMethod.GET, "/api/v1/logs/**", permitAll)

                // Protected: all write operations
                authorize(HttpMethod.PUT, "/api/**", authenticated)
                authorize(HttpMethod.POST, "/api/**", authenticated)
                authorize(HttpMethod.DELETE, "/api/**", authenticated)
                authorize(anyRequest, permitAll)
            }
            httpBasic { }
            formLogin {
                loginProcessingUrl = "/api/login"
                permitAll()
            }
            logout {
                logoutUrl = "/api/logout"
                permitAll()
            }
        }
        return http.build()
    }

    /**
     * File-based user store. For now, a default admin user.
     * TODO: Load from a users.json file in the data directory.
     */
    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin"))
            .roles("ADMIN")
            .build()
        return InMemoryUserDetailsManager(admin)
    }
}
