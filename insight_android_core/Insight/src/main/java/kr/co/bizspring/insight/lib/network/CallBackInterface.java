package kr.co.bizspring.insight.lib.network;

import org.json.JSONException;
import org.json.JSONObject;

public interface CallBackInterface {
	public void toDoInBackground(final JSONObject o) throws JSONException;
    public void onErrorCodefind(final int statusCode, String statusString);
}
