package com.owlab.activiti.rest;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.owlab.util.JsonNodeUtil;


public class TasksTest {
	private static Tasks tasks;
	private static ObjectMapper mapper; 
	
//	public static void main(String[] args) {
//		Tasks tasks = new Tasks("http://58.237.227.195:28081/activiti-rest/service");
//		ObjectMapper mapper = 
//	}
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tasks = new Tasks("http://58.237.227.195:28081/activiti-rest/service");
		mapper = new ObjectMapper();
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetToDoListOverJson() throws ClientProtocolException, UnsupportedEncodingException, JsonProcessingException, URISyntaxException, IOException {
		//fail("Not yet implemented"); // TODO
		JsonNode requestNode = mapper.createObjectNode();
		
		JsonNode response = tasks.getToDoListOverJson(requestNode, "kermit", "kermit");
		
		//Assert.assertEquals(response.get("statusCode").asInt(), 200);
		
		JsonNodeUtil.beautifulPrint(response);
	}

	@Test
	public void testMakeFilterForUnfinishedTasks() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testMakeFilterForFinishedTasks() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testPostCandidateOrAssignedTasks() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetCandidateOrAssignedTasksTotalSize() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetBasicListsForFilteringOfCandidateOrAssignedTasks() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testPostFinishedTasks() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetFinishedTasksTotalSize() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetBasicListsForFilteringOfFinishedTasks() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetProcessKeyNameCategories() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetIdentityLinksOfTasks() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetIdentityLinksOfATask() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetUserInfo() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetGroupInfo() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetCandidateUsersOrGroups() {
		fail("Not yet implemented"); // TODO
	}

}
