package com.kameo.challenger.domain.accounts

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.accounts.IAccountRestService.ConfirmationLinkRequestDTO
import com.kameo.challenger.domain.accounts.IAccountRestService.ConfirmationLinkResponseDTO
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Component
@Path(ServerConfig.restPath)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
open class AccountRestService : IAccountRestService {

    @Inject
    private lateinit var accountDao: AccountDAO

    @Inject
    private lateinit var confirmationLinkDAO: ConfirmationLinkDAO


    @POST
    @Path("/accounts/register")
    override fun registerUser(rr: IAccountRestService.RegisterRequestDTO): IAccountRestService.RegisterResponseDTO {
        val internalResponse = accountDao.registerUser(rr.login,
                rr.password,
                rr.email)
        var errorInfo: String? = null
        if (internalResponse.error != null) {
            errorInfo = internalResponse.error
        }
        return IAccountRestService.RegisterResponseDTO(internalResponse.error == null,
                internalResponse.requireEmailConfirmation,
                errorInfo)

    }

    @GET
    @Path("ping")
    fun ping(): String {
        return "pong"
    }


    @GET
    @Path("/accounts")
    fun checkIfLoginExists(@QueryParam("login") login: String): Boolean {
        return accountDao.checkIfLoginExists(login)
    }

    @POST
    @Path("/accounts/confirmationLinks/confirmation={uid}")
    override fun confirmOrGetInfoIfNeeded(@PathParam("uid") uid: String, confirmParams: ConfirmationLinkRequestDTO): ConfirmationLinkResponseDTO {
        return confirmationLinkDAO.confirmLink(uid, confirmParams, accountDao)
    }


    @POST
    @Path("/accounts/passwordReset")
    @WebResponseStatus(WebResponseStatus.ACCEPTED)
    fun passwordReset(email: String) {
        accountDao.sendResetMyPasswordLink(email)
    }
}