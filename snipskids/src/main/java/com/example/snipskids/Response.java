package com.example.snipskids;

public class Response {

    public enum Action {
        MUSIC,
        STORY,
        NONE
    }
    private int iconRes;
    private int responseStringRes;
    private String responseString;

    private Action action;

    public int getIconRes() {
        return iconRes;
    }

    public int getResponseStringRes() {
        return responseStringRes;
    }

    public String getResponseString() {
        return responseString;
    }

    public Action getAction() {
        return action;
    }

    public Response(int iconRes, int responseStringRes, Action action) {
        this.iconRes = iconRes;
        this.responseStringRes = responseStringRes;
        this.action = action;
    }

    public Response(int iconRes, int responseStringRes) {
        this(iconRes, responseStringRes, Action.NONE);
    }

    public Response(int iconRes, String responseString) {
        this.iconRes = iconRes;
        this.responseString = responseString;
        this.action = Action.NONE;
    }
}
