package org.example.beassignment

import org.example.beassignment.config.AiProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
@SpringBootApplication
@EnableConfigurationProperties(AiProperties::class)
class BeAssignmentApplication

fun main(args: Array<String>) {
    runApplication<BeAssignmentApplication>(*args)
}
