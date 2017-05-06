package com.tw.Billing;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Revenue {
	int amount;
	String currencyType;
	
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getCurrencyType() {
		return currencyType;
	}
	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}
	
	@Override
	public String toString(){
		return "INFO [amount="+amount+", currencyType="+currencyType+"]";
	}
}