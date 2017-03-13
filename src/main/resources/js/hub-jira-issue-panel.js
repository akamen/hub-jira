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

// Format of each entry: <JIRA custom attribute name>|<category>|<label>
var hubCustomFields = [
	"BDS Hub Project|Project|Project", 
	"BDS Hub Project Version|Project|Version", 
	"BDS Hub Component|Component|Component", 
	"BDS Hub Component Version|Component|Version", 
	"BDS Hub Policy Rule|Policy|Rule", 
	"BDS Hub Component Licenses|Component|Licenses",
	"BDS Hub Component Usage|Component|Usage", 
	"BDS Hub Component Origin|Component|Origin", 
	"BDS Hub Component Origin ID|Component|Origin ID",
	"BDS Hub Project Version Nickname|Project|Nickname"];

function getIndexOfFieldName(targetFieldName) {
	for (i=0; i < hubCustomFields.length; i++) {
		var entry = hubCustomFields[i];
		var parts = entry.split("|");
		var currentFieldName = parts[0];
		if (currentFieldName === targetFieldName) {
			return i;
		}
	}
	return -1;
}

function getFieldCategoryAtIndex(fieldIndex) {
	return getFieldPartAtIndex(1, fieldIndex);
}

function getFieldLabelAtIndex(fieldIndex) {
	return getFieldPartAtIndex(2, fieldIndex);
}

function getFieldPartAtIndex(partIndex, fieldIndex) {
	var entry = hubCustomFields[fieldIndex];
	var parts = entry.split("|");
	var part = parts[partIndex];
	return part;
}

function getCustomFieldValues() {
	var projectLeft=true;
	var componentLeft=true;

	console.log("getCustomFieldValues()");
	
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
						var fieldIndex = getIndexOfFieldName(customFieldName);
						if (fieldIndex >= 0) {
							console.log("*** Found Hub custom field: " + customFieldName);
							if (customFieldPropertyValueField.length > 0) {
								console.log("custom field has a value");
								var fieldValue = customFieldPropertyValueField[0].innerText;
								console.log("Value: " + fieldValue);

								// TODO hacking for now
								var fieldCategoryName = getFieldCategoryAtIndex(fieldIndex);
								if (fieldCategoryName === "Project") {
									var projectFieldList = AJS.$('.' + "module").find('#' + "hubProjectFieldList");
									
									var listItemElem = document.createElement("li");
									if (projectLeft) {
										listItemElem.className = "item";
										projectLeft=false;
									} else {
										listItemElem.className = "item-right fiftyPercent";
										projectLeft=true;
									}
									
									var ListItemHubWrapDiv = document.createElement("div");
									ListItemHubWrapDiv.className = "hubWrap";
									
									var ListItemFieldLabelStrong = document.createElement("strong");
									ListItemFieldLabelStrong.className = "name";
									var ListItemFieldValueDiv = document.createElement("div");
									ListItemFieldValueDiv.id = "hubProjectName"; // TODO this will vary; add to hubCustomFields
									ListItemFieldValueDiv.className = "value";
									var label = getFieldLabelAtIndex(fieldIndex);
									ListItemFieldLabelStrong.innerText = label;
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
	var arrayIndex = getIndexOfFieldName(customFieldName);
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
	