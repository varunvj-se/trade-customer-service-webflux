package au.com.innovativecoder.tradecustomerservicewebflux.dto;

import au.com.innovativecoder.tradecustomerservicewebflux.domain.Ticker;
import au.com.innovativecoder.tradecustomerservicewebflux.domain.TradeAction;

public record StockTradeRequest(Ticker ticker, Integer price, Integer quantity, TradeAction action) {

    public Integer totalPrice() {
        return price * quantity;
    }
}
