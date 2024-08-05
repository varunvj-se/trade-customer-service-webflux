package au.com.innovativecoder.tradecustomerservicewebflux.mapper;

import au.com.innovativecoder.tradecustomerservicewebflux.domain.Ticker;
import au.com.innovativecoder.tradecustomerservicewebflux.dto.CustomerInformation;
import au.com.innovativecoder.tradecustomerservicewebflux.dto.Holding;
import au.com.innovativecoder.tradecustomerservicewebflux.dto.StockTradeRequest;
import au.com.innovativecoder.tradecustomerservicewebflux.dto.StockTradeResponse;
import au.com.innovativecoder.tradecustomerservicewebflux.entity.Customer;
import au.com.innovativecoder.tradecustomerservicewebflux.entity.PortfolioItem;

import java.util.List;

public class EntityDtoMapper {

    public static CustomerInformation toCustomerInformation(Customer customer, List<PortfolioItem> items) {
        var holdings = items.stream()
                .map(i -> new Holding(i.getTicker(), i.getQuantity()))
                .toList();

        return new CustomerInformation(
                customer.getId(),
                customer.getName(),
                customer.getBalance(),
                holdings
        );
    }

    public static PortfolioItem toPortfolioItem(Integer customerId, Ticker ticker) {
        var portfolioItem = new PortfolioItem();
        portfolioItem.setCustomerId(customerId);
        portfolioItem.setTicker(ticker);
        portfolioItem.setQuantity(0);
        return portfolioItem;
    }

    public static StockTradeResponse toStockTradeResponse(StockTradeRequest stockTradeRequest, Integer customerId, Integer balance) {
        return new StockTradeResponse(
                customerId,
                stockTradeRequest.ticker(),
                stockTradeRequest.price(),
                stockTradeRequest.quantity(),
                stockTradeRequest.action(),
                stockTradeRequest.totalPrice(),
                balance
        );
    }
}
