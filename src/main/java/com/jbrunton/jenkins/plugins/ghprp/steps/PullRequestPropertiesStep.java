package com.jbrunton.jenkins.plugins.ghprp.steps;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Run;
import org.apache.commons.io.FileUtils;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PullRequestPropertiesStep extends AbstractStepImpl {
    private final String branchName;
    private final String repository;

    @DataBoundConstructor
    public PullRequestPropertiesStep(String branchName, String repository) {
        this.branchName = branchName;
        this.repository = repository;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() { super(Execution.class); }

        @Override
        public String getFunctionName() {
            return "pullRequestProperties";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Github pull request properties";
        }
    }


    public static final class Execution extends AbstractSynchronousNonBlockingStepExecution<Map> {

        @Inject
        private transient PullRequestPropertiesStep step;

        @StepContextParameter
        private transient Run build;

        @StepContextParameter
        private transient Launcher launcher;

        @Override
        protected Map run() throws Exception {
            try {
                PullRequest pr = findPullRequestForBranch();
                return propertiesForPullRequest(pr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private PullRequest findPullRequestForBranch() throws IOException {
            List<PullRequest> pullRequests = new LinkedList<>();

            for (PullRequest pr : findOpenPullRequests()) {
                final String sourceBranch = pr.getHead().getRef();
                if (sourceBranch.equals(step.branchName)) {
                    pullRequests.add(pr);
                }
            }

            if (pullRequests.size() == 1) {
                return pullRequests.get(0);
            } else {
                throw new RuntimeException("Expected to find one pull request for this branch, but found " + pullRequests.size());
            }
        }

        private List<PullRequest> findOpenPullRequests() throws IOException {
            RepositoryId repoId = RepositoryId.createFromId(step.repository);
            PullRequestService prService = new PullRequestService();
            return prService.getPullRequests(repoId, "open");
        }

        private Map propertiesForPullRequest(PullRequest pr) {
            Map<String, String> properties = new HashMap<>();

            final String sourceBranch = pr.getHead().getRef();
            properties.put("sourceBranch", sourceBranch);
            properties.put("sourceBranchRef", "origin/" + sourceBranch);

            final String targetBranch = pr.getBase().getRef();
            properties.put("targetBranch", targetBranch);
            properties.put("targetBranchRef", "origin/" + targetBranch);

            final String mergeRef = "origin/pr/" + pr.getNumber();
            properties.put("mergeRef", mergeRef);

            return properties;
        }

    }
}
