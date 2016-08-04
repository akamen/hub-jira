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
package com.blackducksoftware.integration.jira.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.project.ProjectManager;
import com.blackducksoftware.integration.hub.component.api.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.component.api.ComponentVersion;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;
import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.hub.notification.NotificationServiceException;
import com.blackducksoftware.integration.hub.notification.api.NotificationItem;
import com.blackducksoftware.integration.hub.notification.api.NotificationType;
import com.blackducksoftware.integration.hub.notification.api.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.api.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.config.HubProject;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.HubProjectMappings;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.policy.PolicyOverrideNotificationConverter;
import com.blackducksoftware.integration.jira.issue.JiraServices;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.ProjectManagerMock;

public class PolicyOverrideNotificationConverterTest {

	private static final String BOM_COMPONENT_VERSION_POLICY_STATUS_LINK = "bomComponentVersionPolicyStatusLink";
	private static final String TEST_PROJECT_VERSION = "testVersionName";
	private static final String HUB_COMPONENT_NAME = "test Hub Component";
	private static final String HUB_PROJECT_NAME = "test Hub Project";
	private static final String NOTIF_URL = "http://test.notif.url";
	private static final String PROJECT_URL = "http://test.project.url";
	private static final String JIRA_ISSUE_TYPE = "Task";
	private static final String BOM_COMPONENT_VERSION_POLICY_STATUS_LINK_PREFIX = BOM_COMPONENT_VERSION_POLICY_STATUS_LINK;
	private static final String COMPONENT_VERSION_LINK = "http://eng-hub-valid03.dc1.lan/api/components/0934ea45-c739-4b58-bcb1-ee777022ce4f/versions/7c45d411-92ca-45b0-80fc-76b765b954ef";
	private static final String VERSION_NAME = "versionName";
	private static final String PROJECTVERSION_URL = "http://eng-hub-valid03.dc1.lan/api/projects/a3b48f57-9c00-453f-8672-804e08c317f2/versions/7d4fdbed-936b-468f-af7f-826dfc072c5b";
	private static final String RULE_URL_PREFIX = "ruleUrl";
	private static final String RULE_NAME = "ruleName";
	private static final String RULE_URL = "http://eng-hub-valid03.dc1.lan/api/policy-rules/138d0d0f-45b5-4e51-8a32-42ed8946434c";
	private static final String RULE_LINK_NAME = "policy-rule";

	private static List<String> rulesIncludingViolatedRule;
	private static List<String> rulesExcludingViolatedRule;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		rulesIncludingViolatedRule = new ArrayList<>();
		rulesIncludingViolatedRule.add("ruleUrl0");
		rulesIncludingViolatedRule.add("ruleUrl");
		rulesIncludingViolatedRule.add("ruleUrl99");

		rulesExcludingViolatedRule = new ArrayList<>();
		rulesExcludingViolatedRule.add("ruleUrl0");
		rulesExcludingViolatedRule.add("ruleUrl1");
		rulesExcludingViolatedRule.add("ruleUrl2");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testWithRuleListWithMatches() throws NotificationServiceException, UnexpectedHubResponseException {
		final HubEvents events = generateEvents(rulesIncludingViolatedRule, true, true);

		assertEquals(1, events.getPolicyViolationOverrideEvents().size());

		assertTrue(events.getPolicyViolationOverrideEvents().get(0).getIssueSummary().contains(HUB_PROJECT_NAME));
		assertTrue(events.getPolicyViolationOverrideEvents().get(0).getIssueSummary()
				.contains(TEST_PROJECT_VERSION));
		assertTrue(events.getPolicyViolationOverrideEvents().get(0).getIssueSummary()
				.contains(HUB_COMPONENT_NAME));
		assertTrue(events.getPolicyViolationOverrideEvents().get(0).getIssueSummary().contains(VERSION_NAME));

		assertTrue(events.getPolicyViolationOverrideEvents().get(0).getIssueSummary().contains(HUB_PROJECT_NAME));
		assertTrue(events.getPolicyViolationOverrideEvents().get(0).getIssueSummary()
				.contains(TEST_PROJECT_VERSION));
		assertTrue(events.getPolicyViolationOverrideEvents().get(0).getIssueSummary()
				.contains(HUB_COMPONENT_NAME));
		assertTrue(events.getPolicyViolationOverrideEvents().get(0).getIssueSummary().contains(VERSION_NAME));
	}

	@Test
	public void testWithRuleListNoMatch() throws NotificationServiceException, UnexpectedHubResponseException {
		final HubEvents events = generateEvents(rulesExcludingViolatedRule, true, true);

		assertEquals(0, events.getPolicyViolationEvents().size());
	}

	@Test
	public void testNoProjectMappingMatch() throws NotificationServiceException, UnexpectedHubResponseException {
		final HubEvents events = generateEvents(rulesIncludingViolatedRule, true, false);
		assertEquals(0, events.getPolicyViolationEvents().size());
	}

	@Test
	public void testWithoutMappings() throws NotificationServiceException, UnexpectedHubResponseException {
		final HubEvents events = generateEvents(rulesIncludingViolatedRule, false, false);

		assertEquals(0, events.getPolicyViolationEvents().size());
	}

	private HubEvents generateEvents(final List<String> rulesToMonitor, final boolean includeProjectMappings,
			final boolean projectMappingMatch) throws NotificationServiceException, UnexpectedHubResponseException {
		final NotificationService mockHubNotificationService = createMockHubNotificationService(true);
		final ProjectManager jiraProjectManager = createJiraProjectManager();
		final ApplicationUserMock jiraUser = new ApplicationUserMock();

		final JiraContext ticketGenInfo = new JiraContext(jiraUser, JIRA_ISSUE_TYPE);

		final JiraServices jiraServices = Mockito.mock(JiraServices.class);
		Mockito.when(jiraServices.getJiraProjectManager()).thenReturn(jiraProjectManager);
		final HubProjectMappings mappings = new HubProjectMappings(jiraServices, ticketGenInfo, createProjectMappings(
				includeProjectMappings, projectMappingMatch));

		final NotificationItem notification = createNotification();

		final NotificationToEventConverter converter = new PolicyOverrideNotificationConverter(mappings, jiraServices,
				ticketGenInfo, rulesToMonitor, mockHubNotificationService);
		final HubEvents events = converter.generateEvents(notification);
		return events;
	}

	private NotificationItem createNotification() {

		final MetaInformation meta = new MetaInformation(null, NOTIF_URL, null);
		final PolicyOverrideNotificationItem notif = new PolicyOverrideNotificationItem(meta);
		notif.setCreatedAt(new Date());
		notif.setType(NotificationType.POLICY_OVERRIDE);
		final PolicyOverrideNotificationContent content = new PolicyOverrideNotificationContent();
		content.setProjectName(HUB_PROJECT_NAME);
		content.setProjectVersionLink(PROJECTVERSION_URL);
		content.setComponentName(HUB_COMPONENT_NAME);
		// final List<ComponentVersionStatus> componentVersionStatuses = new
		// ArrayList<>();
		// final ComponentVersionStatus compVerStatus = new
		// ComponentVersionStatus();
		// compVerStatus.setComponentName(HUB_COMPONENT_NAME_PREFIX);
		// compVerStatus.setComponentVersionLink(COMPONENT_VERSION_LINK_PREFIX);
		// compVerStatus.setBomComponentVersionPolicyStatusLink(BOM_COMPONENT_VERSION_POLICY_STATUS_LINK_PREFIX);
		// componentVersionStatuses.add(compVerStatus);
		content.setBomComponentVersionPolicyStatusLink(BOM_COMPONENT_VERSION_POLICY_STATUS_LINK);
		content.setComponentVersionLink(COMPONENT_VERSION_LINK);
		notif.setContent(content);
		System.out.println("Notif: " + notif);

		return notif;
	}

	private Set<HubProjectMapping> createProjectMappings(final boolean includeMapping, final boolean includeMatch) {

		final Set<HubProjectMapping> mappings = new HashSet<>();

		if (includeMapping) {
			String suffix;
			if (includeMatch) {
				suffix = "";
			} else {
				suffix = "XX";
			}

			final HubProjectMapping mapping = new HubProjectMapping();
			final HubProject hubProject = new HubProject();
			hubProject.setProjectName(HUB_PROJECT_NAME);
			hubProject.setProjectUrl(PROJECT_URL + suffix);
			mapping.setHubProject(hubProject);
			final JiraProject jiraProject = new JiraProject();
			jiraProject.setProjectId(ProjectManagerMock.JIRA_PROJECT_ID_BASE);
			jiraProject.setProjectName(ProjectManagerMock.JIRA_PROJECT_PREFIX);
			mapping.setJiraProject(jiraProject);

			System.out.println("Mapping: " + mapping);
			mappings.add(mapping);
		}
		return mappings;
	}

	private ProjectManager createJiraProjectManager() {
		final ProjectManagerMock projectManager = new ProjectManagerMock();
		projectManager.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());
		return projectManager;
	}

	private NotificationService createMockHubNotificationService(final boolean ruleMatches)
			throws NotificationServiceException, UnexpectedHubResponseException {
		String suffix;
		if (ruleMatches) {
			suffix = "";
		} else {
			suffix = "XX";
		}
		final NotificationService mockHubNotificationService = Mockito.mock(NotificationService.class);

		final ReleaseItem releaseItem = getReleaseItem();
		Mockito.when(mockHubNotificationService
				.getProjectReleaseItemFromProjectReleaseUrl(PROJECTVERSION_URL))
				.thenReturn(releaseItem);
		List<MetaLink> links = new ArrayList<>();
		MetaInformation meta = new MetaInformation(null, null, links);
		final ComponentVersion componentVersion = new ComponentVersion(meta);
		componentVersion.setVersionName(VERSION_NAME);
		Mockito.when(mockHubNotificationService.getComponentVersion(COMPONENT_VERSION_LINK))
		.thenReturn(componentVersion);

		links = new ArrayList<>();

		links.add(new MetaLink(RULE_LINK_NAME, RULE_URL_PREFIX + suffix));

		meta = new MetaInformation(null, RULE_URL, null);
		final PolicyRule rule = new PolicyRule(meta, RULE_NAME, "description", true, true, null,
				"createdAt", "createdBy", "updatedAt", "updatedBy");
		Mockito.when(mockHubNotificationService.getPolicyRule(RULE_URL_PREFIX)).thenReturn(rule);

		meta = new MetaInformation(null, null, links);
		final BomComponentVersionPolicyStatus status = new BomComponentVersionPolicyStatus(meta);
		Mockito.when(
				mockHubNotificationService.getPolicyStatus(BOM_COMPONENT_VERSION_POLICY_STATUS_LINK_PREFIX))
				.thenReturn(status);

		return mockHubNotificationService;
	}

	private ReleaseItem getReleaseItem() {
		final List<MetaLink> links = new ArrayList<>();
		final MetaLink link = new MetaLink("project", PROJECT_URL);
		links.add(link);
		final MetaInformation _meta = new MetaInformation(null, PROJECTVERSION_URL, links);
		final ReleaseItem releaseItem = new ReleaseItem(TEST_PROJECT_VERSION, "testPhase",
				"testDistribution", "testSource", _meta);
		return releaseItem;
	}

}