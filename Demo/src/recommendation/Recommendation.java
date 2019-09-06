package recommendation;

import java.util.ArrayList; 
import java.util.Collections; 
import java.util.HashMap; 
import java.util.HashSet; 
import java.util.List;
import java.util.Map; 
import java.util.Map.Entry; 
import java.util.Set;
import db.DBConnection; 
import entity.Item;

// Recommend based on geo-location and similar categories.
public class Recommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<>();
		
		// Step 1, get all favorite item ids
		DBConnection connection = new DBConnection(); 
		Set<String> favoriteItemIds = connection.getFavoriteItemIds(userId);
		
		// Step 2, get all categories, then sort by count
		Map<String, Integer> allCategories = new HashMap<>();
		for (String itemId : favoriteItemIds) {
			Set<String> categories = connection.getCategories(itemId); 
			for (String category : categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			} 
		}
		List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
		// Sort decreasing order by count
		Collections.sort(categoryList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> e2.getValue() - e1.getValue());
		
		// Step 3, search based on category, filter out favored items
		Set<String> searchedItemIds = new HashSet<>();
		for (Entry<String, Integer> category : categoryList) {
			List<Item> items = connection.searchItems(lat, lon, category.getKey());
			for (Item item : items) {
				if (!favoriteItemIds.contains(item.getItemId()) && 
					// filter our the repetitive items
					!searchedItemIds.contains(item.getItemId())) {
					recommendedItems.add(item); 
					searchedItemIds.add(item.getItemId());
				}
			}
		}
		connection.close();
		return recommendedItems;	
	}
}
