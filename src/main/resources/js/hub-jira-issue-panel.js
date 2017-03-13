/*
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
 */
var detailsModuleId = "details-module";
var customFieldsModuleId = "customfieldmodule";

var hubCustomFields = ["BDS Hub Project", "BDS Hub Project Version", "BDS Hub Component", 
	"BDS Hub Component Version", "BDS Hub Policy Rule", "BDS Hub Component Licenses",
	"BDS Hub Component Usage", "BDS Hub Component Origin", "BDS Hub Component Origin ID",
	"BDS Hub Project Version Nickname"];

var hubCustomFieldValues = [];

function getCustomFieldValues() {
	console.log("getCustomFieldValues()");
	
	// TODO factor out
	for (hubCustomFieldIndex = 0; hubCustomFieldIndex < hubCustomFields.length; hubCustomFieldIndex++) {
		hubCustomFieldValues.push("");
	}
	
	
	var detailsModule = AJS.$('#' + detailsModuleId);
	if(detailsModule.length > 0){
		var customFieldsModule = AJS.$(detailsModule).find('#' + customFieldsModuleId);
		if(customFieldsModule.length > 0){
			var customFieldPropertyList =  AJS.$(customFieldsModule).find(".property-list");
			if(customFieldPropertyList.length > 0){
				var properties = customFieldPropertyList.children();
				if(properties.length > 0){
					for(i=0; i < properties.length; i++){
						var property = properties[i];
						var customFieldPropertyLabel =  AJS.$(property).find("strong.name");
						var customFieldPropertyValueField =  AJS.$(property).find("div.value");
						
						var customFieldName = AJS.$(customFieldPropertyLabel).prop("title");
						var arrayIndex = hubCustomFields.indexOf(customFieldName);
						if (arrayIndex >= 0) {
							console.log("*** Found Hub custom field: " + customFieldName);
							if (customFieldPropertyValueField.length > 0) {
								console.log("custom field has a value");
								var fieldValue = customFieldPropertyValueField[0].innerText;
								console.log("Value: " + fieldValue);
								hubCustomFieldValues[arrayIndex] = fieldValue;

								// TODO hacking for now
								if (customFieldName === "BDS Hub Project") {
									var projectFieldList = AJS.$('.' + "module").find('#' + "hubProjectFieldList");
									
									var listItemElem = document.createElement("li");
									listItemElem.className = "item"; // TODO item-right for every other one
									var ListItemHubWrapDiv = document.createElement("div");
									ListItemHubWrapDiv.className = "hubWrap";
									
									var ListItemFieldLabelStrong = document.createElement("strong");
									ListItemFieldLabelStrong.className = "name";
									var ListItemFieldValueDiv = document.createElement("div");
									ListItemFieldValueDiv.id = "hubProjectName"; // TODO this will vary
									ListItemFieldValueDiv.className = "value";
									ListItemFieldLabelStrong.innerText = customFieldName;
									ListItemFieldValueDiv.innerText = fieldValue;
									
									ListItemHubWrapDiv.append(ListItemFieldLabelStrong);
									ListItemHubWrapDiv.append(ListItemFieldValueDiv);
									listItemElem.append(ListItemHubWrapDiv);
									
									projectFieldList.append(listItemElem);
									
//									<li class="item">
//									<div class="hubWrap">
//										<strong class="name">$i18n.getText("hub.integration.jira.issue.custom.project.label"):</strong>
//										<div id="hubProjectName" class="value">$bdsHubProject</div>
//									</div>
//									</li>
								}

							}
						}
					}
				} else{
					setTimeout(getCustomFieldValues, 100);
				}
			} else{
				setTimeout(getCustomFieldValues, 100);
			}
		} else{
			setTimeout(getCustomFieldValues, 100);
		}
	} else{
		setTimeout(getCustomFieldValues, 100);
	}
}

function hideHubCustomFields(){
	var detailsModule = AJS.$('#' + detailsModuleId);
	if(detailsModule.length > 0){
	var customFieldsModule = AJS.$(detailsModule).find('#' + customFieldsModuleId);
	if(customFieldsModule.length > 0){
		var customFieldPropertyList =  AJS.$(customFieldsModule).find(".property-list");
		if(customFieldPropertyList.length > 0){
			var properties = customFieldPropertyList.children();
			if(properties.length > 0){
				for(i=0; i < properties.length; i++){
					checkPropertyAndHideHubField(properties[i]);
				}
			} else{
				setTimeout(hideHubCustomFields, 100);
			}
		} else{
			setTimeout(hideHubCustomFields, 100);
		}
	} else{
		setTimeout(hideHubCustomFields, 100);
	}
	} else{
		setTimeout(hideHubCustomFields, 100);
	}
}


function checkPropertyAndHideHubField(property){
	var customFieldPropertyLabel =  AJS.$(property).find("strong.name");
	var customFieldPropertyValueField =  AJS.$(property).find("div.value");
	
	var customFieldName = AJS.$(customFieldPropertyLabel).prop("title");
	var arrayIndex = hubCustomFields.indexOf(customFieldName);
	if (arrayIndex >= 0) {
		var displayStyle = AJS.$(property).css("display");
		if(displayStyle && displayStyle != "none"){
			//AJS.$(property).css("display", "none");
			AJS.$(property).remove();
		}
	}
	
	AJS.$(customFieldPropertyValueField).change(function(){
	    alert("The text has been changed.");
	});
}
	