package com.kameo.challenger.domain.accounts

import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Component
@Path("/api/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AccountRestService : IAccountRestService {

    @Inject
    private lateinit var accountDao: AccountDAO


    @POST
    @Path("register")
    override fun registerUser(rr: IAccountRestService.RegisterRequestDTO): IAccountRestService.RegisterResponseDTO {
        val internalResponse = accountDao.registerUser(rr.login,
                rr.password,
                rr.email);

        var errorInfo: String? = null;
        if (internalResponse.error!=null) {
           errorInfo=internalResponse.error
        }


        return IAccountRestService.RegisterResponseDTO(internalResponse.error==null,
                internalResponse.requireEmailConfirmation,
                errorInfo)

    }


}