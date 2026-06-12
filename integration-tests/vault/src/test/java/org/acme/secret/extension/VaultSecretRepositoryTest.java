package org.acme.secret.extension;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@QuarkusTest
class VaultSecretRepositoryTest {

    @BeforeEach
    @AfterEach
    void tearDown() {
        given().when()
                .delete("/secret/{name}/tearDown", "my-secret")
                .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    void shouldStoreSecret() {
        String firstGet = given().when()
                .get("/secret/{name}", "my-secret")
                .then()
                .log().all()
                .statusCode(200)
                .extract().body().asString();
        String stored = given().when()
                .formParam("value", "my-value")
                .post("/secret/{name}/store", "my-secret")
                .then()
                .log().all()
                .statusCode(200)
                .extract().body().asString();
        String secondGet = given().when()
                .get("/secret/{name}", "my-secret")
                .then()
                .log().all()
                .statusCode(200)
                .extract().body().asString();

        assertAll(
                () -> assertThat(firstGet).isEqualTo("null"),
                () -> assertThat(stored).isEqualTo("my-value"),
                () -> assertThat(secondGet).isEqualTo("my-value")
        );
    }

    @Test
    void shouldFailToStoreSecretWhenAlreadyStored() {
        given().when()
                .formParam("value", "my-value")
                .post("/secret/{name}/store", "my-secret")
                .then()
                .log().all()
                .statusCode(200);
        String exception = given().when()
                .formParam("value", "my-value")
                .post("/secret/{name}/store", "my-secret")
                .then()
                .log().all()
                .statusCode(409)
                .extract().body().asString();
        assertThat(exception).isEqualTo("SecretAlreadyStoredException");
    }
}
