package au.com.innovativecoder.tradecustomerservicewebflux.controller;

import au.com.innovativecoder.tradecustomerservicewebflux.dto.CustomerInformation;
import au.com.innovativecoder.tradecustomerservicewebflux.dto.StockTradeRequest;
import au.com.innovativecoder.tradecustomerservicewebflux.dto.StockTradeResponse;
import au.com.innovativecoder.tradecustomerservicewebflux.entity.Customer;
import au.com.innovativecoder.tradecustomerservicewebflux.service.CustomerService;
import au.com.innovativecoder.tradecustomerservicewebflux.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final TradeService tradeService;

    @Autowired
    public CustomerController(CustomerService customerService, TradeService tradeService) {
        this.customerService = customerService;
        this.tradeService = tradeService;
    }

    /**
     * Retrieves customer information by customer ID.
     *
     * @param customerId the ID of the customer
     * @return a Mono emitting the CustomerInformation
     */
    @GetMapping("/{customerId}")
    public Mono<CustomerInformation> getCustomerInformation(@PathVariable("customerId") Integer customerId) {
        return customerService.getCustomerInformation(customerId);
    }

    /**
     * Handles the trading operation for a customer.
     *
     * @param customerId the ID of the customer
     * @param stockTradeRequestMono the request containing stock trade details
     * @return a Mono emitting the StockTradeResponse after the trade operation is executed
     */

    /*
    The method takes two parameters: customerId, which is extracted from the URL path,
    and stockTradeRequestMono, which is the request body containing the stock trade details wrapped in a Mono.
    The Mono type is part of Project Reactor, used for handling asynchronous operations in a reactive programming style.
     */
    @PostMapping("/{customerId}/trade")
    public Mono<StockTradeResponse> trade(@PathVariable("customerId") Integer customerId, @RequestBody Mono<StockTradeRequest> stockTradeRequestMono) {
        // the flatMap operator is used to transform the stockTradeRequestMono into another Mono by applying a
        // function to its value. The function takes the stockTradeRequest and calls the trade method of the TradeService class,
        // passing the customerId and stockTradeRequest as arguments.
        return stockTradeRequestMono.flatMap(stockTradeRequest -> tradeService.trade(customerId, stockTradeRequest));
    }
}
