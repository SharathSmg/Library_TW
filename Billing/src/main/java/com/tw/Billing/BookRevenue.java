package com.tw.Billing;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BookRevenue {
	int bookId;
	int revenue;
	public int getBookId() {
		return bookId;
	}
	public void setBookId(int bookId) {
		this.bookId = bookId;
	}
	public int getRevenue() {
		return revenue;
	}
	public void setRevenue(int revenue) {
		this.revenue = revenue;
	}
	
	@Override
	public String toString(){
		return "INFO [bookId="+bookId+", revenue="+revenue+"]";
	}
}
