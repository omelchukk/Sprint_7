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
public class CreateCourierTest {
    private String login;
    private String password;
    private String firstName;
    private String id;

    @BeforeEach
    @Step("Подготовка тестовых данных")
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        this.login = "courier_" + UUID.randomUUID();
        this.password = "pass_" + UUID.randomUUID();
        this.firstName = "name_" + UUID.randomUUID();
    }

    @AfterEach
    @Step("Удаление созданных курьеров")
    public void tearDown() {
        if (id != null) {
            deleteCourier(id);
        }
    }

    @Test
    @DisplayName("Успешное создание курьера")
    @Description("Cоздание курьера с валидными данными и проверка кода и тела ответа")
    public void createNewCourierAndCheckResponse() {
        Courier courier = new Courier(login, password, firstName);
        Response response = createCourier(courier);
        validateCourierCreated(response);
    }

    @Test
    @DisplayName("Создание двух одинаковых курьеров")
    @Description("Создаем одного курьера с валидными данными, а затем пробуем создать точно такого же. В результате должна быть ошибка.")
    public void createTwoSameCouriers() {
        Courier courier = new Courier(login, password, firstName);
        Response first = createCourier(courier);
        validateCourierCreated(first);

        Response second = createCourier(courier);
        validateDuplicateCourierError(second);
    }

    @Test
    @DisplayName("Создание курьера без логина")
    @Description("Попытка создать курьера без ввода логина. В результате должна быть ошибка.")
    public void createCourierWithoutLogin() {
        Courier courier = new Courier("", password, firstName);
        Response response = createCourier(courier);
        validateMissingFieldError(response);
    }

    @Test
    @DisplayName("Создание курьера без пароля")
    @Description("Попытка создать курьера без ввода пароля. В результате должна быть ошибка.")
    public void createCourierWithoutPassword() {
        Courier courier = new Courier(login, "", firstName);
        Response response = createCourier(courier);
        validateMissingFieldError(response);
    }

    @Test
    @DisplayName("Создание курьера без имени")
    @Description("Попытка создать курьера без ввода имени. Курьер должен быть создан без проблем.")
    public void createCourierWithoutFirstName() {
        Courier courier = new Courier(login, password, "");
        Response response = createCourier(courier);
        validateCourierCreated(response);
    }

    @Step("Создание курьера: {courier.login}, {courier.password}, {courier.firstName}")
    private Response createCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier");
    }

    @Step("Проверка, что курьер успешно создан")
    private void validateCourierCreated(Response response) {
        response.then().statusCode(201).body("ok", equalTo(true));
        Object courierId = response.path("id");
        if (courierId != null) {
            id = String.valueOf(courierId);
        }
    }

    @Step("Проверка ошибки: дублирование логина")
    private void validateDuplicateCourierError(Response response) {
        response.then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется"));
    }

    @Step("Проверка ошибки: отсутствует обязательное поле")
    private void validateMissingFieldError(Response response) {
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Step("Удаление курьера по id = {id}")
    private void deleteCourier(String id) {
        given()
                .header("Content-type", "application/json")
                .when()
                .delete("/api/v1/courier/" + id)
                .then()
                .statusCode(200);
    }
}
