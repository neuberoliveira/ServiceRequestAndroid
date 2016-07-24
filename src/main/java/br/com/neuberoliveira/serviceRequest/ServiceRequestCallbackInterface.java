package br.com.neuberoliveira.serviceRequest;

/**
 * Created by neuber on 14/04/14.
 */
public interface ServiceRequestCallbackInterface{
    void onSuccess(ServiceRequest request);
    void onFail(ServiceRequest request);
}
