package io.quarkus.it.config.jasypt;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import io.smallrye.config.SmallRyeConfig;

@Path("/secret")
public class SecretResource {
    @Inject
    SmallRyeConfig config;

    @GET
    public Response get() {
        return Response.ok().entity(config.getRawValue("my.secret")).build();
    }
}
