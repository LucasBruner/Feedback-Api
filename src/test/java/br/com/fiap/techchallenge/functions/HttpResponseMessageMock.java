package br.com.fiap.techchallenge.functions;

import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;

public class HttpResponseMessageMock implements HttpResponseMessage {

    private final HttpStatusType status;
    private final String body;

    public HttpResponseMessageMock(HttpStatusType status, String body) {
        this.status = status;
        this.body = body;
    }

    @Override
    public HttpStatusType getStatus() {
        return status;
    }

    @Override
    public String getHeader(String key) {
        return null;
    }

    @Override
    public Object getBody() {
        return body;
    }

    public static class HttpResponseMessageBuilderMock implements Builder {

        private HttpStatusType status;
        private String body;

        @Override
        public Builder status(HttpStatusType status) {
            this.status = status;
            return this;
        }

        @Override
        public Builder header(String key, String value) {
            return this;
        }

        @Override
        public Builder body(Object body) {
            this.body = (String) body;
            return this;
        }

        @Override
        public HttpResponseMessage build() {
            return new HttpResponseMessageMock(status, body);
        }
    }
}
