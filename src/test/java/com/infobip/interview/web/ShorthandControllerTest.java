package com.infobip.interview.web;

import com.infobip.interview.models.RequestWrapper;
import com.infobip.interview.models.User;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.notNullValue;


/**
 * Created by mikhail.davydov on 27.09.2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ShorthandControllerTest {

    private static final String LOCALHOST = "http://localhost";
    private static final int PORT = 8080;
    private static final String URL = "https://stackoverflow.com/questions/1567929/website-safe-data-access-architecture-question?rq=1";
    private static final String REDIRECT_TYPE = "302";

    @Before
    public void setUp() throws Exception {
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri(LOCALHOST)
                .setPort(PORT)
                .setContentType(ContentType.JSON)
                .build()
                .log().all();
    }

    // /account
    @Test
    public void usernameValid() throws Exception {
        String username = RandomStringUtils.randomAlphanumeric(6);
        RequestWrapper body = RequestWrapper.builder()
                .username(username)
                .build();

        given()
                .body(body)
                .when()
                .post("/account")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("success", equalTo(true))
                .body("description", equalTo("Your account is opened"))
                .body("password.length()", equalTo(8));
    }

    @Test
    public void usernameInvalidMediaType() throws Exception {
        String username = RandomStringUtils.randomAlphanumeric(6);
        RequestWrapper body = RequestWrapper.builder()
                .username(username)
                .build();

        given()
                .body(body)
                .contentType(ContentType.XML)
                .when()
                .post("/account")
                .then()
                .log().all()
                .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
    }

    @Test
    public void usernameInvalidUsername() throws Exception {
        String username = "user/user";
        RequestWrapper body = RequestWrapper.builder()
                .username(username)
                .build();

        given()
                .body(body)
                .when()
                .post("/account")
                .then()
                .log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void usernameTwice() throws Exception {
        String username = RandomStringUtils.randomAlphanumeric(6);
        RequestWrapper body = RequestWrapper.builder()
                .username(username)
                .build();

        given()
                .body(body)
                .when()
                .post("/account")
                .then()
                .log().all();

        given()
                .body(body)
                .when()
                .post("/account")
                .then()
                .log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    // /register
    @Test
    public void registerValid() throws Exception {
        User user = createUser();
        RequestWrapper body = RequestWrapper.builder()
                .url(URL)
                .build();

        given()
                .auth().basic(user.getUsername(), user.getPassword())
                .body(body)
                .when()
                .post("/register")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("shortUrl", notNullValue());
    }

    @Test
    public void registerNoAuth() throws Exception {
        RequestWrapper body = RequestWrapper.builder()
                .url(URL)
                .redirectType(REDIRECT_TYPE)
                .build();

        given()
                .body(body)
                .when()
                .post("/register")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void registerInvalidURL() throws Exception {
        User user = createUser();
        String invalidUrl = "InvalidUrlHere";
        RequestWrapper body = RequestWrapper.builder()
                .url(invalidUrl)
                .redirectType(REDIRECT_TYPE)
                .build();

        given()
                .auth().basic(user.getUsername(), user.getPassword())
                .body(body)
                .when()
                .post("/register")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void registerInvalidRedirectType() throws Exception {
        User user = createUser();
        String invalidRedirectType = "invalidRedirectTypeHere";
        RequestWrapper body = RequestWrapper.builder()
                .url(URL)
                .redirectType(invalidRedirectType)
                .build();

        given()
                .auth().basic(user.getUsername(), user.getPassword())
                .body(body)
                .when()
                .post("/register")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void registerInvalidContentType() throws Exception {
        User user = createUser();
        RequestWrapper body = RequestWrapper.builder()
                .url(URL)
                .build();

        given()
                .auth().basic(user.getUsername(), user.getPassword())
                .body(body)
                .contentType(ContentType.XML)
                .when()
                .post("/register")
                .then().log().all()
                .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
    }


    // /statistic/{AccountId}
    @Test
    public void statisticValid() throws Exception {
        User user = createUser();
        createShortUrl(user.getUsername(), user.getPassword());

        given()
                .auth().basic(user.getUsername(), user.getPassword())
                .pathParam("AccountId", user.getUsername())
                .when()
                .get("/statistic/{AccountId}")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("$", hasValue(0));
    }

    @Test
    public void statisticNoAuth() throws Exception {
        String username = RandomStringUtils.randomAlphanumeric(6);
        given()
                .pathParam("AccountId", username)
                .when()
                .get("/statistic/{AccountId}")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void statisticInvalidUser() throws Exception {
        User user = createUser();
        String invalidUser = "invalidUser";
        given()
                .auth().basic(user.getUsername(), user.getPassword())
                .pathParam("AccountId", invalidUser)
                .when()
                .get("/statistic/{AccountId}")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void statisticOtherUser() throws Exception {
        User user = createUser();
        User other = createUser();

        given()
                .auth().basic(user.getUsername(), user.getPassword())
                .pathParam("AccountId", other.getUsername())
                .when()
                .get("/statistic/{AccountId}")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }


    // /{url}
    @Test
    public void redirectValid() throws Exception {
        User user = createUser();
        String shortUrl = createShortUrl(user.getUsername(), user.getPassword());

        given()
                .auth().basic(user.getUsername(), user.getPassword())
                .when()
                .get(shortUrl)
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    public void redirectNoAuth() throws Exception {
        User user = createUser();
        String shortUrl = createShortUrl(user.getUsername(), user.getPassword());

        given()
                .when()
                .get(shortUrl)
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }


    // /help
    @Test
    public void help() throws Exception {
        given()
                .when()
                .get("/help")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    private User createUser() {
        String username = RandomStringUtils.randomAlphanumeric(6);
        RequestWrapper body = RequestWrapper.builder()
                .username(username)
                .build();
        String password = given().body(body).post("/account").then().log().all().extract().path("password");
        return User.builder().username(username).password(password).build();
    }

    private String createShortUrl(String username, String password) {
        RequestWrapper body = RequestWrapper.builder()
                .url(URL)
                .build();
        return given().auth().basic(username, password).body(body).post("/register").then().log().all().extract().path("shortUrl");
    }
}