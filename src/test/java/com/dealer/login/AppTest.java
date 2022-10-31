package com.dealer.login;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.dealer.login.model.Dealer;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    public void handleRequest_shouldReturnConstantValue() {
        LoginLambda function = new LoginLambda();
        APIGatewayProxyResponseEvent result = function.handleRequest(new APIGatewayProxyRequestEvent(), null);
        assertNotNull(result);
    }
}
