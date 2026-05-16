package com.gbak.stoker.alerts

import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import java.util.Properties

@ConfigurationProperties(prefix = "stoker.email")
data class EmailProperties(
    val enabled: Boolean = false,
    val smtpHost: String = "",
    val smtpPort: Int = 465,
    val username: String = "",
    val password: String = "",
    val sendTo: String = "",
    val useSsl: Boolean = true
)

@Service
class EmailDelivery(
    private val emailProperties: EmailProperties
) {
    private val log = LoggerFactory.getLogger(EmailDelivery::class.java)

    fun send(subject: String, messageText: String) {
        if (!emailProperties.enabled) {
            log.debug("Email delivery disabled, skipping: {}", subject)
            return
        }

        try {
            val props = Properties().apply {
                put("mail.smtp.host", emailProperties.smtpHost)
                put("mail.smtp.port", emailProperties.smtpPort.toString())
                put("mail.smtp.auth", "true")
                if (emailProperties.useSsl) {
                    put("mail.smtp.socketFactory.port", emailProperties.smtpPort.toString())
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                }
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication =
                    PasswordAuthentication(emailProperties.username, emailProperties.password)
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(emailProperties.username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailProperties.sendTo))
                setSubject(subject)
                setText(messageText)
            }

            Transport.send(message)
            log.info("Email sent: {}", subject)
        } catch (e: Exception) {
            log.error("Failed to send email: {}", e.message)
        }
    }
}
