package com.owlab.activiti.rest;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.owlab.util.WeakRestClient;


public class ActivitiTaskList {
	private String activitiServiceUri;

	public ActivitiTaskList(String activitiServiceUri) {
		this.activitiServiceUri = activitiServiceUri;
	}

	public JsonNode getToDoListOverJson(JsonNode requestNode, String authId, String authPassword) throws ClientProtocolException, UnsupportedEncodingException, JsonProcessingException,
			URISyntaxException, IOException, AuthenticationException {
		final ObjectMapper mapper = new ObjectMapper();
		ObjectNode result = mapper.createObjectNode();

		JsonNode queryWaitingOrFinished = requestNode.path("queryWaitingOrFinished");
		// if (typeOfTasks.isMissingNode() || typeOfTasks.isNull()) {
		//
		// } else if (typeOfTasks.asText().equalsIgnoreCase("false")) {
		if (queryWaitingOrFinished.isMissingNode() || queryWaitingOrFinished.isNull() || queryWaitingOrFinished.asText().equals("waiting")) {
			// default request, return not completed tasks
			JsonNode filterContainer = makeFilterForUnfinishedTasks(requestNode, authId);
			// JsonNode hasFilter = filterContainer.path("hasFilter");
			JsonNode taskListPage = postCandidateOrAssignedTasks(filterContainer.get("filter"), authId, authPassword);

			// Because the post http call may not give total size, we have to
			// get total size with another REST call.
			int totalSize = getCandidateOrAssignedTasksTotalSize(authId, "kermit");

			// Get various data to be used for filtering of future requests
			JsonNode basicLists = getBasicListsForFilteringOfCandidateOrAssignedTasks(totalSize, authId, authPassword);
			/*
			 * lists: ArrayNode taskUrls = mapper.createArrayNode(); ArrayNode
			 * categories = mapper.createArrayNode(); ArrayNode
			 * processDefinitions = mapper.createArrayNode(); ArrayNode
			 * customers = mapper.createArrayNode(); ArrayNode products =
			 * mapper.createArrayNode();
			 */
			//JsonNodeUtil.beautifulPrint(basicLists);
			if (basicLists.path("statusCode").asInt() != 200) {
				// exit from here
			}

			JsonNode taskCategories = basicLists.get("categories");
			JsonNode customers = basicLists.get("customers");
			JsonNode products = basicLists.get("products");

			JsonNode taskUrls = basicLists.get("taskUrls");
			JsonNode identityLinks = getIdentityLinksOfTasks(taskUrls, authId, authPassword);

			// get list of candidate users and groups information
			JsonNode candidateUsersOrGroups = getCandidateUsersOrGroups(identityLinks, authId, authPassword);

			JsonNode processDefinitionUrls = basicLists.get("processDefinitions");
			JsonNode processKeyNameCategories = getProcessKeyNameCategories(processDefinitionUrls, authId, authPassword);

			if (processKeyNameCategories.path("statusCode").asInt() != 200) {
				// exit from here
			}

			JsonNode processKeyAndNames = processKeyNameCategories.get("processKeyAndNames");
			JsonNode processCategories = processKeyNameCategories.get("processCategories");

			result.put("statusCode", 200);
			// to resend previous query type
			result.put("queryWaitingOrFinished", queryWaitingOrFinished.asText());
			// to set possible filter data on UI
			result.set("taskCategories", taskCategories);
			result.set("candidateUsersOrGroups", candidateUsersOrGroups);
			result.set("processKeyAndNames", processKeyAndNames);
			result.set("processCategories", processCategories);
			result.set("customers", customers);
			result.set("products", products);
			// to resend the search criteria given
			result.set("filter", filterContainer.get("filter"));
			// to draw task list table
			result.set("taskTablePage", taskListPage);

		} else if (queryWaitingOrFinished.asText().equals("finished")) {
			// return finished tasks
			JsonNode filterContainer = makeFilterForFinishedTasks(requestNode, authId);

			JsonNode taskListPage = postFinishedTasks(filterContainer.get("filter"), authId, authPassword);

			int totalSize = getFinishedTasksTotalSize(authId, authPassword);

			JsonNode basicLists = getBasicListsForFilteringOfCandidateOrAssignedTasks(totalSize, authId, authPassword);
			/*
			 * lists: ArrayNode taskUrls = mapper.createArrayNode(); ArrayNode
			 * categories = mapper.createArrayNode(); ArrayNode
			 * processDefinitions = mapper.createArrayNode(); ArrayNode
			 * customers = mapper.createArrayNode(); ArrayNode products =
			 * mapper.createArrayNode();
			 */
			if (basicLists.path("statusCode").asInt() != 200) {
				// exit from here
			}

			JsonNode taskCategories = basicLists.get("categories");
			JsonNode customers = basicLists.get("customers");
			JsonNode products = basicLists.get("products");

			JsonNode processDefinitionUrls = basicLists.get("processDefinitions");
			JsonNode processKeyNameCategories = getProcessKeyNameCategories(processDefinitionUrls, authId, authPassword);

			if (processKeyNameCategories.path("statusCode").asInt() != 200) {
				// exit from here
			}

			JsonNode processKeyAndNames = processKeyNameCategories.get("processKeyAndNames");
			JsonNode processCategories = processKeyNameCategories.get("processCategories");

			result.put("statusCode", 200);
			// to resend previous query type
			result.put("queryWaitingOrFinished", queryWaitingOrFinished.asText());
			// to set possible filter data on UI
			result.set("taskCategories", taskCategories);

			// result.set("candidateUsersOrGroups", candidateUsersOrGroups); //
			// no need in finished tasks
			result.set("processKeyAndNames", processKeyAndNames);
			result.set("processCategories", processCategories);
			result.set("customers", customers);
			result.set("products", products);
			// to resend the search criteria given
			result.set("filter", filterContainer.get("filter"));
			// to draw task list table
			result.set("taskTablePage", taskListPage);

		}

		return result;
	}

	public JsonNode makeFilterForUnfinishedTasks(JsonNode requestNode, String authId) {
		ObjectNode result = null;

		// JsonNode category = requestNode.path("category");
		JsonNode candidateUser = requestNode.path("candidateUser");
		JsonNode candidateGroup = requestNode.path("candidateGroup");
		JsonNode processDefinitionKey = requestNode.path("processDefinitionKey");
		JsonNode dueAfter = requestNode.path("dueDateAfter");
		JsonNode dueBefore = requestNode.path("dueDateBefore");
		// JsonNode taskCompletedAfter = requestNode.path("taskCompletedAfter");
		// JsonNode taskCompletedBefore =
		// requestNode.path("taskCompletedBefore");

		JsonNode customerId = requestNode.path("customerId");
		JsonNode productCode = requestNode.path("productCode");

		JsonNode start = requestNode.path("start");
		JsonNode size = requestNode.path("size");

		boolean hasFilter = false;

		ObjectMapper mapper = new ObjectMapper();
		result = mapper.createObjectNode();

		ObjectNode filter = mapper.createObjectNode();

		// filter.put("taskAssignee", userId); // to get login user's finished
		// tasks
		// filter.put("finished", true); // to get finished tasks
		filter.put("includeProcessVariables", true); // to get process variables
		filter.put("candidateOrAssigned", authId); // to get process variables

		// if(!(finished.isMissingNode() || finished.isNull())) {
		// filter.put("finished", finished.asText());
		// hasFilter = true;
		// }
		//
		// if(!(category.isMissingNode() || category.isNull())) {
		// filter.put("category", category.asText());
		// hasFilter = true;
		// }

		if (!(processDefinitionKey.isMissingNode() || processDefinitionKey.isNull())) {
			filter.put("processDefinitionKey", processDefinitionKey.asText());
			hasFilter = true;
		}

		if (!(candidateUser.isMissingNode() || candidateUser.isNull())) {
			filter.put("candidateUser", candidateUser.asText());
			hasFilter = true;
		}

		if (!(candidateGroup.isMissingNode() || candidateGroup.isNull())) {
			filter.put("candidateGroup", candidateGroup.asText());
			hasFilter = true;
		}

		if (!(dueAfter.isMissingNode() || dueAfter.isNull())) {
			filter.put("dueAfter", dueAfter.asText());
			hasFilter = true;
		}

		if (!(dueBefore.isMissingNode() || dueBefore.isNull())) {
			filter.put("dueBefore", dueBefore.asText());
			hasFilter = true;
		}

		if (!(start.isMissingNode() || start.isNull())) {
			filter.put("start", start.asText());
			hasFilter = true;
		}

		if (!(size.isMissingNode() || size.isNull())) {
			filter.put("size", size.asText());
			hasFilter = true;
		}

		// if(!(taskCompletedAfter.isMissingNode() ||
		// taskCompletedAfter.isNull())) {
		// filter.put("taskCompletedAfter", taskCompletedAfter.asText());
		// hasFilter = true;
		// }
		//
		// if(!(taskCompletedBefore.isMissingNode() ||
		// taskCompletedBefore.isNull())) {
		// filter.put("taskCompletedBefore", taskCompletedBefore.asText());
		// hasFilter = true;
		// }

		ArrayNode processInstanceVariables = mapper.createArrayNode();

		if (!(customerId.isMissingNode() || customerId.isNull())) {
			ObjectNode processInstanceVariable = mapper.createObjectNode();
			processInstanceVariable.put("name", "custId");
			processInstanceVariable.put("value", customerId.asText());
			processInstanceVariable.put("operation", "equals");
			processInstanceVariable.put("type", "string");

			processInstanceVariables.add(processInstanceVariable);
		}

		if (!(productCode.isMissingNode() || productCode.isNull())) {
			ObjectNode processInstanceVariable = mapper.createObjectNode();
			processInstanceVariable.put("name", "pdCd");
			processInstanceVariable.put("value", productCode.asText());
			processInstanceVariable.put("operation", "equals");
			processInstanceVariable.put("type", "string");

			processInstanceVariables.add(processInstanceVariable);
		}

		if (processInstanceVariables.size() > 0)
			filter.set("processInstanceVariables", processInstanceVariables);

		result.put("hasFilter", hasFilter);
		result.set("filter", filter);

		return result;

	}

	public JsonNode makeFilterForFinishedTasks(JsonNode requestNode, String authId) {
		ObjectNode result = null;

		// JsonNode finished = requestNode.path("finished");
		// JsonNode category = requestNode.path("category");
		// JsonNode candidateUser = requestNode.path("candidateUser");
		// JsonNode candidateGroup = requestNode.path("candidateGroup");
		JsonNode processDefinitionKey = requestNode.path("processDefinitionKey");
		JsonNode dueDateAfter = requestNode.path("dueDateAfter");
		JsonNode dueDateBefore = requestNode.path("dueDateBefore");
		JsonNode taskCompletedAfter = requestNode.path("taskCompletedAfter");
		JsonNode taskCompletedBefore = requestNode.path("taskCompletedBefore");

		JsonNode customerId = requestNode.path("customerId");
		JsonNode productCode = requestNode.path("productCode");

		JsonNode start = requestNode.path("start");
		JsonNode size = requestNode.path("size");

		boolean hasFilter = false;

		ObjectMapper mapper = new ObjectMapper();
		result = mapper.createObjectNode();

		ObjectNode filter = mapper.createObjectNode();

		filter.put("taskAssignee", authId); // to get login user's finished
											// tasks
		filter.put("finished", true); // to get finished tasks
		filter.put("includeProcessVariables", true); // to get process variables

		// if(!(finished.isMissingNode() || finished.isNull())) {
		// filter.put("finished", finished.asText());
		// hasFilter = true;
		// }
		//
		// if(!(category.isMissingNode() || category.isNull())) {
		// filter.put("category", category.asText());
		// hasFilter = true;
		// }
		//
		// if(!(candidateUser.isMissingNode() || candidateUser.isNull())) {
		// filter.put("candidateUser", candidateUser.asText());
		// hasFilter = true;
		// }
		//
		// if(!(candidateGroup.isMissingNode() || candidateGroup.isNull())) {
		// filter.put("candidateGroup", candidateGroup.asText());
		// hasFilter = true;
		// }

		if (!(processDefinitionKey.isMissingNode() || processDefinitionKey.isNull())) {
			filter.put("processDefinitionKey", processDefinitionKey.asText());
			hasFilter = true;
		}

		if (!(dueDateAfter.isMissingNode() || dueDateAfter.isNull())) {
			filter.put("dueDateAfter", dueDateAfter.asText());
			hasFilter = true;
		}

		if (!(dueDateBefore.isMissingNode() || dueDateBefore.isNull())) {
			filter.put("dueDateBefore", dueDateBefore.asText());
			hasFilter = true;
		}

		if (!(taskCompletedAfter.isMissingNode() || taskCompletedAfter.isNull())) {
			filter.put("taskCompletedAfter", taskCompletedAfter.asText());
			hasFilter = true;
		}

		if (!(taskCompletedBefore.isMissingNode() || taskCompletedBefore.isNull())) {
			filter.put("taskCompletedBefore", taskCompletedBefore.asText());
			hasFilter = true;
		}

		if (!(start.isMissingNode() || start.isNull())) {
			filter.put("start", start.asText());
			hasFilter = true;
		}

		if (!(size.isMissingNode() || size.isNull())) {
			filter.put("size", size.asText());
			hasFilter = true;
		}

		ArrayNode processVariables = mapper.createArrayNode();

		if (!(customerId.isMissingNode() || customerId.isNull())) {
			ObjectNode processVariable = mapper.createObjectNode();
			processVariable.put("name", "custId");
			processVariable.put("value", customerId.asText());
			processVariable.put("operation", "equals");
			processVariable.put("type", "string");

			processVariables.add(processVariable);
		}

		if (!(productCode.isMissingNode() || productCode.isNull())) {
			ObjectNode processVariable = mapper.createObjectNode();
			processVariable.put("name", "pdCd");
			processVariable.put("value", productCode.asText());
			processVariable.put("operation", "equals");
			processVariable.put("type", "string");

			processVariables.add(processVariable);
		}

		if (processVariables.size() > 0)
			filter.set("processVariables", processVariables);
		// filter.set("processVariables", processVariables);

		result.put("hasFilter", hasFilter);
		result.set("filter", filter);

		return result;

	}

	public JsonNode postCandidateOrAssignedTasks(JsonNode filter, String authId, String authPassword) throws ClientProtocolException, UnsupportedEncodingException, JsonProcessingException,
			URISyntaxException, IOException, AuthenticationException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode result = mapper.createObjectNode();

		// JsonNodeUtil.beautifulPrint(filter);
		String endPointExt = "/query/tasks";
		WeakRestClient.RestResponse response = WeakRestClient.post(this.activitiServiceUri + endPointExt)
				.header("content-type", "application/json")
				.basicAuth(authId, authPassword)
				.bodyAsJsonNode(filter)
				// .body(filter.toString())
				.execute();

		result.put("statusCode", response.statusCode);
		if (response.statusCode == 200) {
			result.set("taskTable", response.asJsonNode());
		} else {
			result.put("message", response.responseBody);
		}

		return result;
	}

	public int getCandidateOrAssignedTasksTotalSize(String authId, String authPassword) throws JsonProcessingException, IOException, URISyntaxException, AuthenticationException {
		int totalSize = -1;
		// String serviceBase = "http://localhost:8080/activiti-rest/service";
		String endPointExt = "/runtime/tasks";
		WeakRestClient.RestResponse response = WeakRestClient.get(this.activitiServiceUri + endPointExt).basicAuth(authId, authPassword).queryString("candidateOrAssigned", authId).execute();

		if (response.statusCode == 200) {
			JsonNode rootNode = response.asJsonNode();
			totalSize = rootNode.get("total").asInt();
		}

		return totalSize;
	}

	public JsonNode getBasicListsForFilteringOfCandidateOrAssignedTasks(int totalTasksSize, String authId, String authPassword) throws ClientProtocolException, URISyntaxException, IOException, AuthenticationException {
		final ObjectMapper mapper = new ObjectMapper();
		ObjectNode result = mapper.createObjectNode();

		String endPointExt = "/runtime/tasks";

		if (totalTasksSize <= 0) {
			result.put("statusCode", -100);
			result.put("message", "Given total tasks' size <=0");
			return result;
		}

		WeakRestClient.RestResponse response = WeakRestClient.get(this.activitiServiceUri + endPointExt)
				.basicAuth(authId, authPassword)
				.queryString("candidateOrAssigned", authId)
				.queryString("includeProcessVariables", "true")
				.queryString("size", "" + totalTasksSize)
				.execute();

		// set status code to see if the processing has no error
		result.put("statusCode", response.statusCode);

		if (response.statusCode == 200) {
			// Now we have to extract various data

			// Get task urls
			JsonNode rootNode = response.asJsonNode();
			JsonNode dataNode = rootNode.get("data");

			ArrayNode taskUrls = mapper.createArrayNode();
			ArrayNode categories = mapper.createArrayNode();
			ArrayNode processDefinitions = mapper.createArrayNode();
			ArrayNode customers = mapper.createArrayNode();
			ArrayNode products = mapper.createArrayNode();

			// firstly put the lists to result
			result.set("taskUrls", taskUrls);
			result.set("categories", categories);
			result.set("processDefinitions", processDefinitions);
			result.set("customers", customers);
			result.set("products", products);

			Set<String> processDefinitionUrlSet = new HashSet<String>();
			Set<String> taskCategorySet = new HashSet<String>();
			Map<String, String> customerMap = new HashMap<String, String>();
			Map<String, String> productMap = new HashMap<String, String>();

			// fill the individual lists
			if (dataNode.isArray()) {
				JsonNode node = null;
				for (JsonNode task : (ArrayNode) dataNode) {

					node = task.path("url");
					if (!node.isMissingNode())
						taskUrls.add(node.asText());

					node = task.path("processDefinitionUrl");
					if (!node.isMissingNode())
						// processDefinitions.add(node.asText());
						processDefinitionUrlSet.add(node.asText());

					node = task.path("category");
					if (!node.isMissingNode() && !node.isNull())
						// categories.add(node.asText());
						taskCategorySet.add(node.asText());

					node = task.path("variables");
					if (node.isArray()) {
						JsonNode variableName = null;
						JsonNode variableValue = null;
						JsonNode customerId = null;
						JsonNode customerName = null;
						JsonNode productCd = null;
						JsonNode productName = null;

						for (JsonNode variable : (ArrayNode) node) {
							variableName = variable.path("name");
							if (!variableName.isMissingNode() && !variableName.isNull() && variableName.asText().equals("custId")) {
								variableValue = variable.path("value");
								if (!variableValue.isMissingNode() && !variableValue.isNull())
									customerId = variableValue;
							}

							if (!variableName.isMissingNode() && !variableName.isNull() && variableName.asText().equals("custNm")) {
								variableValue = variable.path("value");
								if (!variableValue.isMissingNode() && !variableValue.isNull())
									customerName = variableValue;
							}

							if (!variableName.isMissingNode() && !variableName.isNull() && variableName.asText().equals("pdCd")) {
								variableValue = variable.path("value");
								if (!variableValue.isMissingNode() && !variableValue.isNull())
									productCd = variableValue;
							}

							if (!variableName.isMissingNode() && !variableName.isNull() && variableName.asText().equals("pdNm")) {
								variableValue = variable.path("value");
								if (!variableValue.isMissingNode() && !variableValue.isNull())
									productName = variableValue;
							}
						}

						if (customerId != null && customerName != null) {
							// ObjectNode customer = mapper.createObjectNode();
							// customer.put("custId", customerId.asText());
							// customer.put("custNm", customerName.asText());
							// customers.add(customer);
							if(!customerId.asText().equals(""))
								customerMap.put(customerId.asText(), customerName.asText());
						}

						if (productCd != null && productName != null) {
							// ObjectNode product = mapper.createObjectNode();
							// product.put("pdCd", productCd.asText());
							// product.put("pdNm", productName.asText());
							// products.add(product);
							if(!productCd.asText().equals(""))
								productMap.put(productCd.asText(), productName.asText());
						}

					}
				}

				if (processDefinitionUrlSet.size() > 0) {
					for (String processDefinition : processDefinitionUrlSet) {
						processDefinitions.add(processDefinition);
					}
				}

				if (taskCategorySet.size() > 0) {
					for (String taskCategory : taskCategorySet) {
						categories.add(taskCategory);
					}
				}

				if (customerMap.size() > 0) {
					Set<String> keySet = customerMap.keySet();
					for (String key : keySet) {
						ObjectNode customer = mapper.createObjectNode();
						customer.put("custId", key);
						customer.put("custNm", customerMap.get(key));
						customers.add(customer);
					}
				}

				if (productMap.size() > 0) {
					Set<String> keySet = productMap.keySet();
					for (String key : keySet) {
						ObjectNode product = mapper.createObjectNode();
						product.put("pdCd", key);
						product.put("pdNm", productMap.get(key));
						products.add(product);
					}
				}
			}
		} else {
			result.put("message", response.responseBody);
		}

		return result;
	}

	public JsonNode postFinishedTasks(JsonNode filter, String authId, String authPassword) throws ClientProtocolException, UnsupportedEncodingException, JsonProcessingException, URISyntaxException,
			IOException, AuthenticationException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode result = mapper.createObjectNode();

		String endPointExt = "/query/historic-task-instances";
		WeakRestClient.RestResponse response = WeakRestClient.post(this.activitiServiceUri + endPointExt).header("content-type", "application/json").basicAuth(authId, authPassword)
				.bodyAsJsonNode(filter).execute();

		result.put("statusCode", response.statusCode);
		if (response.statusCode == 200) {
			result.set("taskTable", response.asJsonNode());
		} else {
			result.put("message", response.responseBody);
		}

		return result;
	}

	public int getFinishedTasksTotalSize(String authId, String authPassword) throws ClientProtocolException, UnsupportedEncodingException, JsonProcessingException, URISyntaxException, IOException, AuthenticationException {
		int totalSize = -1;

		String endPointExt = "/history/historic-task-instances";
		WeakRestClient.RestResponse response = WeakRestClient.get(this.activitiServiceUri + endPointExt)
		// .header("content-type", "application/json")
				.basicAuth(authId, authPassword)
				// .bodyAsJsonNode(filter)
				.execute();

		if (response.statusCode == 200) {
			JsonNode rootNode = response.asJsonNode();
			totalSize = rootNode.get("total").asInt();
		}

		return totalSize;
	}

	public JsonNode getBasicListsForFilteringOfFinishedTasks(int totalTasksSize, String authId, String authPassword) throws ClientProtocolException, URISyntaxException, IOException, AuthenticationException {
		final ObjectMapper mapper = new ObjectMapper();
		ObjectNode result = mapper.createObjectNode();

		String endPointExt = "/history/historic-task-instances";

		if (totalTasksSize <= 0) {
			result.put("statusCode", -100);
			result.put("message", "Given total tasks' size <=0");
			return result;
		}

		WeakRestClient.RestResponse response = WeakRestClient.get(this.activitiServiceUri + endPointExt).basicAuth(authId, authPassword).queryString("taskAssignee", authId)
				.queryString("includeProcessVariables", "true").queryString("finished", "true").queryString("size", "" + totalTasksSize).execute();

		// set status code to see if the processing has no error
		result.put("statusCode", response.statusCode);

		if (response.statusCode == 200) {
			// Now we have to extract various data

			// Get task urls
			JsonNode rootNode = response.asJsonNode();
			JsonNode dataNode = rootNode.get("data");

			ArrayNode taskUrls = mapper.createArrayNode();
			ArrayNode categories = mapper.createArrayNode();
			ArrayNode processDefinitions = mapper.createArrayNode();
			ArrayNode customers = mapper.createArrayNode();
			ArrayNode products = mapper.createArrayNode();

			// firstly put the lists to result
			result.set("taskUrls", taskUrls);
			result.set("categories", categories);
			result.set("processDefinitions", processDefinitions);
			result.set("customers", customers);
			result.set("products", products);

			// fill the individual lists
			if (dataNode.isArray()) {
				JsonNode node = null;
				for (JsonNode task : (ArrayNode) dataNode) {

					node = task.path("url");
					if (!node.isMissingNode())
						taskUrls.add(node.asText());

					// node = task.path("processDefinition"); // in runtime
					node = task.path("processDefinitionUrl"); // in historic
					if (!node.isMissingNode())
						processDefinitions.add(node.asText());

					node = task.path("category");
					if (!node.isMissingNode())
						categories.add(node.asText());

					node = task.path("variables");
					if (node.isArray()) {
						JsonNode variableName = null;
						JsonNode variableValue = null;
						JsonNode customerId = null;
						JsonNode customerName = null;
						JsonNode productCd = null;
						JsonNode productName = null;

						for (JsonNode variable : (ArrayNode) node) {
							variableName = variable.path("name");
							if (!variableName.isMissingNode() && !variableName.isNull() && variableName.asText().equals("custId")) {
								variableValue = variable.path("value");
								if (!variableValue.isMissingNode() && !variableValue.isNull())
									customerId = variableValue;
							}

							if (!variableName.isMissingNode() && !variableName.isNull() && variableName.asText().equals("custNm")) {
								variableValue = variable.path("value");
								if (!variableValue.isMissingNode() && !variableValue.isNull())
									customerName = variableValue;
							}

							if (!variableName.isMissingNode() && !variableName.isNull() && variableName.asText().equals("pdCd")) {
								variableValue = variable.path("value");
								if (!variableValue.isMissingNode() && !variableValue.isNull())
									productCd = variableValue;
							}

							if (!variableName.isMissingNode() && !variableName.isNull() && variableName.asText().equals("pdNm")) {
								variableValue = variable.path("value");
								if (!variableValue.isMissingNode() && !variableValue.isNull())
									productName = variableValue;
							}
						}

						ObjectNode customer = mapper.createObjectNode();
						customer.put("custId", customerId.asText());
						customer.put("custNm", customerName.asText());
						customers.add(customer);

						ObjectNode product = mapper.createObjectNode();
						product.put("pdCd", productCd.asText());
						product.put("pdNm", productName.asText());
						products.add(product);

					}
				}
			}
		} else {
			result.put("message", response.responseBody);
		}

		return result;
	}

	public JsonNode getProcessKeyNameCategories(JsonNode processDefinitionUrls, String authId, String authPassword) throws ClientProtocolException, URISyntaxException, IOException, AuthenticationException {
		final ObjectMapper mapper = new ObjectMapper();
		ObjectNode result = mapper.createObjectNode();

		ArrayNode processKeyAndNames = mapper.createArrayNode();
		ArrayNode processCategories = mapper.createArrayNode();
		result.set("processKeyAndNames", processKeyAndNames);
		result.set("processCategories", processCategories);

		// String endPointExt = "repository/process-definitions";
		Map<String, String> processKeyAndNamesMap = new HashMap<String, String>();
		Set<String> processCategoriesSet = new HashSet<String>();

		if (processDefinitionUrls.isArray()) {
			// Iterate to get individual process definitions
			for (JsonNode processDefinitionUrl : processDefinitionUrls) {
				WeakRestClient.RestResponse response = WeakRestClient.get(processDefinitionUrl.asText()).basicAuth(authId, authPassword).execute();

				result.put("statusCode", response.statusCode);
				if (response.statusCode != 200) {
					result.put("message", "Error while getting process definitions. Definition url: " + processDefinitionUrl.asText());
					return result;
				}

				JsonNode rootNode = response.asJsonNode();
				processKeyAndNamesMap.put(rootNode.path("key").asText(), rootNode.path("name").asText());
				processCategoriesSet.add(rootNode.path("category").asText());
			}

			Set<String> keys = processKeyAndNamesMap.keySet();
			for (String key : keys) {
				ObjectNode processKeyAndName = mapper.createObjectNode();
				processKeyAndName.put("key", key);
				processKeyAndName.put("name", processKeyAndNamesMap.get(key));
				processKeyAndNames.add(processKeyAndName);
			}

			for (String category : processCategoriesSet) {
				processCategories.add(category);
			}

		}

		return result;
	}

	// public JsonNode getTaskUrlsOfCandidateOrAssignedTasks(int totalTasksSize,
	// String userId, String password)
	// throws ClientProtocolException, URISyntaxException, IOException {
	// JsonNode result = null;
	// String endPointExt = "/runtime/tasks";
	//
	//
	//
	// WeakRestClient.RestResponse response = WeakRestClient
	// .get(this.activitiServiceUri + endPointExt)
	// .basicAuth(userId, password)
	// .queryString("candidateOrAssigned", userId)
	// .queryString("size", "" + totalTasksSize)
	// .execute();
	//
	// ObjectMapper mapper = new ObjectMapper();
	// ArrayNode jsonArray = mapper.createArrayNode();
	// result = jsonArray;
	//
	// if(totalTasksSize > 0)
	// if (response.statusCode == 200) {
	// JsonNode rootNode = response.asJsonNode();
	// JsonNode dataNode = rootNode.get("data");
	// if (dataNode.isArray()) {
	// int tasksSize = dataNode.size();
	// JsonNode urlNode = null;
	// for (int i = 0; i < tasksSize; i++) {
	// urlNode = dataNode.get(i).get("url");
	// // System.out.println(urlNode.isValueNode());
	// jsonArray.add(mapper.createObjectNode().put("url",
	// urlNode.asText()));
	// }
	// }
	// }
	//
	// return result;
	//
	// }
	//
	// public JsonNode getTaskUrlsOfCandidateOrAssigned(int totalTasksSize)
	// throws ClientProtocolException, URISyntaxException, IOException {
	// JsonNode returnJson = null;
	// String endPointExt = "/runtime/tasks";
	//
	// if (totalTasksSize <= 0)
	// return returnJson;
	//
	// WeakRestClient.RestResponse response = WeakRestClient
	// .get(this.activitiServiceUri + endPointExt)
	// .basicAuth("kermit", "kermit")
	// .queryString("candidateOrAssigned", "kermit")
	// .queryString("size", "" + totalTasksSize)
	// // .queryString("includeProcessVariables", "true")
	// // .queryString("finished", "true")
	// // .queryString("owner", "kermit")
	// .execute();
	//
	// ObjectMapper mapper = new ObjectMapper();
	// ArrayNode jsonArray = mapper.createArrayNode();
	//
	// if (response.statusCode == 200) {
	// JsonNode rootNode = response.asJsonNode();
	// JsonNode dataNode = rootNode.get("data");
	// if (dataNode.isArray()) {
	// int tasksSize = dataNode.size();
	// JsonNode urlNode = null;
	// for (int i = 0; i < tasksSize; i++) {
	// urlNode = dataNode.get(i).get("url");
	// // System.out.println(urlNode.isValueNode());
	// jsonArray.add(mapper.createObjectNode().put("url",
	// urlNode.asText()));
	// }
	// }
	// returnJson = jsonArray;
	// }
	//
	// return returnJson;
	//
	// }

	public JsonNode getIdentityLinksOfTasks(JsonNode taskUrls, String authId, String authPassword) throws ClientProtocolException, URISyntaxException, IOException, AuthenticationException {
		if (taskUrls == null || !taskUrls.isArray()) {
			return null;
		}

		ObjectMapper mapper = new ObjectMapper();
		ArrayNode identityLinks = mapper.createArrayNode();

		ArrayNode identityLinksOfATask = null;

		for (JsonNode taskUrl : (ArrayNode) taskUrls) {
			identityLinksOfATask = (ArrayNode) getIdentityLinksOfATask(taskUrl, authId, authPassword);

			for (JsonNode identityLink : identityLinksOfATask) {
				identityLinks.add(identityLink);
			}
		}

		return identityLinks;
	}

	public JsonNode getIdentityLinksOfATask(JsonNode taskUrl, String authId, String authPassword) throws ClientProtocolException, URISyntaxException, IOException, AuthenticationException {
		if (taskUrl == null || taskUrl.isArray())
			return null;

		// JsonNodeUtil.beautifulPrint(taskUrl);
		WeakRestClient.RestResponse response = WeakRestClient.get(taskUrl.asText() + "/identitylinks").basicAuth(authId, authPassword).execute();

		if (response.statusCode == 200) {
			return response.asJsonNode();
		} else {
			return null;
		}
	}

	public JsonNode getUserInfo(String userId, String authId, String authPassword) throws ClientProtocolException, URISyntaxException, IOException, AuthenticationException {
		if (userId == null)
			return null;
		String endPointExt = "/identity/users";
		WeakRestClient.RestResponse response = WeakRestClient.get(this.activitiServiceUri + endPointExt + "/" + userId).basicAuth(authId, authPassword).execute();

		if (response.statusCode == 200) {
			return response.asJsonNode();
		} else {
			return null;
		}
	}

	public JsonNode getGroupInfo(String groupId, String authId, String authPassword) throws ClientProtocolException, URISyntaxException, IOException, AuthenticationException {
		if (groupId == null)
			return null;
		String endPointExt = "/identity/groups";
		WeakRestClient.RestResponse response = WeakRestClient.get(this.activitiServiceUri + endPointExt + "/" + groupId).basicAuth(authId, authPassword).execute();

		if (response.statusCode == 200) {
			return response.asJsonNode();
		} else {
			return null;
		}
	}

	public JsonNode getCandidateUsersOrGroups(JsonNode identityLinks, String authId, String authPassword) throws ClientProtocolException, URISyntaxException, IOException, AuthenticationException {
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
			userNode = getUserInfo(id, authId, authPassword);
			if (userNode != null)
				usersNode.add(userNode);
		}
		for (String id : groupIdSet) {
			groupNode = getGroupInfo(id, authId, authPassword);
			if (groupNode != null) {
				groupsNode.add(groupNode);
			}
		}
		return rootNode;
	}
}


