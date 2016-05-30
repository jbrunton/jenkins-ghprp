# Jenkins Github Pull Request Properties

Provides context about the open pull request (target branch, merge commit, etc.) for a given branch, to make it easier to run CI against the merge.

## Installation

Download from [releases](https://github.com/jbrunton/jenkins-ghprp/releases). Put the hpi file in your Jenkins plugins directory and restart Jenkins.

## What's this for?

Jenkins 2 includes the awesome new multibranch pipeline workflow, which automatically creates projects for each branch in the repository.

A common scenario for such a workflow is to build and test against the pull request merge commit. However, out of the box Jenkins doesn't provide the build environment with any context about open pull requests, so this isn't possible (and the Github Pull Request Builder plugin doesn't seem to work with this new workflow).

## How does this plugin work?

It provides a method which will query the Github repository for open pull requests and find the one associated with the branch under test. This can be used in a jenkins pipeline script like so:

```groovy
def pr = pullRequestProperties(
  branchName: env.BRANCH_NAME,
  repository: 'my/repo'
)
echo "Found pull request from $pr.sourceBranch to $pr.targetBranch. Checking out $pr.mergeRef"
```

Sample log from above job:

    Found pull request from dev-branch to master. Checking out origin/pr/101

## Available Properties

Given `pr` as above, the following properties are available:

|Property          |Description                      |Example            |
|------------------|---------------------------------|-------------------|
|pr.sourceBranch   |The source branch                |`dev-branch`       |
|pr.sourceBranchRef|Ref name for the source branch   |`origin/dev-branch`|
|pr.targetBranch   |The target branch                |`master`           |
|pr.targetBranchRef|Ref name for the target branch   |`origin/master`    |
|pr.mergeRef       |Ref of the merge commit          |`origin/pr/101`    |
