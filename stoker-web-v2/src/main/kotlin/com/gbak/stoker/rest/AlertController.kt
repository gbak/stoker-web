package com.gbak.stoker.rest

import com.gbak.stoker.alerts.AlertManager
import com.gbak.stoker.model.alerts.AlertModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/alerts")
class AlertController(
    private val alertManager: AlertManager
) {
    @GetMapping("/configuration")
    fun getAlertConfiguration(): List<AlertModel> =
        alertManager.getConfiguration()

    @PutMapping("/configuration")
    fun setAlertConfiguration(@RequestBody alerts: List<AlertModel>): ResponseEntity<Void> {
        alertManager.setConfiguration(alerts)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/delivery-methods")
    fun getDeliveryMethods(): Set<String> =
        alertManager.availableDeliveryMethods
}
