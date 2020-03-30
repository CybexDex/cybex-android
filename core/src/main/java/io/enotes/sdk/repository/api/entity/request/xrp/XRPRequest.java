package io.enotes.sdk.repository.api.entity.request.xrp;

import java.util.ArrayList;
import java.util.List;

public class XRPRequest<T> {
    private String method;
    private List<T> params;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<T> getParams() {
        return params;
    }

    public void setParams(List<T> params) {
        this.params = params;
    }

    public static <Z> XRPRequest<Z> getXRPRequest(String method, Z z) {
        XRPRequest<Z> request = new XRPRequest<>();
        request.setMethod(method);
        List<Z> list = new ArrayList<>();
        list.add(z);
        request.setParams(list);
        return request;
    }
}
