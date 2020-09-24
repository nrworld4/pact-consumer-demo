package com.snap.pactconsumerdemo;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "DataProvider", port = "1234")
public class PactTest {

    // This defines the expected interaction for out test
    @Pact(provider = "DataProvider", consumer = "DataConsumer")
    public RequestResponsePact pact(PactDslWithProvider builder) {
        Map<String, String> responseHeader = new HashMap<>();
        return builder
                .given("some state")
                .uponReceiving("a request for json data")
                .path("/get/ice/2.0")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(responseHeader)
//                .body("{\"name\":\"ice\",\"price\":1.0}")
                .body(new PactDslJsonBody()
                                .valueFromProviderState("name", "\\${name}", "some name")
                                .valueFromProviderState("price", "\\${price}", 10.0)
                )
                .toPact();
    }

    @Test
    void test(MockServer mockServer) throws IOException, ProcessingException {
        HttpResponse httpResponse = Request.Get(mockServer.getUrl() + "/get/ice/2.0").execute().returnResponse();
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.newBuilder().freeze();

        JsonNode json = objectMapper.readTree(httpResponse.getEntity().getContent());
        log.info(json.toPrettyString());
        JsonSchema schema = schemaFactory.getJsonSchema(objectMapper.readTree(this.getClass().getResourceAsStream("/test/DataModel.schema")));
        var validationReport = schema.validate(json);

        // print validation errors
        if (validationReport.isSuccess()) {
            log.info("Validation succeeded");
        } else {
            validationReport.forEach(q-> fail(q.getMessage()));
        }

    }

}
