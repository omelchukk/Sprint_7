package order;

import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ExtendWith(AllureJunit5.class)
public class OrderListTests {

    @BeforeEach
    @Step("Подготовка тестовых данных")
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Получение списка заказов")
    @Description("Проверка, что в тело ответа возвращается список заказов")
    public void getOrderList() {
        Response response = getOrderListResponse();
        validateOrderListResponse(response);
    }

    @Step("Выполнение запроса на получение списка заказов")
    private Response getOrderListResponse() {
        return given()
                .get("/api/v1/orders");
    }

    @Step("Проверка, что список заказов получен успешно")
    private void validateOrderListResponse(Response response) {
        response.then()
                .statusCode(200)
                .body("orders", instanceOf(List.class))
                .body("orders[0].id", greaterThan(0))
                .body("orders[0].courierId", not(empty()))
                .body("orders[0].firstName", not(emptyString()))
                .body("orders[0].lastName", not(emptyString()))
                .body("orders[0].address", not(emptyString()))
                .body("orders[0].metroStation", not(emptyString()))
                .body("orders[0].phone", not(emptyString()))
                .body("orders[0].rentTime", greaterThan(0))
                .body("orders[0].deliveryDate", not(emptyString()))
                .body("orders[0].comment", not(emptyString()))
                .body("orders[0].track", greaterThan(0))
                .body("orders[0].createdAt", not(emptyString()))
                .body("orders[0].updatedAt", not(emptyString()))
                .body("orders[0].status", greaterThanOrEqualTo(0))
                .body("pageInfo", not(emptyString()))
                .body("pageInfo[0].page", greaterThanOrEqualTo(0))
                .body("pageInfo[0].total", greaterThanOrEqualTo(0))
                .body("pageInfo[0].limit", greaterThan(0))
                .body("availableStations", instanceOf(List.class))
                .body("availableStations[0].name", not(emptyString()))
                .body("availableStations[0].number", not(emptyString()))
                .body("availableStations[0].color", not(emptyString()));
    }

    }

