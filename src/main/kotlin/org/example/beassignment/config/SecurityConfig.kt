package org.example.beassignment.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.beassignment.filter.JwtAuthFilter
import org.example.beassignment.service.JwtService
import org.example.beassignment.common.ApiResponse
import org.example.beassignment.common.ErrorCode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtService: JwtService,
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .securityContext { it.securityContextRepository(RequestAttributeSecurityContextRepository()) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/api/v1/auth/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/**",
                ).permitAll()
                auth.anyRequest().authenticated()
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { _, response, _ ->
                    response.status = 401
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.characterEncoding = "UTF-8"
                    objectMapper.writeValue(response.writer, ApiResponse.error(ErrorCode.UNAUTHORIZED))
                }
                ex.accessDeniedHandler { _, response, _ ->
                    response.status = 403
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.characterEncoding = "UTF-8"
                    objectMapper.writeValue(response.writer, ApiResponse.error(ErrorCode.FORBIDDEN))
                }
            }
            .addFilterBefore(
                JwtAuthFilter(jwtService, objectMapper),
                UsernamePasswordAuthenticationFilter::class.java,
            )
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
