package com.kameo.challenger.domain.accounts

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.accounts.IAccountRestService.ConfirmationLinkRequestDTO
import com.kameo.challenger.domain.accounts.IAccountRestService.ConfirmationLinkResponseDTO
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkODB
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkType
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkType.*
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.accounts.db.UserStatus
import com.kameo.challenger.domain.accounts.db.UserStatus.WAITING_FOR_EMAIL_CONFIRMATION
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.utils.mail.MailService
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.newapi.unaryPlus
import org.joda.time.DateTimeConstants
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@Component
@Transactional
open class ConfirmationLinkDAO(@Inject val anyDaoNew: AnyDAONew,
                               @Inject val mailService: MailService,
                               @Inject val serverConfig: ServerConfig
) {




    @Scheduled(fixedRate = DateTimeConstants.MILLIS_PER_DAY.toLong())
    open fun scheduler() {
        anyDaoNew.remove(ConfirmationLinkODB::class) {
            it get +ConfirmationLinkODB::sysCreationDate before LocalDateTime.now().minusHours(24)
        }
    }

    open fun confirmLink(uid: String, confirmParams: ConfirmationLinkRequestDTO, accountDAO: AccountDAO): ConfirmationLinkResponseDTO {
        val clODB = anyDaoNew.getFirst(ConfirmationLinkODB::class, {
            it get ConfirmationLinkODB::uid eq uid
        }) ?: return ConfirmationLinkResponseDTO("This link is no longer valid", done = true)

        if (clODB.user.userStatus == WAITING_FOR_EMAIL_CONFIRMATION) {
            if (confirmParams.newLogin != null && confirmParams.newPassword != null) {
                // probujemy usera zarejestrować
                return accountDAO.registerUser(clODB.user.email, confirmParams.newLogin, confirmParams.newPassword).let {
                    it.error?.let {
                        ConfirmationLinkResponseDTO("Welcome! To register please provide login and password. ", validationError = it,
                                newLoginRequired = true, newPasswordRequired = true, done = false, proposedLogin = clODB.user.email)
                    } ?: ConfirmationLinkResponseDTO("You account has been created. You can login now. ", displayLoginButton = true)
                }
            }
            return ConfirmationLinkResponseDTO("Welcome! To register please provide login and password. ", newLoginRequired = true,
                    newPasswordRequired = true, done = false, proposedLogin = clODB.user.email)
        }


        return when (clODB.confirmationLinkType) {
            PASSWORD_RESET -> {
                if (confirmParams.newPassword != null) {
                    //probujemy userowi zresetowac haslo

                    accountDAO.resetPassword(clODB.user, confirmParams.newPassword)
                    anyDaoNew.remove(clODB)

                    ConfirmationLinkResponseDTO("Your password has been changed. You can login now.", displayLoginButton = true)
                } else ConfirmationLinkResponseDTO("You have chosen to reset your password. To proceed please provide new password.", newPasswordRequired = true, done = false)
            }
            EMAIL_CONFIRMATION -> {
                ConfirmationLinkResponseDTO("Your email is confirmed. You can login now. ", displayLoginButton = true)
            }
            CHALLENGE_CONFIRMATION -> ConfirmationLinkResponseDTO(
                    description = "You've accepted challenge " + anyDaoNew.getOne(ChallengeODB::class) { it eqId clODB.challengeId!! } + ".",
                    displayLoginButton = true
            )
        }

    }

    open fun createAndSendPasswordResetLink(u: UserODB) {
        val ccl = ConfirmationLinkODB()
        ccl.user = u
        ccl.confirmationLinkType = ConfirmationLinkType.PASSWORD_RESET
        ccl.uid = UUID.randomUUID().toString()

        val link = toActionLink(ccl)

        val loginOrEmpty = if (u.login != null) "Your current login is <b>${u.login}</b>.<br/>" else ""
        mailService.send(MailService.Message(u.email,
                "Reset your Challenger Password",
                "<html>Hi,\n" +
                        "To change your password please use the link below within 24 hours.<br/>" +
                        "<b><a href='$link'>$link</a></b><br/><br/>" +
                        "Your current login is ${u.login}<br/>" +
                        loginOrEmpty +
                        challengerFooterMail() +
                        "</html>", null, null))
        anyDaoNew.persist(ccl)
    }

    private fun challengerFooterMail(): String = "<br/>Thank you,<br/>" +
            "The Challenger Team"

    open fun createAndSendChallengeConfirmationLink(cb: ChallengeODB, cp: ChallengeParticipantODB) {
        val ccl = ConfirmationLinkODB()
        ccl.user = cp.user
        ccl.challengeId = cb.id
        ccl.confirmationLinkType = ConfirmationLinkType.CHALLENGE_CONFIRMATION
        ccl.uid = UUID.randomUUID().toString()
        println("#######3UID: " + ccl.uid)
        anyDaoNew.persist(ccl)


        val content =
                if (cp.user.userStatus != UserStatus.WAITING_FOR_EMAIL_CONFIRMATION) {
                    "<html>Hi ${cp.user.login}. <br/>" +
                            cb.createdBy.login + " challenged you: " + cb.label + "<br/>" +
                            "Follow the link below to accept or reject challenge.<br/>" +
                            "<a href='${toActionLink(ccl)}'>${toActionLink(ccl)}</a>" +
                            challengerFooterMail() +
                            "</html>"
                } else {
                    "<html>Hi. <br/>" +
                            cb.createdBy.login + " challenged you: " + cb.label + "<br/>" +
                            "Follow the link below to accept or reject challenge.<br/>" +
                            "<a href='${toActionLink(ccl)}'>${toActionLink(ccl)}</a>" +
                            challengerFooterMail() +
                            "</html>"
                }
        mailService.send(MailService.Message(cp.user.email, "Challenger - Invitation to Challenge",
                content, null, null))
    }


    private fun toActionLink(cl: ConfirmationLinkODB): String {
        return serverConfig.getConfirmEmailInvitationPattern(cl.uid)
    }

}
