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
package com.blackducksoftware.integration.jira.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class JiraProjectMappings {

    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final Set<HubProjectMapping> mappings;

    public JiraProjectMappings(final Set<HubProjectMapping> mappings) {
        this.mappings = mappings;
    }

    public List<HubProject> getHubProjects(final Long jiraProjectId) {
        final List<HubProject> matchingHubProjects = new ArrayList<>();

        if (mappings == null || mappings.isEmpty()) {
            logger.debug("There are no configured project mapping");
            return matchingHubProjects;
        }

        for (final HubProjectMapping mapping : mappings) {
            final JiraProject jiraProject = mapping.getJiraProject();
            final HubProject hubProject = mapping.getHubProject();

            // Check by name because the notifications may be for Hub projects
            // that the User doesnt have access to
            logger.debug("Hub Project                                       : " + hubProject.getProjectName());
            logger.debug("jiraProject.getProjectName() (from config mapping): " + jiraProject.getProjectName());
            logger.debug("jiraProject Id                                    : " + jiraProjectId);
            if ((jiraProject.getProjectId() != null)
                    && (jiraProject.getProjectId().equals(jiraProjectId))) {
                logger.debug("Match!");
                matchingHubProjects.add(hubProject);
            }
        }
        logger.debug("Number of matches found: " + matchingHubProjects.size());
        return matchingHubProjects;
    }

    public int size() {
        if (mappings == null) {
            return 0;
        }
        return mappings.size();
    }
}
