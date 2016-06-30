package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class FilteredNotificationResults {

	private final Map<String, List<FilteredNotificationResult>> policyViolationResults = new HashMap<>();
	private final List<FilteredNotificationResult> policyViolationOverrideResults = new ArrayList<>();
	private final List<FilteredNotificationResult> vulnerabilityResults = new ArrayList<>();

	public Map<String, List<FilteredNotificationResult>> getPolicyViolationResults() {
		return policyViolationResults;
	}

	public List<FilteredNotificationResult> getPolicyViolationOverrideResults() {
		return policyViolationOverrideResults;
	}

	public List<FilteredNotificationResult> getVulnerabilityResults() {
		return vulnerabilityResults;
	}

	public void addPolicyViolationResult(final String key, final FilteredNotificationResult notificationResult) {
		if (policyViolationResults.get(key) != null) {
			policyViolationResults.get(key).add(notificationResult);
		} else {
			final List<FilteredNotificationResult> results = new ArrayList<>();
			results.add(notificationResult);
			policyViolationResults.put(key, results);
		}
	}

	public void addPolicyViolationOverrideResult(final FilteredNotificationResult notificationResult) {
		policyViolationOverrideResults.add(notificationResult);
	}

	public void addVulnerabilityResult(final FilteredNotificationResult notificationResult) {
		vulnerabilityResults.add(notificationResult);
	}

	public void addAllResults(final FilteredNotificationResults results) {
		for (final Entry<String, List<FilteredNotificationResult>> entry : results.getPolicyViolationResults().entrySet()) {
			for (final FilteredNotificationResult violationResult : entry.getValue()) {
				addPolicyViolationResult(entry.getKey(), violationResult);
			}
		}
		policyViolationOverrideResults.addAll(results.getPolicyViolationOverrideResults());
		vulnerabilityResults.addAll(results.getVulnerabilityResults());
	}

}
