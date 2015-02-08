package com.owlab.activiti.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.owlab.util.WeakRestClient;

public class Tasks {
	private String activitiServiceUri;

	public Tasks(String activitiServiceUri) {
		this.activitiServiceUri = activitiServiceUri;
	}

	public JsonNode getFinishedTasks() throws JsonProcessingException, IOException, URISyntaxException {
		JsonNode rootNode = null;
		// String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/history/historic-task-instances";
		WeakRestClient.RestResponse response = WeakRestClient
					.get(this.activitiServiceUri + endPointExt)
					.basicAuth("kermit", "kermit")
					.queryString("taskAssignee", "kermit")
					.queryString("taskOwner", "kermit")
					.queryString("finished", "true")
					.execute();

		if (response.statusCode == 200) {
			rootNode = response.asJsonNode();
			
		}

		return rootNode;
	}
	public JsonNode getCandidateOrAssignedTasks() throws JsonProcessingException, IOException, URISyntaxException {
		JsonNode rootNode = null;
		// String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/runtime/tasks";
		WeakRestClient.RestResponse response = WeakRestClient
					.get(this.activitiServiceUri + endPointExt)
					.basicAuth("kermit", "kermit")
					.queryString("candidateOrAssigned", "kermit")
					.queryString("includeProcessVariables", "true")
					.execute();

		if (response.statusCode == 200) {
			rootNode = response.asJsonNode();
			
		}

		return rootNode;
	}
	
	public int getTotalTasksSize() throws JsonProcessingException, IOException, URISyntaxException {
		int totalSize = -1;
		// String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/runtime/tasks";
		WeakRestClient.RestResponse response = WeakRestClient
					.get(this.activitiServiceUri + endPointExt)
					.basicAuth("kermit", "kermit")
					.queryString("candidateOrAssigned", "kermit")
					.execute();
		
		if (response.statusCode == 200) {
			JsonNode rootNode = response.asJsonNode();
			totalSize = rootNode.get("total").asInt();
		}

		return totalSize;
	}

	public JsonNode getTaskUrlsOfCandidateOrAssigned(int totalTasksSize)
			throws ClientProtocolException, URISyntaxException, IOException {
		JsonNode returnJson = null;
		String endPointExt = "/runtime/tasks";

		if (totalTasksSize <= 0)
			return returnJson;

		WeakRestClient.RestResponse response = WeakRestClient
				.get(this.activitiServiceUri + endPointExt)
				.basicAuth("kermit", "kermit")
				.queryString("candidateOrAssigned", "kermit")
				.queryString("size", "" + totalTasksSize)
				// .queryString("includeProcessVariables", "true")
				// .queryString("finished", "true")
				// .queryString("owner", "kermit")
				.execute();

		ObjectMapper mapper = new ObjectMapper();
		ArrayNode jsonArray = mapper.createArrayNode();

		if (response.statusCode == 200) {
			JsonNode rootNode = response.asJsonNode();
			JsonNode dataNode = rootNode.get("data");
			if (dataNode.isArray()) {
				int tasksSize = dataNode.size();
				JsonNode urlNode = null;
				for (int i = 0; i < tasksSize; i++) {
					urlNode = dataNode.get(i).get("url");
					// System.out.println(urlNode.isValueNode());
					jsonArray.add(mapper.createObjectNode().put("url",
							urlNode.asText()));
				}
			}
			returnJson = jsonArray;
		}

		return returnJson;

	}

	public JsonNode getIdentityLinksOfTasks(JsonNode taskUrls)
			throws ClientProtocolException, URISyntaxException, IOException {
		if (taskUrls == null || !taskUrls.isArray()) {
			return null;
		}

		ObjectMapper mapper = new ObjectMapper();
		ArrayNode identityLinks = mapper.createArrayNode();

		ArrayNode identityLinksOfATask = null;

		for (JsonNode taskUrl : (ArrayNode) taskUrls) {
			identityLinksOfATask = (ArrayNode) getIdentityLinksOfATask(taskUrl);

			for (JsonNode identityLink : identityLinksOfATask) {
				identityLinks.add(identityLink);
			}
		}

		return identityLinks;
	}

	public JsonNode getIdentityLinksOfATask(JsonNode taskUrl)
			throws ClientProtocolException, URISyntaxException, IOException {
		if (taskUrl == null || taskUrl.isArray())
			return null;

		WeakRestClient.RestResponse response = WeakRestClient
				.get(taskUrl.get("url").asText() + "/identitylinks")
				.basicAuth("kermit", "kermit").execute();

		if (response.statusCode == 200) {
			return response.asJsonNode();
		} else {
			return null;
		}
	}

	public JsonNode getUserInfo(String userId) throws ClientProtocolException,
			URISyntaxException, IOException {
		if (userId == null)
			return null;
		String endPointExt = "/identity/users";
		WeakRestClient.RestResponse response = WeakRestClient
				.get(this.activitiServiceUri + endPointExt + "/" + userId)
				.basicAuth("kermit", "kermit").execute();

		if (response.statusCode == 200) {
			return response.asJsonNode();
		} else {
			return null;
		}
	}

	public JsonNode getGroupInfo(String groupId)
			throws ClientProtocolException, URISyntaxException, IOException {
		if (groupId == null)
			return null;
		String endPointExt = "/identity/groups";
		WeakRestClient.RestResponse response = WeakRestClient
				.get(this.activitiServiceUri + endPointExt + "/" + groupId)
				.basicAuth("kermit", "kermit").execute();

		if (response.statusCode == 200) {
			return response.asJsonNode();
		} else {
			return null;
		}
	}

	public JsonNode getCandidateUsersOrGroups(JsonNode identityLinks)
			throws ClientProtocolException, URISyntaxException, IOException {
		if (identityLinks == null || !identityLinks.isArray())
			return null;

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();

		ArrayNode usersNode = rootNode.putArray("users");
		ArrayNode groupsNode = rootNode.putArray("groups");

		String userId = null;
		String groupId = null;
		JsonNode userNode = null;
		JsonNode groupNode = null;
		Set<String> userIdSet = new HashSet<String>();
		Set<String> groupIdSet = new HashSet<String>();

		for (JsonNode identityLink : (ArrayNode) identityLinks) {
			if (identityLink.get("type").asText().equals("candidate")) { 
				if (!identityLink.path("user").isNull()) {
					userId = identityLink.get("user").asText();
					userIdSet.add(userId);

				}
				if (!identityLink.get("group").isNull()) {

					groupId = identityLink.get("group").asText();
					groupIdSet.add(groupId);

				}
			}
		}

		for (String id : userIdSet) {
			userNode = getUserInfo(id);
			if (userNode != null)
				usersNode.add(userNode);
		}
		for (String id : groupIdSet) {
			groupNode = getGroupInfo(id);
			if (groupNode != null) {
				groupsNode.add(groupNode);
			}
		}
		return rootNode;
	}
}
