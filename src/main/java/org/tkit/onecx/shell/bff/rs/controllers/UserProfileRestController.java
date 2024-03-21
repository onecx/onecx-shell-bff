package org.tkit.onecx.shell.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.onecx.shell.bff.rs.mappers.UserProfileMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.shell.bff.rs.internal.UserProfileApiService;
import gen.org.tkit.onecx.user.profile.client.api.UserProfileV1Api;
import gen.org.tkit.onecx.user.profile.client.model.UserProfile;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class UserProfileRestController implements UserProfileApiService {

    @Inject
    @RestClient
    UserProfileV1Api userProfileClient;

    @Inject
    UserProfileMapper mapper;

    @Override
    public Response getUserProfile() {
        try (Response response = userProfileClient.getUserProfile()) {
            return Response.status(response.getStatus())
                    .entity(mapper.map(response.readEntity(UserProfile.class))).build();
        }
    }
}
