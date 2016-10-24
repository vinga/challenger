package com.kameo.challenger.domain.accounts


interface IAccountRestService {

    data class RegisterRequestDTO(val email: String="",
                                  val login: String="",
                                  val password: String="");

    data class RegisterResponseDTO(
            val registerSuccess: Boolean,
            val needsEmailConfirmation: Boolean,
            val registerError: String? = null

    );

    fun registerUser(registerRequest: RegisterRequestDTO):RegisterResponseDTO

}