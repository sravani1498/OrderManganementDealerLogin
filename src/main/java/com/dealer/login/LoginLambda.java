package com.dealer.login;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.dealer.login.exceptions.BadRequestException;
import com.dealer.login.model.Dealer;
import com.dealer.login.model.Response;
import com.dealer.login.service.DealerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.utils.StringUtils;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class LoginLambda implements RequestHandler<Dealer, Dealer> {
    private final DynamoDbClient dynamoDbClient;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public LoginLambda() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        dynamoDbClient = DependencyFactory.dynamoDbClient();
        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
    }

    public Dealer handleRequest(Dealer input, Context context) {

        DealerService dealerService = new DealerService(dynamoDbClient, context);
        if(StringUtils.isEmpty(input.getUsername()) || StringUtils.isEmpty(input.getPassword())) {
            throw new BadRequestException("Username or password is empty");
        }
        return dealerService.login(input);
    }
}
