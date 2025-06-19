package common.dto;

import common.util.ExecutionResponse;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 2L;
    private ExecutionResponse executionResponse;

    public Response(ExecutionResponse executionResponse) {
        this.executionResponse = executionResponse;
    }

    public ExecutionResponse getExecutionResponse() {
        return executionResponse;
    }

    @Override
    public String toString() {
        return "Response{" + "executionResponse=" + executionResponse + '}';
    }
}