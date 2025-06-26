package courier;

import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ExtendWith(AllureJunit5.class)
public class LoginCourierTest {
    private String login;
    private String password;
    private String firstName;
    private String id;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        this.login = "courier_" + UUID.randomUUID();
        this.password = "pass_" + UUID.randomUUID();
        this.firstName = "name_" + UUID.randomUUID();
    }

    @AfterEach
    public void tearDown() {
        if (id != null && !id.equals("null")) {
            deleteCourierById(id);
        }
    }

    @Test
    @DisplayName("Успешная авторизация")
    @Description("Успешная авторизация с валидными данными и проверка кода и тела ответа")
    public void loginSuccessfullyAndCheckResponse() {
        createCourier(login, password, firstName);
        CourierLogin courier = new CourierLogin(login, password);
        Response response = loginCourier(courier);
        validateSuccessfulLogin(response);
    }

    @Test
    @DisplayName("Авторизация без логина")
    @Description("Попытка авторизации без ввода логина. В результате должна быть ошибка")
    public void loginWithoutLogin() {
        CourierLogin courier = new CourierLogin("", password);
        Response response = loginCourier(courier);
        validateErrorResponse(response, 400, "Недостаточно данных для входа");
    }

    @Test
    @DisplayName("Авторизация без пароля")
    @Description("Попытка авторизации без ввода пароля. В результате должна быть ошибка")
    public void loginWithoutPassword() {
        CourierLogin courier = new CourierLogin(login, "");
        Response response = loginCourier(courier);
        validateErrorResponse(response, 400, "Недостаточно данных для входа");
    }

    @Test
    @DisplayName("Неправильные данные или несуществующий пользователь")
    @Description("Создание курьера и затем попытка залогиниться с ошибкой в данных. В результате получиться несуществующий пользователь и авторизация не пройдет.")
    public void loginWithInvalidCredentials() {
        createCourier("johnpork1", "1234", firstName);
        CourierLogin courier = new CourierLogin("johnpork2", "12345");
        Response wrongLogin = loginCourier(courier);
        validateErrorResponse(wrongLogin, 404, "Учетная запись не найдена");
    }

    @Step("Создание курьера {login}, {password}, {firstName}")
    private void createCourier(String login, String password, String firstName) {
        CourierCreate courier = new CourierCreate(login, password, firstName);
        Response response = given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier");

        response.then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Step("Авторизация курьера")
    private Response loginCourier(CourierLogin courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier/login");

    }

    @Step("Проверка успешной авторизации")
    private void validateSuccessfulLogin(Response response) {
        response.then()
                .statusCode(200)
                .body("id", greaterThan(1));

        Object courierId = response.path("id");
        if (courierId != null) {
            id = String.valueOf(courierId);
        }
    }

    @Step("Проверка ошибки: ожидаемый код {status}, сообщение '{message}'")
    private void validateErrorResponse(Response response, int status, String message) {
        response.then()
                .statusCode(status)
                .body("message", equalTo(message));
    }

    @Step("Удаление курьера по id = {id}")
    private void deleteCourierById(String id) {
        given()
                .header("Content-type", "application/json")
                .when()
                .delete("/api/v1/courier/" + id)
                .then()
                .statusCode(200);
    }
}