package com.jackroot.app.ws.restassuredtest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;

import io.restassured.RestAssured;
import io.restassured.response.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UsersWebServiceEndpointTest {
	
	private final String contextPath = "/mobile-app-ws";
	private final String email = "jcksnroot@gmail.com";
	private final String json = "application/json";
	
	private static String authorizationHeader;
	private static String userId;
	private static List<Map<String, String>> addresses;

	@BeforeEach
	void setUp() throws Exception {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 8080;
	}

	/**
	 * testUserLogin()
	 */
	@Test
	void a() {
		Response response = given().contentType(json).accept(json).body(createLoginDetails())
				.when().post(contextPath + "/users/login")
				.then().statusCode(200).extract().response();
		
		authorizationHeader = response.header("Authorization");
		userId = response.header("UserID");
		
		assertNotNull(authorizationHeader);
		assertNotNull(userId);
	}
	
	/**
	 * testGetUserDetails()
	 */
	@Test
	void b() {
		Response response = given().pathParam("id", userId)
				.header("Authorization", authorizationHeader)
				.contentType(json)
				.accept(json).when().get(contextPath + "/users/{id}")
				.then().statusCode(200).extract().response();
		
		String userPublicId = response.jsonPath().getString("userId");
		String userEmail = response.jsonPath().getString("email");
		String firstName = response.jsonPath().getString("firstName");
		String lastName = response.jsonPath().getString("lastName");
		addresses = response.jsonPath().getList("addresses");
		String addressId = addresses.get(0).get("addressId");
		
		assertNotNull(userPublicId);
		assertNotNull(userEmail);
		assertNotNull(firstName);
		assertNotNull(lastName);
		assertEquals(email, userEmail);
		
		assertTrue(addresses.size() == 2);
		assertTrue(addressId.length() == 30);
	}
	
	/**
	 * testUpdateUser() - update user details
	 */
	@Test
	void c() {
		Response response = given().pathParam("id", userId)
				.header("Authorization", authorizationHeader)
				.contentType(json)
				.accept(json).body(getUserDetails())
				.when().put(contextPath + "/users/{id}")
				.then().statusCode(200).extract().response();
		
		String firstName = response.jsonPath().getString("firstName");
		String lastName = response.jsonPath().getString("lastName");
		List<Map<String, String>> storedAddresses = response.jsonPath().getList("addresses");
		
		assertEquals(getUserDetails().get("firstName"), firstName);
		assertEquals(getUserDetails().get("lastName"), lastName);
		assertNotNull(storedAddresses); 
		assertTrue(addresses.size() == storedAddresses.size());
		assertEquals(addresses.get(0).get("streetName"), storedAddresses.get(0).get("streetName"));
	}
	
	/**
	 * testDeleteUser() - delete user details
	 */
	@Test
	void d() {
		Response response = given().pathParam("id", userId)
				.header("Authorization", authorizationHeader)
				.accept(json).when().delete(contextPath + "/users/{id}")
				.then().statusCode(200).extract().response();
		
		String operationResult = response.jsonPath().getString("operationResult");
		assertEquals("SUCCESS", operationResult);
	}
	
	private Map<String, Object> getUserDetails() {
		Map<String, Object> userDetails = new HashMap<>();
		userDetails.put("firstName", "Jack");
		userDetails.put("lastName", "Justjack");
		
		return userDetails;
	}

	private Map<String, String> createLoginDetails() {
		Map<String, String> loginDetails = new HashMap<String, String>();
		loginDetails.put("email", email);
		loginDetails.put("password", "123");
		
		return loginDetails;
	}
}