package au.com.innovativecoder.tradecustomerservicewebflux.dto;

import au.com.innovativecoder.tradecustomerservicewebflux.domain.Ticker;

public record Holding(Ticker ticker, Integer quantity) {
}
