package org.example.beassignment

import org.example.beassignment.config.AiProperties
import org.example.beassignment.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(AiProperties::class, JwtProperties::class)
class BeAssignmentApplication

fun main(args: Array<String>) {
    runApplication<BeAssignmentApplication>(*args)
}
