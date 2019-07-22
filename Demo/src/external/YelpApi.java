package external;

import java.io.BufferedReader; 
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException; 
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList; 
import java.util.HashSet; 
import java.util.List; 
import java.util.Set;

import org.json.JSONArray; 
import org.json.JSONException; 
import org.json.JSONObject;

public class YelpApi {
	
	private static final String HOST = "https://api.yelp.com";
	private static final String ENDPOINT = "/v3/businesses/search"; 
	private static final String DEFAULT_TERM = "";
	private static final int SEARCH_LIMIT = 20;
	
	private static final String TOKEN_TYPE = "Bearer"; 
	private static final String API_KEY = "goFjBjcDEtRfhACrb-1S86U-i09Df3QJWvyG_lola2rsVDnLpiZOaeGUiRfbC7hB-Hh4S17q2C6mOQLJuILKiyeAMJGsMxOQrMlPmES0FGnokdNbK2-MNe7XTSA2XXYx";
	
	public JSONArray search(double lat, double lon, String term) { 
		if (term == null || term.isEmpty()) {
			term = DEFAULT_TERM;
		}
		
		try {
			term = URLEncoder.encode(term, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	
		String query = String.format("term=%s&latitude=%s&longitude=%s&limit=%s", term, lat, lon, SEARCH_LIMIT);
		String url = HOST + ENDPOINT + "?" + query;
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", TOKEN_TYPE + " " + API_KEY);
			int responseCode = connection.getResponseCode();
			System.out.println("send request url: " + url);
			System.out.println("response code " + responseCode);
			
			if (responseCode != 200) {
				return new JSONArray();
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			String inputLine = "";
			StringBuilder response = new StringBuilder();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			
			in.close();
			
			JSONObject object = new JSONObject(response.toString());
			if (!object.isNull("businesses")) {
				return object.getJSONArray("businesses");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new JSONArray();
	}
	
	private void queryApi(double lat, double lon) {
		JSONArray items = search(lat, lon, null);
		try {
			for (int i = 0; i < items.length(); i++) {
				JSONObject item = items.getJSONObject(i);
				System.out.println(item.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	/**
	* Main entry for sample Yelp API requests. */
	public static void main(String[] args) { 
		YelpApi tmApi = new YelpApi(); 
		tmApi.queryApi(37.38, -122.08);
	}
}
