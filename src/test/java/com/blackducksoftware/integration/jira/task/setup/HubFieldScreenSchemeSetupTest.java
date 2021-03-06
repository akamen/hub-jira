/**
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
package com.blackducksoftware.integration.jira.task.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.field.CustomFieldManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.CustomFieldMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeItemMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenTabMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeMock;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;

public class HubFieldScreenSchemeSetupTest {

    private static final int NUM_FIELDS_VULNERABILITY = 9;

    private static final int NUM_FIELDS_POLICY = NUM_FIELDS_VULNERABILITY + 1;

    @Test
    public void testAddHubFieldConfigurationToJiraOneMissingIssueTypeAssoc() throws GenericEntityException {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

        final JiraServicesMock jiraServices = new JiraServicesMock();
        final CustomFieldManagerMock customFieldManagerMock = new CustomFieldManagerMock();
        final FieldManager fieldManager = new FieldManagerMock(customFieldManagerMock);
        jiraServices.setFieldManager(fieldManager);
        final FieldScreenManager fieldScreenManager = new FieldScreenManagerMock();
        jiraServices.setFieldScreenManager(fieldScreenManager);
        final FieldScreenSchemeManager fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();
        jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);

        final HubFieldScreenSchemeSetup fieldConfigSetupOrig = new HubFieldScreenSchemeSetup(settingService,
                jiraServices);
        final HubFieldScreenSchemeSetup fieldConfigSetup = Mockito.spy(fieldConfigSetupOrig);
        Mockito.when(fieldConfigSetup.createNewScreenSchemeImpl(Mockito.any(FieldScreenSchemeManager.class)))
                .thenAnswer(new Answer<FieldScreenScheme>() {
                    @Override
                    public FieldScreenScheme answer(final InvocationOnMock invocation) throws Throwable {
                        return new FieldScreenSchemeMock();
                    }
                });
        final FieldScreen screen = new FieldScreenMock();
        Mockito.when(fieldConfigSetup.createNewScreenImpl(Mockito.any(FieldScreenManager.class))).thenReturn(screen);
        // Mockito.doReturn(screen).when(fieldConfigSetup.createNewScreenImpl(Mockito.any(FieldScreenManager.class)));

        // Create a custom field
        jiraServices.setCustomFieldManager(customFieldManagerMock);
        final CustomField customField = jiraServices.getCustomFieldManager().createCustomField(HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT,
                HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT, null,
                null, null, null);
        final List<IssueType> hubIssueTypes = getHubIssueTypes();
        final CustomFieldMock customFieldMock = (CustomFieldMock) customField;

        // Associated only ONE Hub IssueType with it (an incomplete/broken config)
        final List<IssueType> associatedIssueTypes = new ArrayList<>();
        associatedIssueTypes.add(hubIssueTypes.get(0));
        customFieldMock.setAssociatedIssueTypes(associatedIssueTypes);

        // See how this handles the incomplete config
        fieldConfigSetup.addHubFieldConfigurationToJira(hubIssueTypes);

        assertNotNull(settingsMock);
        assertTrue(((String) settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR))
                .contains("The custom field BDS Hub Project is missing one or more IssueType associations"));
    }

    @Test
    public void testAddHubFieldConfigurationToJiraFirstTimeCreateNullIssueTypes() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

        final JiraServicesMock jiraServices = new JiraServicesMock();

        final HubFieldScreenSchemeSetup fieldConfigSetup = new HubFieldScreenSchemeSetup(settingService,
                jiraServices);
        fieldConfigSetup.addHubFieldConfigurationToJira(null);

        assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
    }

    @Test
    public void testAddHubFieldConfigurationToJiraFirstTimeCreateNoIssueTypes() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

        final JiraServicesMock jiraServices = new JiraServicesMock();

        final HubFieldScreenSchemeSetup fieldConfigSetup = new HubFieldScreenSchemeSetup(settingService,
                jiraServices);
        fieldConfigSetup.addHubFieldConfigurationToJira(new ArrayList<IssueType>());

        assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
    }

    @Test
    public void testAddHubFieldConfigurationToJiraFirstTimeCreate() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

        final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
        final FieldManagerMock fieldManager = new FieldManagerMock(customFieldManager);
        final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
        final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();

        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setCustomFieldManager(customFieldManager);
        jiraServices.setFieldManager(fieldManager);
        jiraServices.setFieldScreenManager(fieldScreenManager);
        jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);

        final List<IssueType> issueTypes = getHubIssueTypes();

        HubFieldScreenSchemeSetup fieldConfigSetup = new HubFieldScreenSchemeSetup(settingService,
                jiraServices);
        fieldConfigSetup = Mockito.spy(fieldConfigSetup);

        Mockito.when(fieldConfigSetup.createNewScreenImpl(Mockito.any(FieldScreenManager.class)))
                .thenAnswer(new Answer<FieldScreen>() {
                    @Override
                    public FieldScreen answer(final InvocationOnMock invocation) throws Throwable {
                        return new FieldScreenMock();
                    }
                });

        Mockito.when(fieldConfigSetup.createNewScreenSchemeImpl(Mockito.any(FieldScreenSchemeManager.class)))
                .thenAnswer(new Answer<FieldScreenScheme>() {
                    @Override
                    public FieldScreenScheme answer(final InvocationOnMock invocation) throws Throwable {
                        return new FieldScreenSchemeMock();
                    }
                });

        Mockito.when(fieldConfigSetup.createNewFieldScreenSchemeItemImpl(Mockito.any(FieldScreenSchemeManager.class),
                Mockito.any(FieldScreenManager.class))).thenAnswer(new Answer<FieldScreenSchemeItem>() {
                    @Override
                    public FieldScreenSchemeItem answer(final InvocationOnMock invocation) throws Throwable {
                        return new FieldScreenSchemeItemMock();
                    }
                });

        fieldConfigSetup.addHubFieldConfigurationToJira(issueTypes);

        assertEquals(NUM_FIELDS_POLICY, customFieldManager.getCustomFieldObjects().size());
        for (final FieldScreen fieldScreen : fieldScreenManager.getUpdatedScreens()) {
            final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
            assertTrue(fieldScreenMock.getAttemptedScreenStore());
        }
        assertTrue(fieldScreenManager.getUpdatedTabs().size() == 2);

        for (final FieldScreenTab tab : fieldScreenManager.getUpdatedTabs()) {
            final String screenName = tab.getFieldScreen().getName();
            if (screenName.equals(HubJiraConstants.HUB_POLICY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_POLICY, tab.getFieldScreenLayoutItems().size());
            } else if (screenName.equals(HubJiraConstants.HUB_POLICY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_VULNERABILITY, tab.getFieldScreenLayoutItems().size());
            }
        }
        assertTrue(fieldScreenManager.getUpdatedScreens().size() == 2);
        for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemeManager.getUpdatedSchemes()) {
            final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
            assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

            for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {
                assertTrue(currentSchemeItem.getFieldScreen().getName()
                        .equals(HubJiraConstants.HUB_POLICY_SCREEN_NAME)
                        || currentSchemeItem.getFieldScreen().getName()
                                .equals(HubJiraConstants.HUB_SECURITY_SCREEN_NAME));
            }
        }
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemes().size() == 2);
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemeItems().size() == 6);
        assertNotNull(settingsMock);
        assertTrue(((String) settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR))
                .contains("The custom field BDS Hub Policy Rule has no IssueType associations"));
    }

    @Test
    public void testAddHubFieldConfigurationToJiraFirstTimeCreateWithDefaultTabsAndFields() {
        doBasicTest(false);
    }

    @Test
    public void testAddHubFieldConfigurationToJiraFirstTimeCreateWithDefaultTabsWithNullFields() {
        doBasicTest(true);
    }

    private void doBasicTest(final boolean includeSomeNullCustomFields) {
        int expectedPolicyFields = NUM_FIELDS_POLICY + 3;
        if (!includeSomeNullCustomFields) {
            expectedPolicyFields++;
        }
        int expectedVulnerabilityFields = 7;
        if (!includeSomeNullCustomFields) {
            expectedVulnerabilityFields++;
        }
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

        final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
        final FieldManagerMock fieldManager = new FieldManagerMock(customFieldManager);
        final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
        final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();

        fieldScreenManager.setDefaultFieldScreen(getDefaultFieldScreen(includeSomeNullCustomFields));

        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setCustomFieldManager(customFieldManager);
        jiraServices.setFieldManager(fieldManager);
        jiraServices.setFieldScreenManager(fieldScreenManager);
        jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);

        final List<IssueType> issueTypes = getHubIssueTypes();

        HubFieldScreenSchemeSetup fieldConfigSetup = new HubFieldScreenSchemeSetup(settingService,
                jiraServices);
        fieldConfigSetup = Mockito.spy(fieldConfigSetup);

        mockCreationMethods(fieldConfigSetup);

        fieldConfigSetup.addHubFieldConfigurationToJira(issueTypes);

        assertEquals(NUM_FIELDS_POLICY, customFieldManager.getCustomFieldObjects().size());
        for (final FieldScreen fieldScreen : fieldScreenManager.getUpdatedScreens()) {
            final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
            assertTrue(fieldScreenMock.getAttemptedScreenStore());
        }
        assertTrue(fieldScreenManager.getUpdatedTabs().size() == 2);

        for (final FieldScreenTab tab : fieldScreenManager.getUpdatedTabs()) {
            final String screenName = tab.getFieldScreen().getName();
            if (screenName.equals(HubJiraConstants.HUB_POLICY_SCREEN_NAME)) {
                assertEquals(expectedPolicyFields, tab.getFieldScreenLayoutItems().size());
            } else if (screenName.equals(HubJiraConstants.HUB_POLICY_SCREEN_NAME)) {
                assertEquals(expectedVulnerabilityFields, tab.getFieldScreenLayoutItems().size());
            }
        }
        assertTrue(fieldScreenManager.getUpdatedScreens().size() == 2);
        for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemeManager.getUpdatedSchemes()) {
            final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
            assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

            for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {
                assertTrue(currentSchemeItem.getFieldScreen().getName()
                        .equals(HubJiraConstants.HUB_POLICY_SCREEN_NAME)
                        || currentSchemeItem.getFieldScreen().getName()
                                .equals(HubJiraConstants.HUB_SECURITY_SCREEN_NAME));
            }
        }
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemes().size() == 2);
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemeItems().size() == 6);
        if (includeSomeNullCustomFields) {
            assertNotNull(settingsMock);
            assertTrue(((String) settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR))
                    .contains("The custom field BDS Hub Policy Rule has no IssueType associations"));
        } else {
            assertNotNull(settingsMock);
            assertTrue(((String) settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR))
                    .contains("The custom field BDS Hub Policy Rule has no IssueType associations"));
        }
    }

    @Test
    public void testAddHubFieldConfigurationToJiraWithUserChanges() throws Exception {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

        final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
        final FieldManagerMock fieldManager = new FieldManagerMock(customFieldManager);
        final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
        final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();
        final FieldScreen defaultScreen = getDefaultFieldScreen(false);
        fieldScreenManager.setDefaultFieldScreen(defaultScreen);

        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setCustomFieldManager(customFieldManager);
        jiraServices.setFieldManager(fieldManager);
        jiraServices.setFieldScreenManager(fieldScreenManager);
        jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);

        final List<IssueType> issueTypes = getHubIssueTypes();

        HubFieldScreenSchemeSetup fieldConfigSetup = new HubFieldScreenSchemeSetup(settingService,
                jiraServices);
        fieldConfigSetup = Mockito.spy(fieldConfigSetup);

        mockCreationMethods(fieldConfigSetup);

        fieldConfigSetup.addHubFieldConfigurationToJira(issueTypes);
        assertEquals(NUM_FIELDS_POLICY, customFieldManager.getCustomFieldObjects().size());
        for (final FieldScreen fieldScreen : fieldScreenManager.getUpdatedScreens()) {
            final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
            assertTrue(fieldScreenMock.getAttemptedScreenStore());
        }
        assertTrue(fieldScreenManager.getUpdatedTabs().size() == 2);

        for (final FieldScreenTab tab : fieldScreenManager.getUpdatedTabs()) {
            final String screenName = tab.getFieldScreen().getName();
            if (screenName.equals(HubJiraConstants.HUB_POLICY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_POLICY + 4, tab.getFieldScreenLayoutItems().size());
            } else if (screenName.equals(HubJiraConstants.HUB_SECURITY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_VULNERABILITY + 4, tab.getFieldScreenLayoutItems().size());
            }
        }
        assertTrue(fieldScreenManager.getUpdatedScreens().size() == 2);
        for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemeManager.getUpdatedSchemes()) {
            final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
            assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

            for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {

                assertTrue(currentSchemeItem.getFieldScreen().getName()
                        .equals(HubJiraConstants.HUB_POLICY_SCREEN_NAME)
                        || currentSchemeItem.getFieldScreen().getName()
                                .equals(HubJiraConstants.HUB_SECURITY_SCREEN_NAME));
            }
        }
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemes().size() == 2);
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemeItems().size() == 6);
        assertNotNull(settingsMock);
        assertTrue(((String) settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR))
                .contains("The custom field BDS Hub Policy Rule has no IssueType associations"));

        // User edits
        final FieldScreenScheme scheme = fieldScreenSchemeManager.getFieldScreenSchemes().iterator().next();
        final FieldScreenSchemeItem schemeItem = scheme.getFieldScreenSchemeItems().iterator().next();
        schemeItem.setFieldScreen(defaultScreen);

        customFieldManager.removeCustomField(customFieldManager.getCustomFields().get(0));

        fieldConfigSetup.addHubFieldConfigurationToJira(issueTypes);

        assertEquals(NUM_FIELDS_POLICY, customFieldManager.getCustomFieldObjects().size());
        for (final FieldScreen fieldScreen : fieldScreenManager.getUpdatedScreens()) {
            final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
            assertTrue(fieldScreenMock.getAttemptedScreenStore());
        }
        assertTrue(fieldScreenManager.getUpdatedTabs().size() == 2);

        for (final FieldScreenTab tab : fieldScreenManager.getUpdatedTabs()) {
            final String screenName = tab.getFieldScreen().getName();
            if (screenName.equals(HubJiraConstants.HUB_POLICY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_POLICY + 4, tab.getFieldScreenLayoutItems().size());
            } else if (screenName.equals(HubJiraConstants.HUB_SECURITY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_VULNERABILITY + 4, tab.getFieldScreenLayoutItems().size());
            }
        }
        assertTrue(fieldScreenManager.getUpdatedScreens().size() == 2);
        for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemeManager.getUpdatedSchemes()) {
            final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
            assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

            for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {
                assertTrue(currentSchemeItem.getFieldScreen().getName()
                        .equals(HubJiraConstants.HUB_POLICY_SCREEN_NAME)
                        || currentSchemeItem.getFieldScreen().getName()
                                .equals(HubJiraConstants.HUB_SECURITY_SCREEN_NAME));
            }
        }
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemes().size() == 2);
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemeItems().size() == 7);
        assertNotNull(settingsMock);
        assertTrue(((String) settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR))
                .contains("The custom field BDS Hub Policy Rule has no IssueType associations"));
    }

    private void mockCreationMethods(final HubFieldScreenSchemeSetup fieldConfigSetup) {
        Mockito.when(fieldConfigSetup.createNewScreenImpl(Mockito.any(FieldScreenManager.class)))
                .thenAnswer(new Answer<FieldScreen>() {
                    @Override
                    public FieldScreen answer(final InvocationOnMock invocation) throws Throwable {
                        return new FieldScreenMock();
                    }
                });

        Mockito.when(fieldConfigSetup.createNewScreenSchemeImpl(Mockito.any(FieldScreenSchemeManager.class)))
                .thenAnswer(new Answer<FieldScreenScheme>() {
                    @Override
                    public FieldScreenScheme answer(final InvocationOnMock invocation) throws Throwable {
                        return new FieldScreenSchemeMock();
                    }
                });

        Mockito.when(fieldConfigSetup.createNewFieldScreenSchemeItemImpl(Mockito.any(FieldScreenSchemeManager.class),
                Mockito.any(FieldScreenManager.class))).thenAnswer(new Answer<FieldScreenSchemeItem>() {
                    @Override
                    public FieldScreenSchemeItem answer(final InvocationOnMock invocation) throws Throwable {
                        return new FieldScreenSchemeItemMock();
                    }
                });
    }

    private FieldScreen getDefaultFieldScreen(final boolean includeSomeNullFields) {
        final FieldScreenMock fieldScreen = new FieldScreenMock();
        final FieldScreenTabMock defaultTab1 = new FieldScreenTabMock();
        defaultTab1.setFieldScreen(fieldScreen);
        defaultTab1.addFieldScreenLayoutItem("Default Field 1");
        if (includeSomeNullFields) {
            defaultTab1.addFieldScreenLayoutItem(null);
        } else {
            defaultTab1.addFieldScreenLayoutItem("Default Field 2");
        }
        defaultTab1.addFieldScreenLayoutItem("Default Field 3");
        final FieldScreenTabMock defaultTab2 = new FieldScreenTabMock();
        defaultTab2.setFieldScreen(fieldScreen);
        defaultTab2.addFieldScreenLayoutItem("Default Field 1");
        if (includeSomeNullFields) {
            defaultTab2.addFieldScreenLayoutItem(null);
        } else {
            defaultTab2.addFieldScreenLayoutItem("Default Field 2");
        }
        defaultTab2.addFieldScreenLayoutItem("Default Field 3");
        defaultTab2.addFieldScreenLayoutItem("Default Field 4");

        fieldScreen.addTab(defaultTab1);
        fieldScreen.addTab(defaultTab2);

        fieldScreen.setName("Default Screen");
        return fieldScreen;
    }

    private List<IssueType> getHubIssueTypes() {
        final List<IssueType> issueTypes = new ArrayList<>();
        addPolicyIssueType(issueTypes);
        addVulnIssueType(issueTypes);
        return issueTypes;
    }

    private void addVulnIssueType(final List<IssueType> issueTypes) {
        final IssueTypeMock securityIssueType = new IssueTypeMock();
        securityIssueType.setName(HubJiraConstants.HUB_VULNERABILITY_ISSUE);
        securityIssueType.setId(HubJiraConstants.HUB_VULNERABILITY_ISSUE);
        securityIssueType.setValue(Mockito.mock(GenericValue.class));
        issueTypes.add(securityIssueType);
    }

    private void addPolicyIssueType(final List<IssueType> issueTypes) {
        final IssueTypeMock policyIssueType = new IssueTypeMock();
        policyIssueType.setName(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE);
        policyIssueType.setId(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE);
        policyIssueType.setValue(Mockito.mock(GenericValue.class));
        issueTypes.add(policyIssueType);
    }
}
