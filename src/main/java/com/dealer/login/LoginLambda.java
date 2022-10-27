package com.dealer.login;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.dealer.login.error.Error;
import com.dealer.login.model.Dealer;
import com.dealer.login.model.Response;
import com.dealer.login.service.DealerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Map;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class LoginLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDbClient dynamoDbClient;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public LoginLambda() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        dynamoDbClient = DependencyFactory.dynamoDbClient();
        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        //fetch the request body
        String body = requestEvent.getBody();

        final Dealer input = gson.fromJson(body, Dealer.class);
        context.getLogger().log("Dealer Obj" + input.getUsername());
        DealerService dealerService = new DealerService(dynamoDbClient, context);
        if(StringUtils.isEmpty(input.getUsername()) || StringUtils.isEmpty(input.getPassword())) {
            return returnResponse(HttpStatusCode.BAD_REQUEST, null, "Username or password is empty", "400", context);
        }
        try {
            Dealer dealer = dealerService.login(input);
            return returnResponse(HttpStatusCode.OK, gson.toJson(dealer), null,null,context);
        } catch (Exception e) {
            int httpStatusCode = HttpStatusCode.INTERNAL_SERVER_ERROR;
            String errorMessage = "DB Exception occured for user:"+input.getUsername();
            String errorCode = "500";
            if(e.getMessage() == "Invalid Credentials") {
                httpStatusCode = HttpStatusCode.BAD_REQUEST;
                errorMessage = "Invalid Credentials";
                errorCode = "400";
            }
            return returnResponse(httpStatusCode, null,errorMessage, errorCode, context);
        }
    }

    public APIGatewayProxyResponseEvent returnResponse(int statusCode, String responseBody,
            String errorMessage, String errorCode, Context context){
        Error error = new Error();
        if(errorCode != null){
            error.setErrorCode(errorCode);
            error.setErrorMessage(errorMessage);
        }

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(gson.toJson(new Response(statusCode, gson.fromJson(responseBody,Dealer.class), error)));
        responseEvent.setHeaders(Map.of("Content-Type", "application/json"));
        responseEvent.setHeaders(Map.of("Access-Control-Allow-Origin", "*"));
        responseEvent.setHeaders(Map.of("Allow", "POST, OPTIONS"));
        responseEvent.setHeaders(Map.of("Access-Control-Allow-Methods", "POST, OPTIONS"));
        responseEvent.setHeaders(Map.of("Access-Control-Allow-Headers", "*"));


        context.getLogger().log("\n" + responseEvent.toString());
        return responseEvent;

    }

}
