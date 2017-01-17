package com.kameo.challenger.domain.accounts

import com.google.common.base.Strings
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkODB
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkType
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.accounts.db.UserStatus
import com.kameo.challenger.domain.accounts.db.UserStatus.*
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.utils.auth.jwt.JWTService.AuthException
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.newapi.unaryPlus
import org.joda.time.DateTime
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.inject.Inject


@Component
@Transactional
open class AccountDAO(@Inject val anyDaoNew: AnyDAONew,
                      @Inject val confirmationLinkDAO: ConfirmationLinkDAO) {


    @Throws(AuthException::class)
    open fun login(login: String, pass: String): Long {
        if (Strings.isNullOrEmpty(pass)) {
            throw AuthException("No password")
        }

        val u = anyDaoNew.getFirst(UserODB::class, { it get UserODB::login eq login })
        if (u != null) {
            if (u.userStatus == UserStatus.WAITING_FOR_EMAIL_CONFIRMATION)
                throw AuthException("Please confirm your email first")
            if (u.passwordHash == PasswordUtil.getPasswordHash(pass, u.salt)) {
                if (u.userStatus == UserStatus.SUSPENDED && DateTime(u.suspendedDueDate).isBeforeNow) {
                    // lest unblock it, but how we can know which status was previous?
                    u.userStatus = UserStatus.ACTIVE
                    anyDaoNew.em.merge(u)
                }
                if (u.userStatus == UserStatus.SUSPENDED) {
                    throw AuthException("There have been several failed attempts to sign in from this account or IP address. Please wait a while and try again later.")
                } else if (u.userStatus != UserStatus.ACTIVE) {
                    throw AuthException("Your account is not active")
                } else {
                    u.failedLoginsNumber = 0
                    anyDaoNew.em.merge(u)
                    return u.id
                }

            } else {
                u.failedLoginsNumber = u.failedLoginsNumber + 1
                if (u.failedLoginsNumber > 10) {
                    u.userStatus = UserStatus.SUSPENDED
                    u.suspendedDueDate = LocalDateTime.now().plusMinutes(20)
                }
                anyDaoNew.em.merge(u)
                throw AuthException("Wrong credentials")
            }
        } else {
            throw AuthException("User with login '$login' doesn't exist")
        }
    }


    open fun getOrCreateUserForEmail(email: String?): UserODB {
        if (email == null)
            throw IllegalArgumentException("Either second user id or second  user email must be provided")
        val u = anyDaoNew.getFirst(UserODB::class) { it.eq(UserODB::email, email) }
        return u ?: createPendingUserWithEmailOnly(email)
    }

    open fun getOrCreateOauth2GoogleUser(oauth2GoogleId: String, email: String, emailIsVerified: Boolean): UserODB {
        val u = anyDaoNew.getFirst(UserODB::class) { it.eq(UserODB::oauth2GoogleId, oauth2GoogleId) }
                ?: anyDaoNew.getFirst(UserODB::class) { it.eq(UserODB::email, email) }
                ?: createPendingUserWithEmailOnly(email)
        if (u.oauth2GoogleId!=oauth2GoogleId) {
            u.oauth2GoogleId = oauth2GoogleId
            anyDaoNew.merge(u)
        }
        if (u.userStatus == WAITING_FOR_EMAIL_CONFIRMATION && emailIsVerified && email == u.email) {
            u.userStatus = ACTIVE
            anyDaoNew.merge(u)
        }
        return u;
    }
    open fun getOrCreateOauth2FacebookUser(oauth2FacebookId: String, email: String, emailIsVerified: Boolean): UserODB {
        val u = anyDaoNew.getFirst(UserODB::class) { it.eq(UserODB::oauth2FacebookId, oauth2FacebookId) }
                ?: anyDaoNew.getFirst(UserODB::class) { it.eq(UserODB::email, email) }
                ?: createPendingUserWithEmailOnly(email)
        if (u.oauth2FacebookId!=oauth2FacebookId) {
            u.oauth2FacebookId = oauth2FacebookId
            anyDaoNew.merge(u)
        }
        if (u.userStatus == WAITING_FOR_EMAIL_CONFIRMATION && emailIsVerified && email == u.email) {
            u.userStatus = ACTIVE
            anyDaoNew.merge(u)
        }
        return u;
    }

    protected open fun createPendingUserWithEmailOnly(email: String, emailIsVerified: Boolean? = false): UserODB {
        val user = UserODB()
        user.email = email
        user.userStatus = UserStatus.WAITING_FOR_EMAIL_CONFIRMATION
        anyDaoNew.em.persist(user)
        return user
    }

    class InternalRegisterResponseDTO(val error: String? = null, val requireEmailConfirmation: Boolean = false)

    open fun registerUser(login: String, password: String, email: String, emailIsConfirmedByConfirmationLink: String?): InternalRegisterResponseDTO {
        println("register user $login $password $email $emailIsConfirmedByConfirmationLink")
        if (login.isNullOrBlank())
            throw IllegalArgumentException()

        val confirmationLink =
                if (emailIsConfirmedByConfirmationLink != null)
                    anyDaoNew.getFirst(ConfirmationLinkODB::class) {
                        it get +ConfirmationLinkODB::uid eq emailIsConfirmedByConfirmationLink
                        it get ConfirmationLinkODB::confirmationLinkType eq ConfirmationLinkType.EMAIL_CONFIRMATION
                    }
                else null


        val existingUser =
                confirmationLink?.user ?: anyDaoNew.getFirst(UserODB::class) {
                    it get UserODB::email eq email
                }


        val loginIsTaken = anyDaoNew.exists(UserODB::class) { it get +UserODB::login like login }
        if (loginIsTaken)
            return InternalRegisterResponseDTO("Login $login is already registered.")


        if (existingUser == null) {
            val user = UserODB()
            user.email = email
            user.login = login
            user.userStatus = WAITING_FOR_EMAIL_CONFIRMATION
            //updateUserFields(login, password, user)

            anyDaoNew.persist(user)

            val salt = PasswordUtil.createSalt()
            val passwordHash = PasswordUtil.getPasswordHash(password, salt)
            confirmationLinkDAO.createAndSendEmailConfirmationLink(user, login, passwordHash, salt)
            return InternalRegisterResponseDTO(requireEmailConfirmation = true)
        } else if (existingUser.userStatus == SUSPENDED) {
            return InternalRegisterResponseDTO("This account is suspended. Try again after a while.")
        } else if (existingUser.userStatus != WAITING_FOR_EMAIL_CONFIRMATION) {
            return InternalRegisterResponseDTO("Email $email is already registered.")
        } else {

            // user exists but has state waiting for email confirmation (this is when for example sb invited email)
            val salt = PasswordUtil.createSalt()
            val passwordHash = PasswordUtil.getPasswordHash(password, salt)

            if (emailIsConfirmedByConfirmationLink != null) {
                // because user clicked on link from email we know it's confirmed
                existingUser.login = login
                existingUser.salt = salt
                existingUser.passwordHash = passwordHash
                existingUser.userStatus = ACTIVE

                // we can now remove confirmationLink
                anyDaoNew.remove(ConfirmationLinkODB::class) { it get ConfirmationLinkODB::uid eq emailIsConfirmedByConfirmationLink }

                return InternalRegisterResponseDTO(requireEmailConfirmation = false)
            } else {
                confirmationLinkDAO.createAndSendEmailConfirmationLink(existingUser, login, passwordHash, salt)
                return InternalRegisterResponseDTO(requireEmailConfirmation = true)
            }
        }
    }

    open fun getUsersForLabels(labels: List<String>): List<UserODB> {
        return anyDaoNew.getAll(UserODB::class) {
            it newOr {
                it.get(UserODB::email) isIn labels
                it.get(UserODB::login) isIn labels
            }
        }
    }

    open fun sendResetMyPasswordLink(email: String) {
        val u = anyDaoNew.getFirst(UserODB::class) {
            it get UserODB::email eq email
            it get UserODB::userStatus eq UserStatus.ACTIVE // quietly don't send anything if email is not confirmed
        }
        if (u != null)
            confirmationLinkDAO.createAndSendPasswordResetLink(u)
    }

    open fun createAndSendChallengeConfirmationLink(cb: ChallengeODB, cp: ChallengeParticipantODB) {
        confirmationLinkDAO.createAndSendChallengeConfirmationLink(cb, cp)
    }

    open fun getUserIdByLogin(login: String): Long {
        return anyDaoNew.getOne(UserODB::class) {
            it get UserODB::login eq login
        }.id
    }

    open fun checkIfLoginExists(login: String): Boolean {
        return anyDaoNew.exists(UserODB::class) {
            it get UserODB::login eq login
        }
    }

    open fun resetPassword(user: UserODB, newPassword: String) {
        if (user.userStatus == SUSPENDED)
            throw IllegalArgumentException()
        user.userStatus = ACTIVE
        user.salt = PasswordUtil.createSalt()
        user.passwordHash = PasswordUtil.getPasswordHash(newPassword, user.salt)
        anyDaoNew.merge(user)
    }


}
