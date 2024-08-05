package au.com.innovativecoder.tradecustomerservicewebflux;

import au.com.innovativecoder.tradecustomerservicewebflux.domain.Ticker;
import au.com.innovativecoder.tradecustomerservicewebflux.domain.TradeAction;
import au.com.innovativecoder.tradecustomerservicewebflux.dto.StockTradeRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

@SpringBootTest
@AutoConfigureWebTestClient
class TradeCustomerServiceWebfluxApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(TradeCustomerServiceWebfluxApplicationTests.class);

    @Autowired
    public WebTestClient client;


    @Test
    void testCustomerInformation() {
        getCustomer(1, HttpStatus.OK)
                .jsonPath("$.name").isEqualTo("Sam")
                .jsonPath("$.balance").isEqualTo("10000")
                .jsonPath("$.holdings").isEmpty();
    }

    @Test
    void testBuyAndSell() {
        var buyRequest1 = new StockTradeRequest(Ticker.BABATATA, 100, 5, TradeAction.BUY);

        trade(2, buyRequest1, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo( 9500)
                .jsonPath("$.totalPrice").isEqualTo(500);

        var buyRequest2 = new StockTradeRequest(Ticker.BABATATA, 100, 10, TradeAction.BUY);

        trade(2, buyRequest2, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo( 8500)
                .jsonPath("$.totalPrice").isEqualTo(1000);

        var buyRequest3 = new StockTradeRequest(Ticker.GOOGLE, 1000, 3, TradeAction.BUY);

        trade(2, buyRequest3, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo( 5500)
                .jsonPath("$.totalPrice").isEqualTo(3000);

        getCustomer(2, HttpStatus.OK)
                .jsonPath("$.holdings").isNotEmpty()
                .jsonPath("$.holdings.length()").isEqualTo(2)
                .jsonPath("$.holdings[0].ticker").isEqualTo("BABATATA")
                .jsonPath("$.holdings[0].quantity").isEqualTo(15)
                .jsonPath("$.holdings[1].ticker").isEqualTo("GOOGLE")
                .jsonPath("$.holdings[1].quantity").isEqualTo(3);

        var sellRequest1 = new StockTradeRequest(Ticker.BABATATA, 110, 5, TradeAction.SELL);

        trade(2, sellRequest1, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo( 6050)
                .jsonPath("$.totalPrice").isEqualTo(550);

        var sellRequest2 = new StockTradeRequest(Ticker.BABATATA, 110, 10, TradeAction.SELL);

        trade(2, sellRequest2, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo( 7150)
                .jsonPath("$.totalPrice").isEqualTo(1100);

        getCustomer(2, HttpStatus.OK)
                .jsonPath("$.holdings").isNotEmpty()
                .jsonPath("$.holdings.length()").isEqualTo(2)
                .jsonPath("$.holdings[0].ticker").isEqualTo("BABATATA")
                .jsonPath("$.holdings[0].quantity").isEqualTo(0)
                .jsonPath("$.holdings[1].ticker").isEqualTo("GOOGLE")
                .jsonPath("$.holdings[1].quantity").isEqualTo(3);

    }

    @Test
    void testCustomerNotFound() {
        getCustomer(10, HttpStatus.NOT_FOUND)
                .jsonPath("$.detail", "Customer [id=10] is not found");

        var sellRequest1 = new StockTradeRequest(Ticker.BABATATA, 110, 5, TradeAction.SELL);

        trade(10, sellRequest1,  HttpStatus.NOT_FOUND)
                .jsonPath("$.detail", "Customer [id=10] is not found");

        var buyRequest1 = new StockTradeRequest(Ticker.BABATATA, 100, 5, TradeAction.BUY);

        trade(10, buyRequest1, HttpStatus.NOT_FOUND)
                .jsonPath("$.detail", "Customer [id=10] is not found");

    }

    @Test
    void testInsufficientBalance() {
        var buyRequest1 = new StockTradeRequest(Ticker.BABATATA, 1000, 12, TradeAction.BUY);

        trade(1, buyRequest1, HttpStatus.BAD_REQUEST)
                .jsonPath("$.detail", "Customer [id=1] does not have enough funds to carry this transaction");
    }

    @Test
    void testInsufficientShares() {
        var buyRequest = new StockTradeRequest(Ticker.BABATATA, 100, 5, TradeAction.BUY);

        trade(3, buyRequest, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo(9500)
                .jsonPath("$.totalPrice").isEqualTo(500);

        var sellRequest1 = new StockTradeRequest(Ticker.BABATATA, 110, 5, TradeAction.SELL);

        trade(3, sellRequest1, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo( 10050)
                .jsonPath("$.totalPrice").isEqualTo(550);

        var sellRequest2 = new StockTradeRequest(Ticker.BABATATA, 110, 1, TradeAction.SELL);

        trade(3, sellRequest2, HttpStatus.BAD_REQUEST)
                .jsonPath("$.detail", "Customer [id=3] does not have enough shares to complete this transaction");
    }

    private WebTestClient.BodyContentSpec getCustomer(Integer customerId, HttpStatus expectedStatus) {
        return this.client
                .get()
                .uri("/customers/{customerId}", customerId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
                .consumeWith(e -> log.info("{} ", new String(Objects.requireNonNull(e.getResponseBody()))));
    }

    private WebTestClient.BodyContentSpec trade(Integer customerId, StockTradeRequest tradeRequest, HttpStatus expectedStatus) {
        return this.client
                .post()
                .uri("/customers/{customerId}/trade", customerId)
                .bodyValue(tradeRequest)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
                .consumeWith(e -> log.info("{} ", new String(Objects.requireNonNull(e.getResponseBody()))));
    }
}
