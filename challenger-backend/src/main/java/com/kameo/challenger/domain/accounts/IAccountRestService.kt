package com.kameo.challenger.domain.accounts


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


    data class ConfirmationLinkRequestDTO(val newLogin: String? = null, val newPassword: String? = null)
    data class ConfirmationLinkResponseDTO(val description: String,
                                           val newPasswordRequired: Boolean = false,
                                           val validationError: String? = null,
                                           val done: Boolean = true,


                                           val emailRequiredForRegistration: String? = null,
                                           val loginProposedForRegistration: String? = null,
                                           val emailIsConfirmedByConfirmationLink: String?=null,


                                           val displayLoginButton: Boolean = false,
                                           val displayRegisterButton: Boolean = false,


                                           val jwtToken: String? = null,
                                           val login: String? = null )


    fun confirmOrGetInfoIfNeeded(uid: String, confirmParams: ConfirmationLinkRequestDTO): ConfirmationLinkResponseDTO

}