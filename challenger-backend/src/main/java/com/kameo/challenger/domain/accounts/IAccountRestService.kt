package com.kameo.challenger.domain.accounts


interface IAccountRestService {

    data class RegisterRequestDTO(val email: String = "",
                                  val login: String = "",
                                  val password: String = "")

    data class RegisterResponseDTO(
            val registerSuccess: Boolean,
            val needsEmailConfirmation: Boolean,
            val registerError: String? = null

    )

    fun registerUser(rr: RegisterRequestDTO): RegisterResponseDTO


    data class ConfirmationLinkRequestDTO(val newLogin: String? = null, val newPassword: String? = null);
    data class ConfirmationLinkResponseDTO(val description: String,
                                           val newLoginRequired: Boolean = false,
                                           val newPasswordRequired: Boolean = true,
                                           val displayLoginButton: Boolean = false,
                                           val validationError: String? = null,
                                           val done: Boolean = true,
                                           val proposedLogin: String? = null)


    fun confirmOrGetInfoIfNeeded(uid: String, confirmParams: ConfirmationLinkRequestDTO): ConfirmationLinkResponseDTO

}