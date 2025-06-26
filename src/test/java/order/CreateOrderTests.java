package order;

import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ExtendWith(AllureJunit5.class)
public class CreateOrderTests {

    private String track;

    @BeforeEach
    @Step("Подготовка тестовых данных")
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @AfterEach
    @Step("Отмена созданного заказа по треку: {track}")
    public void tearDown() {
        if (track != null && !track.equals("null")) {
            cancelOrder(track);
        }
    }

    static Stream<Arguments> scooterColors() {
        return Stream.of(
                Arguments.of(List.of("BLACK")),
                Arguments.of(List.of("GREY")),
                Arguments.of(List.of("BLACK", "GREY")),
                Arguments.of(List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("scooterColors")
    @DisplayName("Успешное создание заказа")
    @Description("Проверка успешного создания заказа с разными вариантами цветов самокатов")
    public void createNewOrderAndCheckResponse(List<String> color) {
    Order order = new Order("Эдвард", "Каллен", "Улица Пушкина, дом Колотушкина", "4", "88005553535", 2, "2025-07-07", "Позвоните за 5 минут", color );

        Response response = createOrder(order);
        validateOrderCreated(response);
    }


    @Step("Создание заказа")
    private Response createOrder(Order order) {
        return given()
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post("/api/v1/orders");
    }

    @Step("Проверка успешного создания заказа")
    private void validateOrderCreated(Response response) {
        response.then()
                .statusCode(201)
                .body("track", greaterThan(0));

        Object orderTrack = response.path("track");
            track = String.valueOf(orderTrack);
    }

    @Step("Отмена заказа по треку {track}")
    private void cancelOrder(String track) {
        Map<String, String> body = new HashMap<>();
        body.put("track", track);

        given()
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .put("/api/v1/orders/cancel")
                .then()
                .statusCode(200);
    }
}
