package db;

import java.sql.Connection;
import java.sql.DriverManager; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashSet; 
import java.util.List;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder; 
import external.YelpApi;

public class DBConnection {
	
	private Connection connection;
	
	public DBConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance(); 
			connection = DriverManager.getConnection(DBUtil.URL);
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}
	
	public void close() {
		if (connection != null) { 
			try {
				connection.close(); 
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (connection == null) {
			System.err.println("DB connection failed"); 
			return;
		}
		try {
			String sql = "INSERT IGNORE INTO history(user_id, item_id) VALUES(?, ?)";
			PreparedStatement ps = connection.prepareStatement(sql);
			
			ps.setString(1, userId);
			for (String itemId : itemIds) {
				ps.setString(2, itemId); 
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (connection == null) {
			System.err.println("DB connection failed"); 
			return;
		}
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			
			ps.setString(1, userId);
			for (String itemId : itemIds) {
				ps.setString(2, itemId); 
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Set<String> getFavoriteItemIds(String userId) { 
		return null;
	}
	
	public Set<Item> getFavoriteItems(String userId) { 
		return null;
	}
	
	public Set<String> getCategories(String itemId) { 
		return null;
	}
	
	public List<Item> searchItems(double lat, double lon, String term) { 
		YelpApi api = new YelpApi();
		List<Item> items = api.search(lat, lon, term); 
		
		for (Item item : items) {
			saveItem(item); 
		}
		
		return items;
	}
	
	public void saveItem(Item item) {
		if (connection == null) {
			System.err.println("DB connection failed"); 
			return;
		}

		try {
			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)"; 
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			ps.setString(2, item.getName());
			ps.setDouble(3, item.getRating());
			ps.setString(4, item.getAddress());
			ps.setString(5, item.getUrl());
			ps.setString(6, item.getImageUrl());
			ps.setDouble(7, item.getDistance());
			ps.execute();
			
			sql = "INSERT IGNORE INTO categories VALUES(?, ?)"; 
			ps = connection.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			
			for (String category : item.getCategories()) {
				ps.setString(2, category); 
				ps.execute();
			}
			
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}
}
