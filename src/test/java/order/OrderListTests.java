package order;

import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

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
                .body("orders", notNullValue());
    }
}

