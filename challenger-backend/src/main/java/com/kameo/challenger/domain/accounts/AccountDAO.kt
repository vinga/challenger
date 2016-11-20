package com.kameo.challenger.domain.accounts

import com.google.common.base.Strings
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.accounts.db.UserStatus
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.utils.auth.jwt.AbstractAuthFilter
import com.kameo.challenger.utils.odb.AnyDAONew
import org.joda.time.DateTime
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.inject.Inject

@Component
@Transactional
open class AccountDAO(@Inject val anyDaoNew: AnyDAONew,
                      @Inject val confirmationLinkLogic: ConfirmationLinkLogic) {


    @Throws(AbstractAuthFilter.AuthException::class)
    open fun login(login: String, pass: String): Long {
        if (Strings.isNullOrEmpty(pass)) {
            throw AbstractAuthFilter.AuthException("No password")
        }

        val u = anyDaoNew.getFirst(UserODB::class, { it get UserODB::login eq login })
        if (u != null) {
            if (u.userStatus == UserStatus.WAITING_FOR_EMAIL_CONFIRMATION)
                throw AbstractAuthFilter.AuthException("Please confirm your email first")
            if (u.passwordHash == PasswordUtil.getPasswordHash(pass, u.salt)) {
                if (u.userStatus == UserStatus.SUSPENDED && DateTime(u.suspendedDueDate).isBeforeNow) {
                    // lest unblock it, but how we can know which status was previous?
                    u.userStatus = UserStatus.ACTIVE
                    anyDaoNew.em.merge(u)
                }
                if (u.userStatus == UserStatus.SUSPENDED) {
                    throw AbstractAuthFilter.AuthException("There have been several failed attempts to sign in from this account or IP address. Please wait a while and try again later.")
                } else if (u.userStatus != UserStatus.ACTIVE) {
                    throw AbstractAuthFilter.AuthException("Your account is not active")
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
                throw AbstractAuthFilter.AuthException("Wrong credentials")
            }
        } else {
            throw AbstractAuthFilter.AuthException("User with login '$login' doesn't exist")
        }
    }


    open fun getOrCreateUserForEmail(email: String?): UserODB {
        if (email == null)
            throw IllegalArgumentException("Either second user id or second  user email must be provided")
        val u = anyDaoNew.getFirst(UserODB::class) { it.eq(UserODB::email, email) }
        return u ?: createPendingUserWithEmailOnly(email)
    }


    protected open fun createPendingUserWithEmailOnly(email: String): UserODB {
        val user = UserODB()
        user.login = ""
        user.email = email
        user.userStatus = UserStatus.WAITING_FOR_EMAIL_CONFIRMATION
        anyDaoNew.em.persist(user)
        return user
    }

    class InternalRegisterResponseDTO(val error: String? = null, val requireEmailConfirmation: Boolean = false)

    open fun registerUser(login: String, password: String, email: String): InternalRegisterResponseDTO {
        val challengeParticipant = anyDaoNew.getFirst(ChallengeParticipantODB::class, {
            it.get(ChallengeParticipantODB::user).get(UserODB::email) eq email

        })

        if (challengeParticipant == null) {

            val existingUser = anyDaoNew.getFirst(UserODB::class, {
                it.get(UserODB::login) eq login
            })
            if (existingUser != null)
                return InternalRegisterResponseDTO("Login $login is already registered.")
            val existingEmail = anyDaoNew.getFirst(UserODB::class, {
                it.get(UserODB::email) eq email
            })
            if (existingEmail != null)
                return InternalRegisterResponseDTO("Email $email is already registered.")


            val user = UserODB()
            user.login = login
            user.email = email
            user.userStatus = UserStatus.ACTIVE
            user.salt = PasswordUtil.createSalt()
            user.passwordHash = PasswordUtil.getPasswordHash(password, user.salt)
            anyDaoNew.em.persist(user)
            return InternalRegisterResponseDTO(requireEmailConfirmation = false)


        } else if (challengeParticipant.user.userStatus == UserStatus.WAITING_FOR_EMAIL_CONFIRMATION) {
            confirmationLinkLogic.createAndSendEmailConfirmationLink(login, password, email)
            return InternalRegisterResponseDTO(requireEmailConfirmation = true)
        } else

            return InternalRegisterResponseDTO("Cannot register")
    }

    open fun getUsersForLabels(labels: List<String>): List<UserODB> {
        return anyDaoNew.getAll(UserODB::class) {
            it.newOr {
                it.get(UserODB::email) isIn  labels
                it.get(UserODB::login) isIn  labels
            }
        };//TODO comparator with enums, first user active.sortedBy { it.userStatus }
    }

    open fun sendResetMyPasswordLink(email: String) {
        val u = anyDaoNew.getFirst(UserODB::class, {
            it get UserODB::email eq email
            it get UserODB::userStatus eq UserStatus.ACTIVE
        })
        if (u != null)
            confirmationLinkLogic.createAndSendPasswordResetLink(u)
    }

    open fun createAndSendChallengeConfirmationLink(cb: ChallengeODB, cp: ChallengeParticipantODB) {
        confirmationLinkLogic.createAndSendChallengeConfirmationLink(cb, cp)
    }

    open fun getUserIdByLogin(login: String) :Long {
        return anyDaoNew.getOne(UserODB::class, {
            it get UserODB::login eq login
        }).id
    }

    open fun  checkIfLoginExists(login: String): Boolean {
        return anyDaoNew.exists(UserODB::class){
            it get UserODB::login eq login
        }
    }


}
