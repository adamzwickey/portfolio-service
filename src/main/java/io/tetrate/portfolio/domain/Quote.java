package io.tetrate.portfolio.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Quote {
	@JsonProperty("companyName")
	private String name;
	@JsonProperty("symbol")
	private String symbol;
	@JsonProperty("latestPrice")
	private Double lastPrice;
	private Double change;
	private Double low;
	private Double high;
	private String status;
	private String currency;

	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Double getLastPrice() {
		return lastPrice;
	}
	public void setLastPrice(Double lastPrice) {
		this.lastPrice = lastPrice;
	}
	
	@Override
	public String toString() {
		return "Quote [change=" + change + ", high=" + high + ", lastPrice=" + lastPrice + ", low=" + low + ", name="
				+ name + ", symbol=" + symbol + "]";
	}
	public Double getChange() {
		return change;
	}
	public void setChange(Double change) {
		this.change = change;
	}
	public Double getLow() {
		return low;
	}
	public void setLow(Double low) {
		this.low = low;
	}
	public Double getHigh() {
		return high;
	}
	public void setHigh(Double high) {
		this.high = high;
	}

}