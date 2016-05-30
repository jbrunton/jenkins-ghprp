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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PullRequestPropertiesStep extends AbstractStepImpl {
    @DataBoundConstructor
    public PullRequestPropertiesStep() {}

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

        @StepContextParameter
        private transient Run build;

        @StepContextParameter
        private transient Launcher launcher;

        @Override
        protected Map run() throws Exception {
            return createProperties();
        }

        private Map createProperties() {
            try {
                EnvVars env = build.getEnvironment(launcher.getListener());
                String branchName = env.get("BRANCH_NAME", null);

                RepositoryId repoId = new RepositoryId("jbrunton", "pocket-timeline-android");
                PullRequestService prService = new PullRequestService();
                List<PullRequest> openRequests = prService.getPullRequests(repoId, "open");

                for (PullRequest pr : openRequests) {
                    final String sourceBranch = pr.getHead().getRef();
                    final String sourceBranchSha = pr.getHead().getSha();
                    if (sourceBranch.equals(branchName)) {
                        final String targetBranch = pr.getBase().getRef();
                        final String targetBranchSha = pr.getBase().getSha();

                        launcher.getListener().getLogger().println("Pull request found: " + pr.getId());
                        launcher.getListener().getLogger().println("Head: " + sourceBranch);
                        launcher.getListener().getLogger().println("Base: " + targetBranch);

                        final String commitSha = "origin/pr/" + pr.getNumber();

                        return new HashMap() {{
                            put("targetBranch", targetBranch);
                            put("targetBranchSha", targetBranchSha);
                            put("sourceBranch", sourceBranch);
                            put("sourceBranchSha", sourceBranchSha);
                            put("commitSha", commitSha);
                        }};
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
