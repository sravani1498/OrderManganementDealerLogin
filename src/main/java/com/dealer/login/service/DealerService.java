package com.dealer.login.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.dealer.login.exceptions.BadRequestException;
import com.dealer.login.exceptions.DBException;
import com.dealer.login.model.Dealer;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DealerService {
    private Context context;
    private DynamoDbEnhancedClient enhancedClient;
    public DealerService(DynamoDbClient client, Context context) {
        this.context = context;
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }

    public Dealer  login(Dealer input) throws RuntimeException{
        Dealer dealer = new Dealer();

        try {
            DynamoDbTable<Dealer> table = enhancedClient.table("dealer", TableSchema.fromBean(Dealer.class));
            Key key = Key.builder()
                    .partitionValue(input.getUsername())
                    .build();

            // Get the item by using the key.
            Dealer result = table.getItem(
                    (GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));
            String encryptedPassword = encryptPassword(input.getPassword());
            context.getLogger().log("password" + encryptedPassword);
            if (result != null && result.getPassword().equals(encryptedPassword)) {
                dealer.setMessage("Login Success");
                dealer.setDealerId(result.getDealerId());
            } else {
                throw new BadRequestException("Invalid Credentials");
            }
        } catch(DynamoDbException e) {
            context.getLogger().log(e.getMessage());
            throw new DBException("DB operation failed");
        }
        return dealer;
    }

    public  String encryptPassword(String password) {
        try {
            /* MessageDigest instance for MD5. */
            MessageDigest m = MessageDigest.getInstance("MD5");

            /* Add plain-text password bytes to digest using MD5 update() method. */
            m.update(password.getBytes());

            /* Convert the hash value into bytes */
            byte[] bytes = m.digest();

            /* The bytes array has bytes in decimal form. Converting it into hexadecimal format. */
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                s.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            /* Complete hashed password in hexadecimal format */
            return s.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


}
