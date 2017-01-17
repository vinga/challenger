package com.kameo.challenger.domain.accounts

import com.kameo.challenger.domain.accounts.IAccountRestService.RegisterInternalDataDTO


interface IAccountRestService {

    data class RegisterRequestDTO(val email: String = "",
                                  val login: String = "",
                                  val password: String = "",
                                  val emailIsConfirmedByConfirmationLink: String="")

    data class RegisterResponseDTO(
            val registerSuccess: Boolean,
            val needsEmailConfirmation: Boolean,
            val registerError: String? = null

    )

    fun registerUser(rr: RegisterRequestDTO): RegisterResponseDTO


    data class RegisterInternalDataDTO(
        val emailRequiredForRegistration: String,
        val loginProposedForRegistration: String,
        val emailIsConfirmedByConfirmationLink: String)


    data class ConfirmationLinkRequestDTO(val newLogin: String? = null, val newPassword: String? = null)
    data class ConfirmationLinkResponseDTO(val description: String,

                                           val validationError: String? = null,
                                           val done: Boolean = true,

                                           val newPasswordRequired: Boolean = false,
                                           val newLoginRequired: Boolean=false,

                                           val registerInternalData: RegisterInternalDataDTO?=null,

                                           val displayLoginButton: Boolean = false,
                                           val displayRegisterButton: Boolean = false,
                                           val nextActions: List<NextActionType> = emptyList<NextActionType>(),

                                           val jwtToken: String? = null,
                                           val login: String? = null,
                                           val displayLoginWelcomeInfo: Boolean?=null)


    enum class NextActionType {
        AUTO_LOGIN,
        MANAGED_REGISTER_BUTTON,
        NEXT,
        MAIN_PAGE,
        LOGIN_BUTTON,
        REGISTER_BUTTON
    }

    fun confirmOrGetInfoIfNeeded(uid: String, confirmParams: ConfirmationLinkRequestDTO): ConfirmationLinkResponseDTO

}