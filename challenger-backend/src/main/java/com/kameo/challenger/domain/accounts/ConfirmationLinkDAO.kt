package com.kameo.challenger.domain.accounts

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.accounts.IAccountRestService.*
import com.kameo.challenger.domain.accounts.IAccountRestService.NextActionType.*
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
import com.kameo.challenger.domain.challenges.db.ChallengeStatus.REMOVED
import com.kameo.challenger.domain.challenges.db.ChallengeStatus.WAITING_FOR_ACCEPTANCE
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
                               @Inject val serverConfig: ServerConfig,
                               @Inject val jwtJWTokenDAO: JWTokenDAO

) {


    @Scheduled(fixedRate = DateTimeConstants.MILLIS_PER_DAY.toLong())
    open fun scheduler() {
        anyDaoNew.remove(ConfirmationLinkODB::class) {
            it get ConfirmationLinkODB::confirmationLinkType notEq ConfirmationLinkType.CHALLENGE_CONFIRMATION_ACCEPT
            it get ConfirmationLinkODB::confirmationLinkType notEq ConfirmationLinkType.CHALLENGE_CONFIRMATION_REJECT
            it get +ConfirmationLinkODB::sysCreationDate before LocalDateTime.now().minusHours(24)
        }

        anyDaoNew.remove(ConfirmationLinkODB::class) {
            it get ConfirmationLinkODB::confirmationLinkType isIn listOf(ConfirmationLinkType.CHALLENGE_CONFIRMATION_ACCEPT, ConfirmationLinkType.CHALLENGE_CONFIRMATION_REJECT)
            it get +ConfirmationLinkODB::sysCreationDate before LocalDateTime.now().minusYears(1)
        }
    }

    open fun confirmLink(uid: String, confirmParams: ConfirmationLinkRequestDTO, accountDAO: AccountDAO, challengeDAO: ChallengeDAO): ConfirmationLinkResponseDTO {
        val clODB = anyDaoNew.getFirst(ConfirmationLinkODB::class, {
            it get ConfirmationLinkODB::uid eq uid
        }) ?: return ConfirmationLinkResponseDTO("This link is no longer valid", done = true,  nextActions = listOf(MAIN_PAGE, LOGIN_BUTTON, REGISTER_BUTTON))
        val user = clODB.user

        if (user.userStatus == SUSPENDED)
            throw IllegalArgumentException()



        return when (clODB.confirmationLinkType) {
            OAUTH2_LOGIN -> {
                val jwtToken = jwtJWTokenDAO.createNewTokenFromUserId(user.id)
                if (user.login.isNullOrEmpty()) {
                    if (confirmParams.newLogin!=null) {
                        //TODO verify new login
                        if (accountDAO.checkIfLoginExists(confirmParams.newLogin)) {
                            return ConfirmationLinkResponseDTO("This login is already taken. Please specify another login. ",
                                    newLoginRequired = true,
                                    displayLoginButton = true,
                                    nextActions = listOf(NEXT))
                        }
                        user.login=confirmParams.newLogin
                        anyDaoNew.merge(user)
                    }
                    else
                        return ConfirmationLinkResponseDTO("Please specify your login. ",
                            newLoginRequired = true,
                            displayLoginButton = true,
                            nextActions = listOf(NEXT))

                }
                anyDaoNew.remove(clODB)


                ConfirmationLinkResponseDTO("You've been logged now. ",
                        displayLoginButton = true,
                        login = clODB.user.login,
                        jwtToken = jwtToken,
                        nextActions = listOf(AUTO_LOGIN))
            }
            PASSWORD_RESET -> {
                if (confirmParams.newPassword != null) {
                    if (user.login.isNullOrEmpty())
                        throw IllegalArgumentException("Such link shouldn't be created because user's email isn't confirmed yet")

                    accountDAO.resetPassword(user, confirmParams.newPassword)
                    anyDaoNew.remove(clODB)

                    ConfirmationLinkResponseDTO("Your password has been changed. You can login now.",
                            displayLoginButton = true,
                            nextActions = listOf(LOGIN_BUTTON))
                } else ConfirmationLinkResponseDTO("You have chosen to reset your password. To proceed please provide new password.",
                        newPasswordRequired = true,
                        done = false,
                        nextActions = listOf(NEXT)
                        )
            }
            EMAIL_CONFIRMATION -> {

                val loginIsTaken = anyDaoNew.exists(UserODB::class) { it get +UserODB::login like clODB.fieldLogin!! }
                if (loginIsTaken) {
                    anyDaoNew.remove(clODB)
                    return ConfirmationLinkResponseDTO("This login is already taken, please try to register again.",
                            displayRegisterButton = true,
                            nextActions = listOf(REGISTER_BUTTON, LOGIN_BUTTON))
                }
                user.login = clODB.fieldLogin
                user.passwordHash = clODB.fieldPasswordHash!!
                user.salt = clODB.fieldSalt!!
                user.userStatus = ACTIVE
                anyDaoNew.merge(user)
                anyDaoNew.remove(clODB)
                val jwtToken = jwtJWTokenDAO.createNewTokenFromUserId(user.id)
                // user automatically be logged because jwtToken is set
                ConfirmationLinkResponseDTO("Your email is confirmed. You can login now. ",
                        displayLoginButton = true,
                        login = clODB.user.login,
                        jwtToken = jwtToken,
                        displayLoginWelcomeInfo = true,
                        nextActions = listOf(AUTO_LOGIN))
            }
            CHALLENGE_CONFIRMATION_ACCEPT -> {
                val ch=anyDaoNew.find(ChallengeODB::class, clODB.challengeId!!)
                if (ch.challengeStatus==REMOVED && ch.participants.none {it.user.id == user.id && it.challengeStatus==WAITING_FOR_ACCEPTANCE}) {
                    return ConfirmationLinkResponseDTO("The challenge is not accessible anymore.",
                            newLoginRequired = true,
                            displayLoginButton = true,
                            nextActions = listOf(LOGIN_BUTTON, REGISTER_BUTTON))
                }
                challengeDAO.updateChallengeState(user.id, clODB.challengeId!!, ChallengeStatus.ACTIVE)
                anyDaoNew.remove(ConfirmationLinkODB::class) {
                    it get ConfirmationLinkODB::challengeId eq clODB.challengeId
                    it get ConfirmationLinkODB::confirmationLinkType eq ConfirmationLinkType.CHALLENGE_CONFIRMATION_REJECT
                }
                anyDaoNew.remove(clODB)


                var description = "You've accepted challenge " + anyDaoNew.getOne(ChallengeODB::class) { it eqId clODB.challengeId!! }.label + "."
                if (user.userStatus == WAITING_FOR_EMAIL_CONFIRMATION) {
                    description += "You can register now"

                    val confirmationEmailLink = ConfirmationLinkODB()
                    confirmationEmailLink.user = user
                    confirmationEmailLink.confirmationLinkType = ConfirmationLinkType.EMAIL_CONFIRMATION
                    confirmationEmailLink.uid = UUID.randomUUID().toString()
                    anyDaoNew.persist(confirmationEmailLink)

                    return ConfirmationLinkResponseDTO(
                            registerInternalData = RegisterInternalDataDTO(user.email, user.email,confirmationEmailLink.uid),
                            description = description,
                            displayRegisterButton = true,
                            done = true,
                            nextActions = listOf(MANAGED_REGISTER_BUTTON))
                } else { //active user
                    ConfirmationLinkResponseDTO(
                            description = description,
                            displayLoginButton = true,
                            login = clODB.user.login,
                            jwtToken = jwtJWTokenDAO.createNewTokenFromUserId(user.id),
                            nextActions = listOf(AUTO_LOGIN)
                    )
                }


            }
            CHALLENGE_CONFIRMATION_REJECT -> {
                val ch=anyDaoNew.find(ChallengeODB::class, clODB.challengeId!!)
                if (ch.challengeStatus==REMOVED && ch.participants.none {it.user.id == user.id && it.challengeStatus==WAITING_FOR_ACCEPTANCE}) {
                    return ConfirmationLinkResponseDTO("The challenge is not accessible anymore.",
                            newLoginRequired = true,
                            displayLoginButton = true,
                            nextActions = listOf(LOGIN_BUTTON, REGISTER_BUTTON))
                }
                challengeDAO.updateChallengeState(user.id, clODB.challengeId!!, ChallengeStatus.REFUSED)
                anyDaoNew.remove(ConfirmationLinkODB::class) {
                    it get ConfirmationLinkODB::challengeId eq clODB.challengeId
                    it get ConfirmationLinkODB::confirmationLinkType eq ConfirmationLinkType.CHALLENGE_CONFIRMATION_ACCEPT
                }
                anyDaoNew.remove(clODB)

                ConfirmationLinkResponseDTO(
                        description = "You've rejected challenge " + anyDaoNew.getOne(ChallengeODB::class) { it eqId clODB.challengeId!! }.label + ".",
                        displayLoginButton = true,
                        nextActions = listOf(LOGIN_BUTTON, REGISTER_BUTTON)
                )
            }


        }

    }

    private fun challengeODB(clODB: ConfirmationLinkODB) = anyDaoNew.find(ChallengeODB::class, clODB.challengeId!!)

    open fun createOauth2LoginLink(userId: Long): String {
        val ccl = ConfirmationLinkODB()
        ccl.user = UserODB(userId)
        ccl.confirmationLinkType = ConfirmationLinkType.OAUTH2_LOGIN
        ccl.uid = UUID.randomUUID().toString()
        anyDaoNew.persist(ccl)
        return ccl.uid
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


        val loginWithSpace=if (cp.user.login!=null)
           " "+cp.user.login
         else
           ""

        val content =
                if (cp.user.userStatus != UserStatus.WAITING_FOR_EMAIL_CONFIRMATION) {
                    "<html>Hi${loginWithSpace}.<br/>\n" +
                            cb.createdBy.login + " has challenged you.<br/>\n" +
                            "Follow the link below to accept or reject challenge.<br/>\n" +

                            "To accept challenge <b>${cb.label}</b>:<br/>\n" +
                            "<a href='${toActionLink(cclAccept)}'>${toActionLink(cclAccept)}</a><br/>\n" +

                            "To reject challenge:<br/>\n" +
                            "<a href='${toActionLink(cclReject)}'>${toActionLink(cclReject)}</a><br/>\n" +
                            challengerFooterMail() +
                            "</html>"
                } else {
                    "<html>Hi. <br/>\n" +
                            "Somebody called "+cb.createdBy.login + " has challenged you.<br/>\n" +
                            "Follow the link below to accept or reject challenge.<br/>\n" +
                            "To accept challenge <b>${cb.label}</b>:<br/>\n" +
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


    open fun deleteConfirmationLinkForChallenge(challenge: ChallengeODB) {
        anyDaoNew.remove(ConfirmationLinkODB::class) {
            it get ConfirmationLinkODB::challengeId eq challenge.id
        }
    }

    open fun deleteConfirmationLinkForChallengeAndUser(challenge: ChallengeODB, user: UserODB) {
        anyDaoNew.remove(ConfirmationLinkODB::class) {
            it get ConfirmationLinkODB::challengeId eq challenge.id
            it get ConfirmationLinkODB::uid eqId user.id
        }
    }
}
