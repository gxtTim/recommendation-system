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
			connection = DriverManager.getConnection(DBUtility.URL);
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
		if (connection == null) {
			System.err.println("DB connection failed"); return new HashSet<>();
		}
		Set<String> favoriteItemIds = new HashSet<>(); 
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?"; 
			PreparedStatement ps = connection.prepareStatement(sql); 
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) { 
				favoriteItemIds.add(rs.getString("item_id"));
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return favoriteItemIds;
	}
	
	public Set<Item> getFavoriteItems(String userId) { 
		if (connection == null) {
			System.err.println("DB connection failed"); 
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> favoriteItemIds = getFavoriteItemIds(userId); 
		try {
			String sql = "SELECT * FROM items WHERE item_id = ?"; 
			PreparedStatement ps = connection.prepareStatement(sql);
			for (String itemId : favoriteItemIds) { 
				ps.setString(1, itemId);
				ResultSet rs = ps.executeQuery();
				ItemBuilder builder = new ItemBuilder(); 
				while (rs.next()) {
					builder.setItemId(rs.getString("item_id")); 
					builder.setName(rs.getString("name")); 
					builder.setUrl(rs.getString("url")); 
					builder.setImageUrl(rs.getString("image_url")); 
					builder.setAddress(rs.getString("address")); 
					builder.setRating(rs.getDouble("rating")); 
					builder.setDistance(rs.getDouble("distance")); 
					builder.setCategories(getCategories(itemId));
					
					favoriteItems.add(builder.build());
				}
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return favoriteItems;
	}
	
	public Set<String> getCategories(String itemId) { 
		if (connection == null) {
			System.err.println("DB connection failed"); 
			return null;
		}
		Set<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category FROM categories WHERE item_id = ?"; 
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, itemId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) { 
				categories.add(rs.getString("category"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return categories;
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
			ps.setString(5, item.getImageUrl());
			ps.setString(6, item.getUrl());
			
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
