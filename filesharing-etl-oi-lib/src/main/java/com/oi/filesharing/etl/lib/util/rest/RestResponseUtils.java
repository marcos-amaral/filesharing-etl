package com.oi.filesharing.etl.lib.util.rest;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RequestScoped
public class RestResponseUtils {

    private static final Logger logger = LogManager.getLogger();

    public static ResponseBuilder createAlert(ResponseBuilder builder, String message, String param) {
        builder.header("X-app-alert", message);
        builder.header("X-app-params", param);
        return builder;
    }

    public static ResponseBuilder createEntityCreationAlert(ResponseBuilder builder, String entityName, String param) {
        return createAlert(builder, "app." + entityName + ".created", param);
    }

    public static ResponseBuilder createEntityUpdateAlert(ResponseBuilder builder, String entityName, String param) {
        return createAlert(builder, "app." + entityName + ".updated", param);
    }

    public static ResponseBuilder createEntityDeletionAlert(ResponseBuilder builder, String entityName, String param) {
        return createAlert(builder, "app." + entityName + ".deleted", param);
    }

    public static ResponseBuilder createFailureAlert(ResponseBuilder builder, String entityName, String errorKey, String defaultMessage) {
        builder.header("X-app-error", "error." + errorKey);
        builder.header("X-app-params", entityName);
        return builder;
    }

    public Response createResponse(RestMethodEnum restMethodEnum, String entityName, Object responseEntity) {
        ResponseBuilder response;
        switch (restMethodEnum) {
            case GET:
                if (responseEntity != null) {
                    response = Response.status(Response.Status.OK).entity(responseEntity);
                    logger.debug("Entity Found:{}||Response:{}", response, responseEntity);
                } else {
                    response = Response.status(Response.Status.NOT_FOUND);
                    createFailureAlert(response, entityName, "404", "Item não encontrado.");
                    logger.debug("Entity Not Found:{}", response);
                }
                break;
            case GET_ALL:
                response = Response.status(Response.Status.OK).entity(responseEntity);
                logger.debug("Entity Found:{}||Response:{}", response, responseEntity);
                break;
            case PUT:
                if (responseEntity != null) {
                    response = Response.status(Response.Status.OK).entity(responseEntity);
                    createEntityUpdateAlert(response, entityName, responseEntity.toString());
                    logger.debug("Entity Updated:{}||Response:{}", response, responseEntity);
                } else {
                    response = Response.status(Response.Status.NOT_FOUND);
                    createFailureAlert(response, entityName, "404", "Item não encontrado.");
                    logger.debug("Entity Not Found:{}", response);
                }
                break;
            case DELETE:
                if (responseEntity != null) {
                    response = Response.status(Response.Status.OK).entity(responseEntity);
                    createEntityDeletionAlert(response, entityName, responseEntity.toString());
                    logger.debug("Entity Deleted:{}||Response:{}", response, responseEntity);
                } else {
                    response = Response.status(Response.Status.NOT_FOUND);
                    createFailureAlert(response, entityName, "404", "Item não encontrado.");
                    logger.debug("Entity Not Found:{}", response);
                }
                break;
            case POST:
                response = Response.status(Response.Status.CREATED).entity(responseEntity);
                createEntityCreationAlert(response, entityName, responseEntity.toString());
                logger.debug("Entity Created:{}||Response:{}", response, responseEntity);
                break;
            default:
                throw new AssertionError(restMethodEnum.name());
        }
        return response.build();
    }

    public Response createErrorResponse(RestMethodEnum restMethodEnum, String entityName, Exception e) {
        ResponseBuilder response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e);
        createFailureAlert(response, entityName, "500", "Falha não esperada.");
        return response.build();
    }

}
