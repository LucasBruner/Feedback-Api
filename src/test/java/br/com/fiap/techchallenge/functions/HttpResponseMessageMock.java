package br.com.fiap.techchallenge.functions;

import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;

import java.util.HashMap;
import java.util.Map;

public class HttpResponseMessageMock implements HttpResponseMessage {

    private final HttpStatusType status;
    private final Map<String, String> headers;
    private final Object body;

    public HttpResponseMessageMock(HttpStatusType status, Map<String, String> headers, Object body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public HttpStatusType getStatus() {
        return status;
    }

    @Override
    public String getHeader(String key) {
        return headers.get(key);
    }

    @Override
    public Object getBody() {
        return body;
    }

    public static class HttpResponseMessageBuilderMock implements Builder {

        private HttpStatusType status;
        private final Map<String, String> headers = new HashMap<>();
        private Object body;

        public Builder status(HttpStatusType status) {
            this.status = status;
            return this;
        }

        @Override
        public Builder header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        @Override
        public Builder body(Object body) {
            this.body = body;
            return this;
        }

        @Override
        public HttpResponseMessage build() {
            return new HttpResponseMessageMock(status, headers, body);
        }
    }
}
