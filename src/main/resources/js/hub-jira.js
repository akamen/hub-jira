/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
var statusMessageFieldId = "aui-hub-message-field";
var statusMessageTitleId = "aui-hub-message-title";
var statusMessageTitleTextId = "aui-hub-message-title-text";
var statusMessageTextId = "aui-hub-message-text";

var errorStatus = "error";
var successStatus = "success";

var hiddenClass = "hidden";

var hubProjectMappingContainer = "hubProjectMappingContainer";
var hubProjectMappingElement = "hubProjectMappingElement";

var jiraProjectListId = "jiraProjects";
var hubProjectListId = "hubProjects";

var jiraProjectDisplayName = "projectName";
var jiraProjectKey = "projectId";
var jiraProjectExists = "projectExists";
var hubProjectDisplayName = "projectName";
var hubProjectKey = "projectUrl";
var hubProjectExists = "projectExists";

var spinning = false;

var mappingElementCounter = 0;

function updateConfig() {
		putConfig(AJS.contextPath() + '/rest/hub-jira-integration/1.0/', 'Save successful.', 'The configuration is not valid.');
	}

function putConfig(restUrl, successMessage, failureMessage) {
	var jsonMappingArray = getJsonArrayFromMapping();
	  AJS.$.ajax({
	    url: restUrl,
	    type: "PUT",
	    dataType: "json",
	    contentType: "application/json",
	    data: '{ "intervalBetweenChecks": "' + encodeURI(AJS.$("#intervalBetweenChecks").val())
	    + '", "hubProjectMappings": ' + jsonMappingArray
	    + '}',
	    processData: false,
	    success: function() {
	    	hideError('intervalBetweenChecksError');
	    	hideError('hubProjectMappingsError');
	    	
		    showStatusMessage(successStatus, 'Success!', successMessage);
		    stopProgressSpinner();
	    },
	    error: function(response){
	    	var config = JSON.parse(response.responseText);
	    	handleError('intervalBetweenChecksError', config.intervalBetweenChecksError);
	    	handleError('hubProjectMappingsError', config.hubProjectMappingError);
	    	
		    showStatusMessage(errorStatus, 'ERROR!', failureMessage);
		    stopProgressSpinner();
	    }
	  });
}

function getJsonArrayFromMapping(){
	var jsonArray = "[";
	var mappingContainer = AJS.$("#" + hubProjectMappingContainer);
	var mappingElements = mappingContainer.children();
	for (i = 0; i < mappingElements.length; i++) {
		if(i > 0){
			jsonArray += ","
		}
		var mappingElement = mappingElements[i];
		var currentJiraProject = AJS.$(mappingElement).find("input[name*='jiraProject']");
		
		var currentJiraProjectDisplayName = currentJiraProject.val();
		var currentJiraProjectValue = currentJiraProject.attr('projectKey');
		var currentJiraProjectExists = currentJiraProject.attr('projectExists');
		
		var currentHubProject = AJS.$(mappingElement).find("input[name*='hubProject']");
		
		var currentHubProjectDisplayName = currentHubProject.val();
		var currentHubProjectValue = currentHubProject.attr('projectKey');
		var currentHubProjectExists = currentHubProject.attr('projectExists');
		
		jsonArray += '{"'
			+'jiraProject" : {"' 
			+ jiraProjectDisplayName + '":"' + currentJiraProjectDisplayName 
			+ '","' 
			+ jiraProjectKey + '":"' + currentJiraProjectValue 
			+ '","' 
			+ jiraProjectExists + '":"' + currentJiraProjectExists 
			+'"},"' 
			+ 'hubProject" : {"' 
			+  hubProjectDisplayName + '":"' + currentHubProjectDisplayName
			+ '","' 
			+  hubProjectKey + '":"' + currentHubProjectValue
			+ '","' 
			+  hubProjectExists + '":"' + currentHubProjectExists
			+ '"}}';
	}
	jsonArray += "]";
	return jsonArray;
}

function populateForm() {
	  AJS.$.ajax({
	    url: AJS.contextPath() + "/rest/hub-jira-integration/1.0/",
	    dataType: "json",
	    success: function(config) {
	      updateValue("intervalBetweenChecks", config.intervalBetweenChecks);
	      fillInProjects(config.jiraProjects, config.hubProjects);
	      fillInMappings(config.hubProjectMappings);
	      
	      handleError('intervalBetweenChecksError', config.intervalBetweenChecksError);
	      handleError('hubProjectMappingsError', config.hubProjectMappingError);
	    }
	  });
	}

function updateValue(fieldId, configField) {
	if(configField){
		 AJS.$("#" + fieldId).val(decodeURI(configField));
    }
}

function fillInProjects(jiraProjects, hubProjects){
	var mappingElement = AJS.$("#" + hubProjectMappingElement);
	var jiraProjectList = mappingElement.find("datalist[id='"+ jiraProjectListId +"']");
	if(jiraProjects != null && jiraProjects.length > 0){
		for (j = 0; j < jiraProjects.length; j++) {
			var newOption = AJS.$('<option>', {
			    value: jiraProjects[j].projectName,
			    projectKey: jiraProjects[j].projectId,
			    projectExists: jiraProjects[j].projectExists
			});
			
			jiraProjectList.append(newOption);
		}
	}
	
	var hubProjectList = mappingElement.find("datalist[id='"+ hubProjectListId +"']");
	if(hubProjects != null && hubProjects.length > 0){
		for (h = 0; h < hubProjects.length; h++) {
			var newOption = AJS.$('<option>', {
			    value: hubProjects[h].projectName,
			    projectKey: hubProjects[h].projectUrl,
			    projectExists: hubProjects[h].projectExists
			});
			hubProjectList.append(newOption);
		}
	}
}

function fillInMappings(storedMappings){
	var mappingContainer = AJS.$("#" + hubProjectMappingContainer);
	var mappingElements = mappingContainer.children();
	// On loading the page, there should only be one original mapping element
	if(storedMappings != null && storedMappings.length > 0){
		fillInMapping(mappingElements[0], storedMappings[0]);
		
		for (i = 1; i < storedMappings.length; i++) {
			var newMappingElement = addNewMappingElement(hubProjectMappingElement);
			fillInMapping(newMappingElement, storedMappings[i]);
		}
	}
}

function fillInMapping(mappingElement, storedMapping){
	var currentJiraProject = AJS.$(mappingElement).find("input[name*='jiraProject']");
	
	var storedJiraProject = storedMapping.jiraProject;
	var storedJiraProjectDisplayName = storedJiraProject.projectName;
	var storedJiraProjectValue = storedJiraProject.projectId;
	var storedJiraProjectExists = storedJiraProject.projectExists;
	
	if(!storedJiraProjectExists){
		currentJiraProject.css("color", "red");
	} else{
		currentJiraProject.css("color", "black");
	}
	
	currentJiraProject.val(storedJiraProjectDisplayName);
	currentJiraProject.attr("projectKey", storedJiraProjectValue);
	currentJiraProject.attr("projectExists",storedJiraProjectExists)
	
	var currentHubProject = AJS.$(mappingElement).find("input[name*='hubProject']");
	
	var storedHubProject = storedMapping.hubProject;
	var storedHubProjectDisplayName = storedHubProject.projectName;
	var storedHubProjectValue = storedHubProject.projectUrl;
	var storedHubProjectExists = storedHubProject.projectExists;
	
	if(!storedHubProjectExists){
		currentHubProject.css("color", "red");
	} else{
		currentHubProject.css("color", "black");
	}
	currentHubProject.val(storedHubProjectDisplayName);
	currentHubProject.attr("projectKey", storedHubProjectValue);
	currentHubProject.attr("projectExists",storedHubProjectExists)
}

function handleError(fieldId, configField) {
	if(configField){
		showError(fieldId, configField);
    } else{
    	hideError(fieldId);
    }
}

function showError(fieldId, configField) {
	  AJS.$("#" + fieldId).text(decodeURI(configField));
  	  removeClassFromField(fieldId, hiddenClass);
}

function hideError(fieldId) {
	  AJS.$("#" + fieldId).text('');
  	  addClassToField(fieldId, hiddenClass);
}

function showStatusMessage(status, statusTitle, message) {
	resetStatusMessage();
	if(status == errorStatus){
		addClassToField(statusMessageFieldId, 'error');
		addClassToField(statusMessageTitleId, 'icon-error');
	} else if (status == successStatus){
		addClassToField(statusMessageFieldId, 'success');
		addClassToField(statusMessageTitleId, 'icon-success');
	}
	AJS.$("#" + statusMessageTitleTextId).text(statusTitle);
	AJS.$("#" + statusMessageTextId).text(message);
	removeClassFromField(statusMessageFieldId, hiddenClass);
}

function resetStatusMessage() {
	removeClassFromField(statusMessageFieldId,'error');
	removeClassFromField(statusMessageFieldId,'success');
	removeClassFromField(statusMessageTitleId,'icon-error');
	removeClassFromField(statusMessageTitleId,'icon-success');
	AJS.$("#" + statusMessageTitleTextId).text('');
	AJS.$("#" + statusMessageTextId).text('');
	addClassToField(statusMessageFieldId, hiddenClass);
}

function addClassToField(fieldId, cssClass){
	if(!AJS.$("#" + fieldId).hasClass(cssClass)){
		AJS.$("#" + fieldId).addClass(cssClass);
	}
}

function removeClassFromField(fieldId, cssClass){
	if(AJS.$("#" + fieldId).hasClass(cssClass)){
		AJS.$("#" + fieldId).removeClass(cssClass);
	}
}

function startProgressSpinner(){
	 if (!spinning) {
		 var spinner = AJS.$('.spinner');
		 spinner.spin('large');
		 spinning = true;
	 }
}

function stopProgressSpinner(){
	 if (spinning) {
		 AJS.$('.spinner').spinStop();
         spinning = false;
	 }
}

function addNewMappingElement(fieldId){
	var elementToAdd = AJS.$("#" + fieldId).clone();
	mappingElementCounter = mappingElementCounter + 1;
	elementToAdd.attr("id", elementToAdd.attr("id") + mappingElementCounter);
	elementToAdd.appendTo("#" + hubProjectMappingContainer);
	
	var currentJiraProject = AJS.$(elementToAdd).find("input[name*='jiraProject']");
	
	currentJiraProject.val("");
	currentJiraProject.attr("projectKey", "");
	currentJiraProject.attr("projectExists","true");
	currentJiraProject.css("color", "black");
	
	var currentHubProject = AJS.$(elementToAdd).find("input[name*='hubProject']");
	
	currentHubProject.val("");
	currentHubProject.attr("projectKey", "");
	currentHubProject.attr("projectExists","true");
	currentHubProject.css("color", "black");
	
	return elementToAdd;
}

function removeMappingElement(childElement){
	if(AJS.$("#" + hubProjectMappingContainer).children().length > 1){
		AJS.$(childElement).closest("div[name*='"+ hubProjectMappingElement + "']").remove();
	}
}

function onMappingInputChange(inputField){
	var field = AJS.$(inputField);
	var datalist = inputField.list;
	var options = datalist.options;
	
	var optionFound = false;
    for (var i=0;i<options.length;i++){
       if (options[i].value == inputField.value) { 
    	   optionFound = true;
    	   var option = AJS.$(options[i]);
    	   
    	   var projectKey = option.attr("projectKey");
    	   var projectExists = option.attr("projectExists");
    	   field.val(option.val());
    	   field.attr("projectKey", projectKey);
    	   field.attr("projectExists", projectExists)
    	   
    	   if(projectExists === 'false'){
    		   field.css("color", "red");
    		} else{
    			field.css("color", "black");
    		}
    	   break;
    	}
    }
    if(!optionFound){
  	   field.attr("projectKey", "");
  	   field.attr("projectExists", "false")
  	   field.css("color", "red");
    }
}

(function ($) {
	populateForm();
})(AJS.$ || jQuery);

