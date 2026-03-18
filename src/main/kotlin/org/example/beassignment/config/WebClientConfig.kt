package org.example.beassignment.config

import io.netty.channel.ChannelOption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfig(
    private val aiProperties: AiProperties,
) {
    @Bean
    fun aiWebClient(): WebClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, aiProperties.connectTimeoutMs.toInt())
            .responseTimeout(Duration.ofMillis(aiProperties.readTimeoutMs))

        return WebClient.builder()
            .baseUrl(aiProperties.baseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}
