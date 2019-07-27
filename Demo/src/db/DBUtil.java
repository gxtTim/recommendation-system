package db;

public class DBUtil {
	private static final String HOSTNAME = "localhost";
	private static final String PORT_NUM = "8889";// change it to your mysql port number
	public static final String DB_NAME = "yelp_project";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";
	public static final String URL = "jdbc:mysql://" + HOSTNAME + ":" + PORT_NUM + "/" + 
	                           DB_NAME + "?user=" + USERNAME + "&password=" + PASSWORD + "&autoreconnect=true&serverTimezone=UTC";
}
