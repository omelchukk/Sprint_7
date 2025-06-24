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

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(AllureJunit5.class)
@Epic("Order API")
@Feature("Create Order")
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

    static Stream<Arguments> orderDetailsTestData() {
        return Stream.of(
                Arguments.of("Эдвард", "Каллен", "Ул. Пушкина, д. Колотушкина", "4", "88005553535", 2, "2025-07-07", "Позвоните за 5 минут", List.of("BLACK")),
                Arguments.of("Райан", "Гослинг", "Ул. Вязов, д.13", "13", "89031234567", 3, "2025-09-09", "Оставьте у двери", List.of("GREY")),
                Arguments.of("Белла", "Свон", "Руставели, д.1", "25", "88003553535", 1, "2025-08-08", "Домофон не работает", List.of("BLACK", "GREY")),
                Arguments.of("Джон", "Порк", "Ул. Школьная, д.14", "1", "8916131211", 4, "2025-08-28", "Без комментариев", List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("orderDetailsTestData")
    @DisplayName("Успешное создание заказа")
    @Description("Проверка успешного создания заказа с разными вариантами цветов самокатов")
    public void createNewOrderAndCheckResponse(
            String firstName,
            String lastName,
            String address,
            String metroStation,
            String phone,
            int rentTime,
            String deliveryDate,
            String comment,
            List<String> color
    ) {
        Order order = new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);
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
                .body("track", notNullValue());

        Object orderTrack = response.path("track");
        if (orderTrack != null) {
            track = String.valueOf(orderTrack);
        }
    }

    @Step("Отмена заказа по треку {track}")
    private void cancelOrder(String track) {
        given()
                .header("Content-type", "application/json")
                .body("{\"track\": \"" + track + "\"}")
                .when()
                .put("/api/v1/orders/cancel")
                .then()
                .statusCode(200);
    }
}
