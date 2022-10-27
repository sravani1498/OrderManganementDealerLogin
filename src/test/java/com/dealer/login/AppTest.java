package com.dealer.login;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dealer.login.model.Dealer;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    public void handleRequest_shouldReturnConstantValue() {
        LoginLambda function = new LoginLambda();
        Dealer result = function.handleRequest(new Dealer(), null);
        assertNotNull(result);
    }
}
