package io.elastest.epm.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class DockerServiceStatus {
    private String statusMsg;
    private DockerServiceStatusEnum status;

    public DockerServiceStatus() {
        this.initToDefault();
    }

    public enum DockerServiceStatusEnum {
        NOT_INITIALIZED("Not initialized"), INITIALIZING(
                "Initializing"), PULLING(
                        "Pulling"), STARTING("Starting"), READY("Ready");

        private String value;

        DockerServiceStatusEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static DockerServiceStatusEnum fromValue(String text) {
            for (DockerServiceStatusEnum b : DockerServiceStatusEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String msg) {
        this.statusMsg = msg;
    }

    public DockerServiceStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DockerServiceStatusEnum status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DockerServiceStatus [statusMsg=" + statusMsg + ", status="
                + status + "]";
    }

    public void initToDefault() {
        this.setStatus(DockerServiceStatusEnum.NOT_INITIALIZED);
        this.setStatusMsg(status.value);
    }
}
