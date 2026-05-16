package com.gbak.stoker.alerts

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Routes alert notifications to the appropriate delivery channels (Email, Browser).
 */
@Service
class DeliveryRouter(
    private val emailDelivery: EmailDelivery
) {
    private val log = LoggerFactory.getLogger(DeliveryRouter::class.java)

    val availableChannels: Set<String> = setOf("Email", "Browser Alert")

    fun deliver(channels: Set<String>, subject: String, message: String) {
        channels.forEach { channel ->
            try {
                when (channel) {
                    "Email" -> emailDelivery.send(subject, message)
                    "Browser Alert" -> {
                        // Browser alerts are handled via eventPublisher in AlertManager
                        // (pushed to WebSocket /topic/alarms)
                    }
                    else -> log.warn("Unknown delivery channel: {}", channel)
                }
            } catch (e: Exception) {
                log.error("Failed to deliver via {}: {}", channel, e.message)
            }
        }
    }
}
