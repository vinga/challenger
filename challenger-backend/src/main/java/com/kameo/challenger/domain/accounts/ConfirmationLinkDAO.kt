package com.kameo.challenger.domain.accounts

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.accounts.AccountDAO.InternalRegisterResponseDTO
import com.kameo.challenger.domain.accounts.IAccountRestService.ConfirmationLinkRequestDTO
import com.kameo.challenger.domain.accounts.IAccountRestService.ConfirmationLinkResponseDTO
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkODB
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkType
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkType.*
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.accounts.db.UserStatus
import com.kameo.challenger.domain.accounts.db.UserStatus.*
import com.kameo.challenger.domain.challenges.ChallengeDAO
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.db.ChallengeStatus
import com.kameo.challenger.utils.mail.MailService
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.newapi.unaryPlus
import com.kameo.challenger.web.rest.AuthFilter
import org.joda.time.DateTimeConstants
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

@Component
@Transactional
open class ConfirmationLinkDAO(@Inject val anyDaoNew: AnyDAONew,
                               @Inject val mailService: MailService,
                               @Inject val serverConfig: ServerConfig,
                               @Inject val authFilter: Provider<AuthFilter>
) {


    @Scheduled(fixedRate = DateTimeConstants.MILLIS_PER_DAY.toLong())
    open fun scheduler() {
        anyDaoNew.remove(ConfirmationLinkODB::class) {
            it get +ConfirmationLinkODB::sysCreationDate before LocalDateTime.now().minusHours(24)
        }
    }

    open fun confirmLink(uid: String, confirmParams: ConfirmationLinkRequestDTO, accountDAO: AccountDAO, challengeDAO: ChallengeDAO): ConfirmationLinkResponseDTO {
        val clODB = anyDaoNew.getFirst(ConfirmationLinkODB::class, {
            it get ConfirmationLinkODB::uid eq uid
        }) ?: return ConfirmationLinkResponseDTO("This link is no longer valid", done = true)
        val user = clODB.user;

        if (user.userStatus == SUSPENDED)
            throw IllegalArgumentException()



        return when (clODB.confirmationLinkType) {
            PASSWORD_RESET -> {
                if (confirmParams.newPassword != null) {
                    if (user.login.isNullOrEmpty())
                        throw IllegalArgumentException("Such link shouldn't be created because user's email isn't confirmed yet");

                    accountDAO.resetPassword(user, confirmParams.newPassword)
                    anyDaoNew.remove(clODB)

                    ConfirmationLinkResponseDTO("Your password has been changed. You can login now.", displayLoginButton = true)
                } else ConfirmationLinkResponseDTO("You have chosen to reset your password. To proceed please provide new password.", newPasswordRequired = true, done = false)
            }
            EMAIL_CONFIRMATION -> {

                val loginIsTaken = anyDaoNew.exists(UserODB::class) { it get +UserODB::login like clODB.fieldLogin!! }
                if (loginIsTaken) {
                    anyDaoNew.remove(clODB)
                    return ConfirmationLinkResponseDTO("This login is already taken, please try to register again.", displayRegisterButton = true)
                }
                user.login = clODB.fieldLogin;
                user.passwordHash = clODB.fieldPasswordHash!!
                user.salt = clODB.fieldSalt!!
                user.userStatus = ACTIVE
                anyDaoNew.merge(user)
                anyDaoNew.remove(clODB)
                val au = authFilter.get();
                val jwtToken = au.tokenToString(au.createNewTokenFromUserId(user.id))
                // user automatically be logged because jwtToken is set
                ConfirmationLinkResponseDTO("Your email is confirmed. You can login now. ", displayLoginButton = true, login = clODB.user.login, jwtToken = jwtToken)
            }
            CHALLENGE_CONFIRMATION_ACCEPT -> {
                challengeDAO.updateChallengeState(user.id, clODB.challengeId!!, ChallengeStatus.ACTIVE)
                anyDaoNew.remove(ConfirmationLinkODB::class) {
                    it get ConfirmationLinkODB::challengeId eq clODB.challengeId
                    it get ConfirmationLinkODB::confirmationLinkType eq ConfirmationLinkType.CHALLENGE_CONFIRMATION_REJECT
                }
                anyDaoNew.remove(clODB)




                var description = "You've accepted challenge " + anyDaoNew.getOne(ChallengeODB::class) { it eqId clODB.challengeId!! }.label + ".";
                if (user.userStatus == WAITING_FOR_EMAIL_CONFIRMATION) {
                    description += "You can register now";

                    val confirmationEmailLink = ConfirmationLinkODB()
                    confirmationEmailLink.user = user
                    confirmationEmailLink.confirmationLinkType = ConfirmationLinkType.EMAIL_CONFIRMATION
                    confirmationEmailLink.uid = UUID.randomUUID().toString()
                    anyDaoNew.persist(confirmationEmailLink)

                    return ConfirmationLinkResponseDTO(
                            emailRequiredForRegistration=user.email,
                            loginProposedForRegistration = user.email,
                            emailIsConfirmedByConfirmationLink=confirmationEmailLink.uid,
                            description = description,
                            displayRegisterButton = true,
                            done=true)
                } else { //active user



                    ConfirmationLinkResponseDTO(
                            description = description,
                            displayLoginButton = true,
                            login = clODB.user.login,
                            jwtToken = authFilter.get().let{ it.tokenToString(it.createNewTokenFromUserId(user.id)) }
                    )
                }


            }
            CHALLENGE_CONFIRMATION_REJECT -> {
                challengeDAO.updateChallengeState(user.id, clODB.challengeId!!, ChallengeStatus.REFUSED)
                anyDaoNew.remove(ConfirmationLinkODB::class) {
                    it get ConfirmationLinkODB::challengeId eq clODB.challengeId
                    it get ConfirmationLinkODB::confirmationLinkType eq ConfirmationLinkType.CHALLENGE_CONFIRMATION_ACCEPT
                }
                anyDaoNew.remove(clODB)

                ConfirmationLinkResponseDTO(
                        description = "You've rejected challenge " + anyDaoNew.getOne(ChallengeODB::class) { it eqId clODB.challengeId!! }.label + ".",
                        displayLoginButton = true
                )
            }


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

        val cclAccept = ConfirmationLinkODB()
        cclAccept.user = cp.user
        cclAccept.challengeId = cb.id
        cclAccept.confirmationLinkType = ConfirmationLinkType.CHALLENGE_CONFIRMATION_ACCEPT
        cclAccept.uid = UUID.randomUUID().toString()
        anyDaoNew.persist(cclAccept)

        val cclReject = ConfirmationLinkODB()
        cclReject.user = cp.user
        cclReject.challengeId = cb.id
        cclReject.confirmationLinkType = ConfirmationLinkType.CHALLENGE_CONFIRMATION_REJECT
        cclReject.uid = UUID.randomUUID().toString()
        anyDaoNew.persist(cclReject)


        val content =
                if (cp.user.userStatus != UserStatus.WAITING_FOR_EMAIL_CONFIRMATION) {
                    "<html>Hi ${cp.user.login}. <br/>\n" +
                            cb.createdBy.login + " challenged you: " + cb.label + "<br/>\n" +
                            "Follow the link below to accept or reject challenge.<br/>\n" +

                            "To accept challenge:<br/>\n" +
                            "<a href='${toActionLink(cclAccept)}'>${toActionLink(cclAccept)}</a><br/>\n" +

                            "To reject challenge:<br/>\n" +
                            "<a href='${toActionLink(cclReject)}'>${toActionLink(cclReject)}</a><br/>\n" +
                            challengerFooterMail() +
                            "</html>"
                } else {
                    "<html>Hi. <br/>\n" +
                            cb.createdBy.login + " challenged you: " + cb.label + "<br/>\n" +
                            "Follow the link below to accept or reject challenge.<br/>\n" +
                            "To accept challenge:<br/>\n" +
                            "<a href='${toActionLink(cclAccept)}'>${toActionLink(cclAccept)}</a><br/>\n" +

                            "To reject challenge:<br/>\n" +
                            "<a href='${toActionLink(cclReject)}'>${toActionLink(cclReject)}</a><br/>\n" +
                            challengerFooterMail() +
                            "</html>"
                }
        mailService.send(MailService.Message(cp.user.email, "Challenger - Invitation to Challenge",
                content, null, null))
    }


    private fun toActionLink(cl: ConfirmationLinkODB): String {
        return serverConfig.getConfirmEmailInvitationPattern(cl.uid)
    }

    open fun createAndSendEmailConfirmationLink(u: UserODB, fieldLogin: String, fieldPasswordHash: String, fieldSalt: String) {
        if (u.userStatus != WAITING_FOR_EMAIL_CONFIRMATION)
            throw IllegalArgumentException()
        val cl = ConfirmationLinkODB()
        cl.user = u
        cl.fieldLogin = fieldLogin
        cl.fieldPasswordHash = fieldPasswordHash
        cl.fieldSalt = fieldSalt
        cl.confirmationLinkType = ConfirmationLinkType.EMAIL_CONFIRMATION
        cl.uid = UUID.randomUUID().toString()
        anyDaoNew.persist(cl)

        val link = toActionLink(cl)

        mailService.send(MailService.Message(u.email,
                "Welcome to Challenger - confirm your email",
                "<html>Hi,\n" +
                        "To confirm you email address please use the link below within 24 hours.<br/>" +
                        "<b><a href='$link'>$link</a></b><br/><br/>" +
                        challengerFooterMail() +
                        "</html>", null, null))

    }

}
