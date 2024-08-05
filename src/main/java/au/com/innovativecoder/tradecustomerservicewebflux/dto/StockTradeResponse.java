package au.com.innovativecoder.tradecustomerservicewebflux.dto;

import au.com.innovativecoder.tradecustomerservicewebflux.domain.Ticker;
import au.com.innovativecoder.tradecustomerservicewebflux.domain.TradeAction;

public record StockTradeResponse(Integer customer, Ticker ticker, Integer price, Integer quantity, TradeAction action, Integer totalPrice, Integer balance) {
}
