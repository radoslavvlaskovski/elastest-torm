package io.elastest.etm.service;

import com.spotify.docker.client.DockerClient;

import io.elastest.epm.client.DockerContainer;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.model.external.ExternalTJobExecution;

public class DockerExecution {
    private DockerClient dockerClient;
    private DockerContainer testcontainer, appContainer;
    private String testContainerId, appContainerId;

    private String network;
    private int testContainerExitCode;

    private boolean isExternal = false;

    private TJobExecution tJobExec;
    private TJob tJob;

    private ExternalTJob externalTJob;
    private ExternalTJobExecution externalTJobExec;

    private SutSpecification sut;
    private SutExecution sutExec;
    private boolean withSut;

    public DockerExecution() {
    }

    /* *** For TJob *** */
    public DockerExecution(TJobExecution tJobExec, boolean withSut) {
        this.isExternal = false;
        this.tJobExec = tJobExec;
        this.withSut = withSut;
        this.updateFromTJobExec(tJobExec);
    }

    public DockerExecution(TJobExecution tJobExec) {
        this.isExternal = false;
        this.tJobExec = tJobExec;
        this.withSut = tJobExec.isWithSut();
        this.updateFromTJobExec(tJobExec);
    }

    /* *** For External TJob *** */

    public DockerExecution(ExternalTJobExecution externalTJobExec,
            boolean withSut) {
        this.isExternal = true;
        this.externalTJobExec = externalTJobExec;
        this.withSut = withSut;
        this.updateFromExternalTJobExec(externalTJobExec);
    }

    public DockerExecution(ExternalTJobExecution externalTJobExec) {
        this.isExternal = true;
        this.externalTJobExec = externalTJobExec;
        this.withSut = externalTJobExec.isWithSut();
        this.updateFromExternalTJobExec(externalTJobExec);
    }

    /* Getters and Setters */

    public DockerContainer getTestcontainer() {
        return testcontainer;
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    public void setDockerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public void setTestcontainer(DockerContainer testcontainer) {
        this.testcontainer = testcontainer;
    }

    public DockerContainer getAppContainer() {
        return appContainer;
    }

    public void setAppContainer(DockerContainer appContainer) {
        this.appContainer = appContainer;
    }

    public String getTestContainerId() {
        return testContainerId;
    }

    public void setTestContainerId(String testContainerId) {
        this.testContainerId = testContainerId;
    }

    public String getAppContainerId() {
        return appContainerId;
    }

    public void setAppContainerId(String appContainerId) {
        this.appContainerId = appContainerId;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public Long getExecutionId() {
        if (isExternal) {
            return externalTJobExec.getId();
        } else {
            return tJobExec.getId();
        }
    }

    public boolean isExternal() {
        return isExternal;
    }

    public TJob gettJob() {
        return tJob;
    }

    public void settJob(TJob tJob) {
        this.tJob = tJob;
    }

    public TJobExecution getTJobExec() {
        return tJobExec;
    }

    public void setTJobExec(TJobExecution tJobExec) {
        this.updateFromTJobExec(tJobExec);
    }

    public void updateFromTJobExec(TJobExecution tJobExec) {
        this.tJobExec = tJobExec;
        this.tJob = tJobExec.getTjob();
        this.sut = this.tJob.getSut();
    }

    public ExternalTJob getExternalTJob() {
        return externalTJob;
    }

    public void setExternalTJob(ExternalTJob externalTJob) {
        this.externalTJob = externalTJob;
    }

    public ExternalTJobExecution getExternalTJobExec() {
        return externalTJobExec;
    }

    public void setExternalTJobExec(ExternalTJobExecution externalTJobExec) {
        this.updateFromExternalTJobExec(externalTJobExec);
    }

    public void updateFromExternalTJobExec(
            ExternalTJobExecution externalTJobExec) {
        this.externalTJobExec = externalTJobExec;
        this.externalTJob = externalTJobExec.getExTJob();
        this.sut = this.externalTJob.getSut();
    }

    public SutSpecification getSut() {
        return sut;
    }

    public void setSut(SutSpecification sut) {
        this.sut = sut;
    }

    public boolean isWithSut() {
        return withSut;
    }

    public void setWithSut(boolean withSut) {
        this.withSut = withSut;
    }

    public SutExecution getSutExec() {
        return sutExec;
    }

    public void setSutExec(SutExecution sutExec) {
        this.sutExec = sutExec;
    }

    public int getTestContainerExitCode() {
        return testContainerExitCode;
    }

    public void setTestContainerExitCode(int testContainerExitCode) {
        this.testContainerExitCode = testContainerExitCode;
    }

    @Override
    public String toString() {
        return "DockerExecution [dockerClient=" + dockerClient
                + ", testcontainer=" + testcontainer + ", appContainer="
                + appContainer + ", testContainerId=" + testContainerId
                + ", appContainerId=" + appContainerId + ", network=" + network
                + ", testContainerExitCode=" + testContainerExitCode
                + ", isExternal=" + isExternal + ", tJobExec=" + tJobExec
                + ", tJob=" + tJob + ", externalTJob=" + externalTJob
                + ", externalTJobExec=" + externalTJobExec + ", sut=" + sut
                + ", sutExec=" + sutExec + ", withSut=" + withSut + "]";
    }

}
