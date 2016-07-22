package com.neuberdesigns.ServiceRequest;

import android.content.Context;

/**
 * Created by neuber on 05/04/15.
 */
public class GeneralLeads extends ServiceRequest {
    /*'app': this.getDefault(app_id),
    'email': this.getDefault(email),
    'name': this.getDefault(name),
    'password': this.getDefault(password),
    'facebook_id': this.getDefault(facebook_id),
    'facebook_data': this.getDefault(facebook_data),
    'test': this.isTest,*/
    protected static final String BASE_URL = "http://dopaminamob.com.br/general-leads/";
    protected static final String BASE_URL_TEST = "http://localhost/general-leads/slim/public/api/v2/";
    protected Context context;
    protected String endpoint;
    protected String method;
    protected String appId;
    protected String email;
    protected String name;
    protected String password;
    protected String facebookId;
    protected String facebookData;
    protected boolean test = true;

    public GeneralLeads(Context context) {
        this(false, context, null);
    }

    public GeneralLeads(Context context, ServiceRequestCallbackInterface srci) {
        this(false, context, srci);
    }

    public GeneralLeads(boolean isLocal, Context context, ServiceRequestCallbackInterface srci) {
        //super(context, srci);
        super(context);
        if (isLocal)
            baseURL = BASE_URL_TEST;
        else
            baseURL = BASE_URL;

        baseURL = BASE_URL_TEST;
        appId = context.getPackageName();
    }

    @Override
    protected void setStringResources() {

    }

    public GeneralLeads setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public void add() {
        addParam("name", name);
        addParam("email", email);
        addParam("password", password);
        addParam("app", appId);
        addParam("facebook_id", facebookId);
        addParam("facebook_data", facebookData);

        sendRequest("add");
    }

    protected void sendRequest(String endpoint) {
        execute(endpoint);
    }

    public GeneralLeads setName(String name) {
        this.name = name;
        addParam("name", name);
        return this;
    }

    public GeneralLeads setEmail(String email) {
        this.email = email;
        addParam("email", email);
        return this;
    }

    public GeneralLeads setFacebookData(String facebookData) {
        this.facebookData = facebookData;
        addParam("facebook_data", facebookData);
        return this;
    }

    public GeneralLeads setFacebookId(String facebookId) {
        this.facebookId = facebookId;
        addParam("facebook_id", facebookId);
        return this;
    }

    public GeneralLeads setMethod(String method) {
        this.method = method;
        return this;
    }

    public GeneralLeads setPassword(String password) {
        this.password = password;
        addParam("password", password);
        return this;
    }

    public GeneralLeads setTest(boolean test) {
        this.test = test;
        addParam("test", String.valueOf(test));
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public GeneralLeads setAppId(String appId) {
        this.appId = appId;
        addParam("app", appId);
        return this;
    }
}
