package com.owlab.activiti.rest;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.owlab.util.JsonNodeUtil;

public class TasksTest {
	private static Tasks tasks;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tasks = new Tasks("http://localhost:8080/activiti-rest/service");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetTotalTaskSize() throws JsonProcessingException, IOException, URISyntaxException {
		//fail("Not yet implemented");
		
		int totalTasksSize = tasks.getTotalTasksSize();
		Assert.assertTrue(totalTasksSize >= 0);
	}
	
	@Test
	public void testGetCandidateUsersOrGroups() throws JsonProcessingException, IOException, URISyntaxException {
		//fail("Not yet implemented");
		JsonNode rootNode = tasks.getTaskUrlsOfCandidateOrAssigned(tasks.getTotalTasksSize());
		//Assert.assertTrue(totalTasksSize >= 0);
		JsonNodeUtil.beautifulPrint(rootNode);
		
	}
	
	@Test
	public void testGetCandidateUsersOrGroupsOfATask() throws ClientProtocolException, JsonProcessingException, URISyntaxException, IOException {
		JsonNode taskUrls = tasks.getTaskUrlsOfCandidateOrAssigned(tasks.getTotalTasksSize());
		//JsonNode anIdentityLink = tasks.getIdentityLinksOfATask(taskUrls.get(0));
		//JsonNodeUtil.beautifulPrint(taskUrls);
		JsonNode identityLinks = tasks.getIdentityLinksOfTasks(taskUrls);
		//JsonNodeUtil.beautifulPrint(identityLinks);
		JsonNode usersOrGroups = tasks.getCandidateUsersOrGroups(identityLinks);
		JsonNodeUtil.beautifulPrint(usersOrGroups);
	}

	@Test
	public void testGetCandidateOrAssignedTasks() throws JsonProcessingException, IOException, URISyntaxException {
		JsonNode rootNode = tasks.getCandidateOrAssignedTasks();
		
		JsonNodeUtil.beautifulPrint(rootNode);
	}
	
	@Test
	public void testGetFinishedTasks() throws JsonProcessingException, IOException, URISyntaxException {
		JsonNode rootNode = tasks.getFinishedTasks();
		
		JsonNodeUtil.beautifulPrint(rootNode);
	}
}
