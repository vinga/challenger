package com.kameo.challenger.utils.mail

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import java.util.*
import javax.inject.Inject
import javax.mail.Message.RecipientType
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress



/**
 * Created by Kamila on 2016-12-22.
 */
@Component
open class MailService {
    @Inject
    private val javaMailSender: JavaMailSender? = null

    @Value("\${mailSenderFrom}")
    private lateinit var mailSenderFrom:String;

    data class Message(val toEmail: String, val subject: String, val content: String, val replyTo: String?=null, val from: String?=null)

    open fun send(m: Message) {
        println("Send message "+mailSenderFrom)
        val mail = javaMailSender!!.createMimeMessage()
        try {
            val helper = MimeMessageHelper(mail, true)
            helper.setTo(m.toEmail)
            if (m.replyTo != null)
                helper.setReplyTo(m.replyTo)

            helper.setFrom(mailSenderFrom);
            helper.setSubject(m.subject)
            helper.setText(m.content,m.content.startsWith("<html>",true))
        } catch (e: MessagingException) {
            e.printStackTrace()
        } finally {
        }
        javaMailSender.send(mail)

    }


}
fun main(args : Array<String>) {

    val username = "user@gmail.com"
    val password = ""
    val recp=""

    val props = Properties()
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.port", "587")

    val session = Session.getInstance(props,
            object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

    try {

        val message = MimeMessage(session)
        message.setFrom(InternetAddress(username))
        message.setRecipients(RecipientType.TO,
                InternetAddress.parse(recp))
        message.setSubject("Jak tam")
        message.setText("DCo am")

        Transport.send(message)

        println("Done")

    } catch (e: MessagingException) {
        throw RuntimeException(e)
    }

}