/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosUserSettings;
import com.microsoft.azure.cosmos.CosmosDatabaseForTest;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.RequestOptions;

import reactor.core.publisher.Flux;

public class ReadFeedUsersTest extends TestSuiteBase {

    public final String databaseId = CosmosDatabaseForTest.generateId();
    private CosmosDatabase createdDatabase;

    private CosmosClient client;
    private List<CosmosUserSettings> createdUsers = new ArrayList<>();

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedUsersTest(CosmosClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readUsers() throws Exception {

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);

        Flux<FeedResponse<CosmosUserSettings>> feedObservable = createdDatabase.listUsers(options);

        int expectedPageSize = (createdUsers.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<CosmosUserSettings> validator = new FeedResponseListValidator.Builder<CosmosUserSettings>()
                .totalSize(createdUsers.size())
                .exactlyContainsInAnyOrder(createdUsers.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosUserSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdDatabase = createDatabase(client, databaseId);

        for(int i = 0; i < 5; i++) {
            createdUsers.add(createUsers(createdDatabase));
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }

    public CosmosUserSettings createUsers(CosmosDatabase cosmosDatabase) {
        CosmosUserSettings user = new CosmosUserSettings();
        user.setId(UUID.randomUUID().toString());
        return cosmosDatabase.createUser(user, new RequestOptions()).block().getCosmosUserSettings();
    }
}
