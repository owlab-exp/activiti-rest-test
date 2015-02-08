package com.owlab.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.owlab.util.WeakRestClient;

public class WeakRestClientTest {

	@Test
	public void testGetCandidateUsersOrGroups() throws JsonProcessingException,
			IOException {
		String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/repository/process-definitions";
		WeakRestClient.RestResponse response = null;
		try {
			response = WeakRestClient
					.get(serviceBase + endPointExt + "/"
							+ "loanApproval:4:30873" + "/identitylinks")
					.basicAuth("kermit", "kermit")
					// .queryString(queryParameters)
					// .queryString("start", "29")
					// .queryString("taskAssignee", "kermit")
					// .queryString("taskOwner", "kermit")
					.execute();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(response.statusCode, 200);
		// System.out.println(restResponse.responseBody);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		Object o = mapper.treeToValue(response.asJsonNode(), Object.class);
		System.out.println(mapper.writeValueAsString(o));
	}

	@Test
	public void testGetCandidateUsersOrGroupsOfATask()
			throws JsonProcessingException, IOException {
		String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/runtime/tasks";
		WeakRestClient.RestResponse response = null;
		try {
			response = WeakRestClient
					.get(serviceBase + endPointExt + "/" + "31163"
							+ "/identitylinks").basicAuth("kermit", "kermit")
					// .queryString(queryParameters)
					// .queryString("start", "29")
					// .queryString("taskAssignee", "kermit")
					// .queryString("taskOwner", "kermit")
					.execute();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(response.statusCode, 200);
		// System.out.println(restResponse.responseBody);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		Object o = mapper.treeToValue(response.asJsonNode(), Object.class);
		System.out.println(mapper.writeValueAsString(o));
	}

	@Test
	public void testGetHistoryTaskInstances() throws JsonProcessingException,
			IOException {
		String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/history/historic-task-instances";
		WeakRestClient.RestResponse restResponse = null;
		try {
			restResponse = WeakRestClient
					.get(serviceBase + endPointExt)
					.basicAuth("kermit", "kermit")
					// .queryString(queryParameters)
					.queryString("finished", "true")
					.queryString("taskAssignee", "kermit")
					.queryString("taskOwner", "kermit").execute();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(restResponse.statusCode, 200);
		System.out.println(restResponse.responseBody);
	}

	@Test
	public void testGetCandidateOrAssignedTasks()
			throws JsonProcessingException, IOException {
		String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/runtime/tasks";
		WeakRestClient.RestResponse response = null;
		try {
			response = WeakRestClient.get(serviceBase + endPointExt)
					.basicAuth("kermit", "kermit")
					.queryString("candidateOrAssigned", "kermit")
					.queryString("includeProcessVariables", "true")
					// .queryString("finished", "true")
					// .queryString("owner", "kermit")
					.execute();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(response.statusCode, 200);
		// System.out.println(restResponse.responseBody);
		// System.out.println(restResponse.asJsonObject().toString(3));
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		Object o = mapper.treeToValue(response.asJsonNode(), Object.class);
		System.out.println(mapper.writeValueAsString(o));



		if (response.statusCode == 200) {
			Set<String> taskUrls = new HashSet<String>();
			Set<String> assignees = new HashSet<String>();
			Set<String> processInstanceIds = new HashSet<String>();
			Set<String> categories = new HashSet<String>();
			Map<String, String> customers = new HashMap<String, String>();
			Map<String, String> products = new HashMap<String, String>();
			
			JsonNode rootNode = response.asJsonNode();
			int taskListSize = rootNode.get("size").asInt();
			JsonNode dataNode = rootNode.get("data");

			JsonNode valueNode = null;
			for (int i = 0; i < taskListSize; i++) {
				JsonNode taskNode = dataNode.get(i);
				
				valueNode = taskNode.get("url");
				if (!valueNode.isNull())
					taskUrls.add(valueNode.asText());
				
				valueNode = taskNode.get("assignee");
				if (!valueNode.isNull())
					assignees.add(valueNode.asText());

				valueNode = taskNode.get("processInstanceId");
				if (!valueNode.isNull())
					processInstanceIds.add(valueNode.asText());

				valueNode = taskNode.get("category");
				if (!valueNode.isNull())
					categories.add(valueNode.asText());

				valueNode = taskNode.get("variables");
				if (valueNode.isArray()) {
					int variablesSize = valueNode.size();
					JsonNode variableNameNode = null;
					JsonNode variableValueNode = null;
					String variableName = null;
					String variableValue = null;
					String customerId = null;
					String customerName = null;
					String productCode = null;
					String productName = null;
					for (int j = 0; j < variablesSize; j++) {
						variableNameNode = valueNode.get(j).get("name");
						variableValueNode = valueNode.get(j).get("value");

						if (variableNameNode.isNull()
								|| variableNameNode.isMissingNode()
								|| variableValueNode.isNull()
								|| variableValueNode.isMissingNode())
							continue;

						variableName = variableNameNode.asText();
						variableValue = variableValueNode.asText();

						if (variableName.equals("") && variableValue.equals(""))
							continue;

						if (variableName.equals("aprvlAplctnCustId"))
							customerId = variableValue;
						else if (variableName.equals("aprvlAplctnCustNm"))
							customerName = variableValue;
						else if (variableName.equals("pdCd"))
							productCode = variableValue;
						else if (variableName.equals("pdNm"))
							productName = variableValue;
					}

					customers.put(customerId, customerName);
					products.put(productCode, productName);
				}
			}

			// to get values from above code
			for (String taskUrl : taskUrls) {
				System.out.println("taskUrl:" + taskUrl);
			}
			
			for (String assignee : assignees) {
				System.out.println("assignee:" + assignee);
			}

			for (String processInstanceId : processInstanceIds) {
				System.out.println("processInstanceIds:" + processInstanceId);
			}

			for (String category : categories) {
				System.out.println("task category:" + category);
			}

			System.out.println(customers);
			System.out.println(products);

		} else {// Rest call does not return 200

		}

	}
	
	@Test
	public void getTotalTasksSize() throws JsonProcessingException, IOException {
		String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/runtime/tasks";
		WeakRestClient.RestResponse response = null;
		try {
			response = WeakRestClient.get(serviceBase + endPointExt)
					.basicAuth("kermit", "kermit")
					.queryString("candidateOrAssigned", "kermit")
					.queryString("includeProcessVariables", "true")
					// .queryString("finished", "true")
					// .queryString("owner", "kermit")
					.execute();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(response.statusCode, 200);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		Object o = mapper.treeToValue(response.asJsonNode(), Object.class);
		System.out.println(mapper.writeValueAsString(o));



		if (response.statusCode == 200) {
		}
		
	}

	@Test
	public void testGetATask() {
		String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/runtime/tasks";
		WeakRestClient.RestResponse restResponse = null;
		try {
			restResponse = WeakRestClient
					.get(serviceBase + endPointExt + "/31275")
					.basicAuth("kermit", "kermit")
					// .queryString("taskId", "25018")
					// .queryString("taskOwner", "kermit")
					.execute();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(restResponse.statusCode, 200);
		System.out.println(restResponse.responseBody);
		// System.out.println(restResponse.asJsonObject().toString(3));
	}

	@Test
	public void testGetVariablesOfAnExecution() throws JsonProcessingException,
			IOException {
		String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/runtime/executions";
		WeakRestClient.RestResponse restResponse = null;
		try {
			restResponse = WeakRestClient
					.get(serviceBase + endPointExt + "/31221/variables")
					.basicAuth("kermit", "kermit")
					// .queryString("taskId", "25018")
					// .queryString("taskOwner", "kermit")
					.execute();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(restResponse.statusCode, 200);
		System.out.println(restResponse.responseBody);
		// System.out.println(restResponse.asJsonArray().toString(3));
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		Object o = mapper.treeToValue(restResponse.asJsonNode(), Object.class);
		System.out.println(mapper.writeValueAsString(o));
	}

	@Test
	public void testGetHistoricProcessInstances() {
		String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/history/historic-process-instances";
		WeakRestClient.RestResponse restResponse = null;
		try {
			restResponse = WeakRestClient.get(serviceBase + endPointExt)
					// ;// + "/31221/variables")
					.basicAuth("kermit", "kermit")
					.queryString("includeProcessVariables", "true") // only
																	// meaningful
																	// for list
																	// not an
																	// item
					// .queryString("taskId", "25018")
					// .queryString("taskOwner", "kermit")
					.execute();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(restResponse.statusCode, 200);
		System.out.println(restResponse.responseBody);
		// System.out.println(restResponse.asJsonObject().toString(3));
	}

	@Test
	public void testGetHistoricTaskInstances() {
		String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/history/historic-task-instances";
		WeakRestClient.RestResponse restResponse = null;
		try {
			restResponse = WeakRestClient
					.get(serviceBase + endPointExt)
					// ;// + "/31221/variables")
					.basicAuth("kermit", "kermit")
					.queryString("processInstanceId", "31283")
					.queryString("includeProcessVariables", "true")
					// .queryString("includeTaskLocalVariables", "true")

					.execute();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(restResponse.statusCode, 200);
		System.out.println(restResponse.responseBody);
		// System.out.println(restResponse.asJsonObject().toString(3));
	}

	@Test
	public void testTaskActions() {
		String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/runtime/tasks";
		WeakRestClient.RestResponse restResponse = null;
		try {
			// JSONObject jsonRequest = new JSONObject();
			// jsonRequest.put("action", "claim");
			// jsonRequest.put("assignee", "kermit");

			ObjectMapper mapper = new ObjectMapper();
			ObjectNode node = mapper.createObjectNode();
			node.put("action", "claim");
			node.put("assignee", "kermit");

			restResponse = WeakRestClient
					.post(serviceBase + endPointExt + "/31337")
					.header("content-type", "application/json")
					.basicAuth("kermit", "kermit")
					// .body(jsonRequest.toString())
					.body(node.toString()).execute();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(restResponse.responseBody);
		Assert.assertEquals(restResponse.statusCode, 200);
		// System.out.println(restResponse.asJsonArray().toString(3));
	}

	@Test
	public void testUpdateATask() throws JsonParseException,
			JsonMappingException, JsonProcessingException, IOException {
		String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/runtime/tasks";
		WeakRestClient.RestResponse restResponse = null;
		try {
			// JSONObject jsonRequest = new JSONObject();
			// jsonRequest.put("owner", "kermit");
			// jsonRequest.put("assignee", "kermit");

			ObjectNode node = new ObjectMapper().createObjectNode();
			node.put("owner", "kermit");
			node.put("assignee", "kermit");

			restResponse = WeakRestClient
					.put(serviceBase + endPointExt + "/31337")
					.header("content-type", "application/json")
					.basicAuth("kermit", "kermit")
					// .body(jsonRequest.toString())
					.body(node.toString()).execute();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(restResponse.responseBody);
		Assert.assertEquals(restResponse.statusCode, 200);
		// System.out.println(restResponse.asJsonObject().toString(3));
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		Object o = mapper.treeToValue(restResponse.asJsonNode(), Object.class);
		System.out.println(mapper.writeValueAsString(o));

	}
}
