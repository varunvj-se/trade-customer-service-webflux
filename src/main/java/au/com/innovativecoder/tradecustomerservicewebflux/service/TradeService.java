package au.com.innovativecoder.tradecustomerservicewebflux.service;

import au.com.innovativecoder.tradecustomerservicewebflux.dto.StockTradeRequest;
import au.com.innovativecoder.tradecustomerservicewebflux.dto.StockTradeResponse;
import au.com.innovativecoder.tradecustomerservicewebflux.entity.Customer;
import au.com.innovativecoder.tradecustomerservicewebflux.entity.PortfolioItem;
import au.com.innovativecoder.tradecustomerservicewebflux.exceptions.ApplicationExceptions;
import au.com.innovativecoder.tradecustomerservicewebflux.mapper.EntityDtoMapper;
import au.com.innovativecoder.tradecustomerservicewebflux.repository.CustomerRepository;
import au.com.innovativecoder.tradecustomerservicewebflux.repository.PortfolioItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    private final CustomerRepository customerRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    @Autowired
    public TradeService(CustomerRepository customerRepository, PortfolioItemRepository portfolioItemRepository) {
        this.customerRepository = customerRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    /**
     * Handles the trading operation for a customer based on the action specified in the request.
     *
     * @param customerId the ID of the customer
     * @param stockTradeRequest the request containing stock trade details
     * @return a Mono emitting the StockTradeResponse after the trade operation is executed
     */
    @Transactional
    public Mono<StockTradeResponse> trade(Integer customerId, StockTradeRequest stockTradeRequest) {
        return switch (stockTradeRequest.action()) {
            case BUY -> this.buyStock(customerId, stockTradeRequest);
            case SELL -> this.sellStock(customerId, stockTradeRequest);
        };
    }

    /**
     * Handles the process of buying stock for a customer.
     *
     * @param customerId the ID of the customer
     * @param stockTradeRequest the request containing stock trade details
     * @return a Mono emitting the StockTradeResponse after the buy operation is executed
     */
    private Mono<StockTradeResponse> buyStock(Integer customerId, StockTradeRequest stockTradeRequest) {
        // Retrieve the customer by their ID
        var customerMono = this.customerRepository.findById(customerId)
                // If the customer is not found, throw a customerNotFound exception
                .switchIfEmpty(ApplicationExceptions.customerNotFound(customerId))
                .doOnNext(customer -> log.info("Customer {} buying {} at a price of {}", customerId, stockTradeRequest.ticker(), stockTradeRequest.price()))
                // Filter the customer based on whether they have sufficient balance for the trade
                .filter(c -> c.getBalance() >= stockTradeRequest.totalPrice())
                // If the customer does not have sufficient balance, throw an insufficientBalance exception
                .switchIfEmpty(ApplicationExceptions.insufficientBalance(customerId));

        // Retrieve the portfolio item by customer ID and ticker
        var portfolioItemMono = this.portfolioItemRepository.findByCustomerIdAndTicker(customerId, stockTradeRequest.ticker())
                // If the portfolio item is not found, create a new one
                .defaultIfEmpty(EntityDtoMapper.toPortfolioItem(customerId, stockTradeRequest.ticker()));

        // Zip the customer and portfolio item Monos and execute the buy operation
        return customerMono.zipWhen(customer -> portfolioItemMono)
                .flatMap(t -> this.executeBuy(t.getT1(), t.getT2(), stockTradeRequest));
    }

    /**
     * Executes the buy operation by updating the customer's balance and portfolio item quantity.
     *
     * @param customer the customer entity
     * @param portfolioItem the portfolio item entity
     * @param stockTradeRequest the request containing stock trade details
     * @return a Mono emitting the StockTradeResponse after the buy operation is executed
     */
    private Mono<StockTradeResponse> executeBuy(Customer customer, PortfolioItem portfolioItem, StockTradeRequest stockTradeRequest){
        customer.setBalance(customer.getBalance() - stockTradeRequest.totalPrice());
        portfolioItem.setQuantity(portfolioItem.getQuantity() + stockTradeRequest.quantity());
        return this.saveAndBuildResponse(customer, portfolioItem, stockTradeRequest);
    }

    /**
     * Handles the process of selling stock for a customer.
     *
     * @param customerId the ID of the customer
     * @param stockTradeRequest the request containing stock trade details
     * @return a Mono emitting the StockTradeResponse after the sell operation is executed
     */
    private Mono<StockTradeResponse> sellStock(Integer customerId, StockTradeRequest stockTradeRequest){
        // Retrieve the customer by their ID
        var customerMono = this.customerRepository.findById(customerId)
                // If the customer is not found, throw a customerNotFound exception
                .switchIfEmpty(ApplicationExceptions.customerNotFound(customerId));

        // Retrieve the portfolio item by customer ID and ticker
        var portfolioItem = this.portfolioItemRepository.findByCustomerIdAndTicker(customerId, stockTradeRequest.ticker())
                // Filter the portfolio item based on whether it has sufficient shares for the trade
                .filter(p -> p.getQuantity() >= stockTradeRequest.quantity())
                .doOnNext(customer -> log.info("Customer {} selling {} at a price of {}", customerId, stockTradeRequest.ticker(), stockTradeRequest.price()))
                // If the portfolio item does not have sufficient shares, throw an insufficientShares exception
                .switchIfEmpty(ApplicationExceptions.insufficientShares(customerId));

        // Zip the customer and portfolio item Monos and execute the sell operation
        return customerMono.zipWhen(customer -> portfolioItem)
                .flatMap(t -> this.executeSell(t.getT1(), t.getT2(), stockTradeRequest));
    }

    /**
     * Executes the sell operation by updating the customer's balance and portfolio item quantity.
     *
     * @param customer the customer entity
     * @param portfolioItem the portfolio item entity
     * @param stockTradeRequest the request containing stock trade details
     * @return a Mono emitting the StockTradeResponse after the sell operation is executed
     */
    private Mono<StockTradeResponse> executeSell(Customer customer, PortfolioItem portfolioItem, StockTradeRequest stockTradeRequest){
        customer.setBalance(customer.getBalance() + stockTradeRequest.totalPrice());
        portfolioItem.setQuantity(portfolioItem.getQuantity() - stockTradeRequest.quantity());
        return this.saveAndBuildResponse(customer, portfolioItem, stockTradeRequest);
    }

    /**
     * Saves the customer and portfolio item entities and builds the StockTradeResponse.
     *
     * @param customer the customer entity
     * @param portfolioItem the portfolio item entity
     * @param stockTradeRequest the request containing stock trade details
     * @return a Mono emitting the StockTradeResponse after saving the entities
     */
    private Mono<StockTradeResponse> saveAndBuildResponse(Customer customer, PortfolioItem portfolioItem, StockTradeRequest stockTradeRequest) {
        var response = EntityDtoMapper.toStockTradeResponse(stockTradeRequest, customer.getId(), customer.getBalance());
        return Mono.zip(
                this.customerRepository.save(customer),
                this.portfolioItemRepository.save(portfolioItem)
        ).thenReturn(response);
    }
}
