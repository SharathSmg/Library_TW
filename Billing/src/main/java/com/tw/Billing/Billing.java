
package com.tw.Billing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.print.attribute.standard.DateTimeAtCompleted;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// The Java class will be hosted at the URI path "/myresource"
@Path("/billing")
public class Billing {
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    // TODO: update the class to suit your needs
    
    // The Java method will process HTTP GET requests
    @GET 
    // The Java method will produce content identified by the MIME Media
    // type "text/plain"
    @Produces("text/plain")
    public String getIt() {
        return "Got it!";
    }
    
    @GET
	@Path("/amount")
	@Produces(MediaType.APPLICATION_JSON)
	public Revenue getAmount(@QueryParam("userId") Integer userId, @QueryParam("bookId") Integer bookId, 
								@QueryParam("rentedDays") Integer daysRentedFor){
		
    	int bookPrice = 10000;
    	int rent = 0;
    	if(daysRentedFor < 5){
    		rent = Double.valueOf(bookPrice * 0.001).intValue() ;
    	}else if(daysRentedFor < 10){
    		rent = Double.valueOf(bookPrice * 0.002).intValue();
    	}else{
    		rent = Double.valueOf(bookPrice * 0.005).intValue();
    	}
    	
    	Revenue r = new Revenue();
		r.amount=rent;
		r.currencyType="Rupee";
		
		updateUserBalance(userId, rent);
		updateRevenueTable(bookId, rent);
		
		return r;
	}
	
    private void updateRevenueTable(int bookId, int rent){
    	Connection con = JDBCConnection.getConnection();
		String query = "insert into book_revenue (id, amountPaid, paidDate) values (?, ?, ?)";
		try{
			PreparedStatement preparedStmt ;
			PreparedStatement p = con.prepareStatement(query);
			Date dateToday = new Date();
		    Date utilDate = new SimpleDateFormat("yyyy/MM/dd").parse(sdf.format(dateToday));
		    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime()); 
			p.setInt(1, bookId);
			p.setInt(2, rent);
			p.setDate(3, sqlDate);
			p.execute();
	    } catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void updateUserBalance(int userId, int rent){
    	Connection con = JDBCConnection.getConnection();
    	int remainingAmount = getRemainingAmount(userId);
    	remainingAmount = remainingAmount - rent;
    	String updateQuery = "update user set balance = ? where id = ?";
    	PreparedStatement preparedStmt ;
    	try {
    		preparedStmt = con.prepareStatement(updateQuery);
    		preparedStmt.setInt(1, remainingAmount);
    	    preparedStmt.setInt(2, userId);
    	    preparedStmt.execute();
    	}catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong");
		}
    }
    
	@GET
	@Path("/revenue/book/{bookId}")
	@Produces(MediaType.APPLICATION_JSON)
	public BookRevenue getRevenueForBook(@PathParam("bookId") Integer bookId){
		String query = "select sum(amountPaid) from book_revenue where id = "+bookId;
		Statement stmt = JDBCConnection.getStatement();
		BookRevenue br = null;
		try{
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				br = new BookRevenue();
				br.setBookId(bookId);
				br.setRevenue(rs.getInt(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong");
		}
		return br;
	}
	
	private int getRemainingAmount(int userId){
		Statement stmt = JDBCConnection.getStatement();
		String query = "select balance from user where id = "+userId;
		IntResponse ir = null;
		try {
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong");
		}
		return -1;
	}
	
	@GET
	@Path("/revenue/allBooks")
	@Produces(MediaType.APPLICATION_JSON)
	public IntResponse getTotalRevenue(){
		
		Statement stmt = JDBCConnection.getStatement();
		String query = "select sum(amountPaid) from book_revenue";
		IntResponse ir = null;
		try {
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				System.out.println(rs.getInt(1));
				ir = new IntResponse();
				System.out.println("SETTING RESPONNSE");
				ir.setRes(rs.getInt(1));
				System.out.println("RETURNING RESPONSE");
				return ir;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong");
			return ir;
		}
		return ir;
	}
	
	@POST
	@Path("/subscribe/user/{userId}/{amount}")
	/*
	 * Insert into user db the amount subscribed
	 */
	public Response subscribeUserWithAmount(@PathParam("userId") Integer userId,@PathParam("amount") Integer amount){
		System.out.println("inside method");
		Connection con = JDBCConnection.getConnection();
		String query = "insert into user (id, balance) values (?, ?)";
		
		PreparedStatement preparedStmt;
		try {
			preparedStmt = con.prepareStatement(query);
			preparedStmt.setInt(1, userId);
		    preparedStmt.setInt(2, amount);
		    preparedStmt.execute();
		    System.out.println("INSERTING DATA...");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong");
			return Response.status(300).build();
		}
		return Response.status(200).build();
	}
	
	@POST
	@Path("/pay/amount/{userId}/{amount}")
	public Response updateSubscription(@PathParam("userId") Integer userId,@PathParam("amount") Integer amount){
		Statement stmt = JDBCConnection.getStatement();
		Connection con = JDBCConnection.getConnection();
		String selectQuery = "select balance from user where id = "+userId;
		IntResponse ir = null;
		try {
			ResultSet rs = stmt.executeQuery(selectQuery);
			while(rs.next()){
				ir = new IntResponse();
				System.out.println("SETTING RESPONNSE");
				ir.setRes(rs.getInt(1));
				int newBalance = rs.getInt(1) + amount;
				
				String updateQuery = "update user set balance = ? where id = ?";
			      PreparedStatement preparedStmt = con.prepareStatement(updateQuery);
			      preparedStmt.setInt(1, newBalance);
			      preparedStmt.setInt(2, userId);
			      preparedStmt.execute();
			      
				System.out.println("RETURNING RESPONSE");
				
			}
		} catch (SQLException e) {
			System.out.println("Something went wrong");
			return Response.status(300).build();
		}
		return Response.status(200).build();
	}
	
	@GET
	@Path("/subscription/{userId}")
	public String getSubscriptionStatus(@PathParam("userId") int userId){
		String str = "NOT_SUBSCRIBED";
		/*
		 * Query for subscription status here
		 */
		Statement stmt = JDBCConnection.getStatement();
		String query = "select balance from user where id = "+userId;
		try {
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				System.out.println(rs.getInt(1));
				int response = rs.getInt(1);
				if(response >= 1){
					str = "SUBSCRIBED";
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong");
		}
		return str;
	}
	
}
