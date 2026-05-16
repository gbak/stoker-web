package com.gbak.stoker.websocket

import com.gbak.stoker.model.alerts.BrowserAlarm
import com.gbak.stoker.model.events.DataPointEvent
import com.gbak.stoker.model.events.StateChangeEvent
import com.gbak.stoker.weather.WeatherChangeEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

/**
 * Bridges Spring application events to WebSocket STOMP topics.
 * Clients subscribe to:
 *   /topic/temperatures - real-time temperature and blower data
 *   /topic/events       - connection state changes
 *   /topic/alarms       - browser alarm notifications
 *   /topic/weather      - weather updates
 */
@Service
class StokerWebSocketPublisher(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val log = LoggerFactory.getLogger(StokerWebSocketPublisher::class.java)

    @EventListener
    fun onDataPoint(event: DataPointEvent) {
        log.trace("Publishing {} data points to /topic/temperatures", event.dataPoints.size)
        messagingTemplate.convertAndSend("/topic/temperatures", event.dataPoints)
    }

    @EventListener
    fun onStateChange(event: StateChangeEvent) {
        log.info("Publishing state change to /topic/events: {}", event.eventType)
        messagingTemplate.convertAndSend("/topic/events", event)
    }

    @EventListener
    fun onAlarm(alarm: BrowserAlarm) {
        log.info("Publishing alarm to /topic/alarms: {}", alarm.message)
        messagingTemplate.convertAndSend("/topic/alarms", alarm)
    }

    @EventListener
    fun onWeather(event: WeatherChangeEvent) {
        log.debug("Publishing weather to /topic/weather")
        messagingTemplate.convertAndSend("/topic/weather", event.weatherData)
    }
}
