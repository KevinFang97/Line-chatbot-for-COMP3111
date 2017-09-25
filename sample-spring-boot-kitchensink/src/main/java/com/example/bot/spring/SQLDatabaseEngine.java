package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.net.URISyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

@Slf4j
public class SQLDatabaseEngine extends DatabaseEngine {
	@Override
	String search(String text) throws Exception {
		//String username = "czeqruiqorlnqi";
		//String password = "f36d2e08e3e2f7e4501b2b5de89cf2fe8b5f09d75f5cf442da707e88c97f3b92";
		//String dbUrl = "postgres://czeqruiqorlnqi:f36d2e08e3e2f7e4501b2b5de89cf2fe8b5f09d75f5cf442da707e88c97f3b92@ec2-23-21-85-76.compute-1.amazonaws.com:5432/d5ct4b32d7mv1v";
		String result = null;
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			connection = this.getConnection();
			stmt = connection.prepareStatement("SELECT * FROM keytable;");
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				String q = rs.getString(0);
				String a = rs.getString(1);
				int hit = rs.getInt(2);
				if (text.toLowerCase().contains(q.toLowerCase())) {
					result = a;
					hit += 1;
					connection.prepareStatement("UPDATE keytable SET hit = "+hit+" WHERE keyword = '"+q+"';").executeQuery();
				}
			}
			
		} catch (SQLException e) {
			log.info("IOException while opening database or execute query: {}", e.toString());
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException ex) {
				log.info("IOException while closing connections/statement/resultSets: {}", ex.toString());
			}
		}
		
		if (result != null)
			return result;
		throw new Exception("NOT FOUND");
	}
	
	
	private Connection getConnection() throws URISyntaxException, SQLException {
		Connection connection;
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() +  "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

		log.info("Username: {} Password: {}", username, password);
		log.info ("dbUrl: {}", dbUrl);
		
		connection = DriverManager.getConnection(dbUrl, username, password);

		return connection;
	}


}
