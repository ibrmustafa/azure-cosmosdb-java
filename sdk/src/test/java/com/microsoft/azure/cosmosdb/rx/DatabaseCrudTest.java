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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosClient.Builder;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosDatabaseRequestOptions;
import com.microsoft.azure.cosmos.CosmosDatabaseResponse;
import com.microsoft.azure.cosmos.CosmosDatabaseSettings;
import com.microsoft.azure.cosmos.CosmosResponseValidator;
import com.microsoft.azure.cosmos.CosmosDatabaseForTest;

import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class DatabaseCrudTest extends TestSuiteBase {
    private final String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private final List<String> databases = new ArrayList<>();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public DatabaseCrudTest(Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createDatabase() throws Exception {
        CosmosDatabaseSettings databaseDefinition = new CosmosDatabaseSettings(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        // create the database
        Mono<CosmosDatabaseResponse> createObservable = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
                .withId(databaseDefinition.getId()).build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createDatabase_AlreadyExists() throws Exception {
        CosmosDatabaseSettings databaseDefinition = new CosmosDatabaseSettings(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block();

        // attempt to create the database
        Mono<CosmosDatabaseResponse> createObservable = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceAlreadyExists().build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readDatabase() throws Exception {
        // read database
        Mono<CosmosDatabaseResponse> readObservable = client.getDatabase(preExistingDatabaseId).read();

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
                .withId(preExistingDatabaseId).build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readDatabase_DoesntExist() throws Exception {
        // read database
        Mono<CosmosDatabaseResponse> readObservable = client.getDatabase("I don't exist").read();

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, validator);
    }


    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteDatabase() throws Exception {
        // create the database
        CosmosDatabaseSettings databaseDefinition = new CosmosDatabaseSettings(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());
        CosmosDatabase database = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block().getDatabase();

        // delete the database
        Mono<CosmosDatabaseResponse> deleteObservable = database.delete();

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteDatabase_DoesntExist() throws Exception {
        // delete the database
        Mono<CosmosDatabaseResponse> deleteObservable = client.getDatabase("I don't exist").delete();

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(deleteObservable, validator);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdDatabase = createDatabase(client, preExistingDatabaseId);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        for(String dbId: databases) {
            safeDeleteDatabase(client.getDatabase(dbId));
        }
        safeClose(client);
    }
}
