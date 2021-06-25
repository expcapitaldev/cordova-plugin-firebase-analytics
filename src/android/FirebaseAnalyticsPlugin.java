package by.chemerisuk.cordova.firebase;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


public class FirebaseAnalyticsPlugin extends ReflectiveCordovaPlugin {
    private static final String TAG = "FirebaseAnalyticsPlugin";

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase Analytics plugin");

        Context context = this.cordova.getActivity().getApplicationContext();

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @CordovaMethod
    private void logEvent(String name, JSONObject params, CallbackContext callbackContext) throws JSONException {
        this.firebaseAnalytics.logEvent(name, parse(params));

        callbackContext.success();
    }

    @CordovaMethod
    private void setUserId(String userId, CallbackContext callbackContext) {
        this.firebaseAnalytics.setUserId(userId);

        callbackContext.success();
    }

    @CordovaMethod
    private void setUserProperty(String name, String value, CallbackContext callbackContext) {
        this.firebaseAnalytics.setUserProperty(name, value);

        callbackContext.success();
    }

    @CordovaMethod
    private void resetAnalyticsData(CallbackContext callbackContext) {
        this.firebaseAnalytics.resetAnalyticsData();

        callbackContext.success();
    }

    @CordovaMethod
    private void setEnabled(boolean enabled, CallbackContext callbackContext) {
        this.firebaseAnalytics.setAnalyticsCollectionEnabled(enabled);

        callbackContext.success();
    }

    @CordovaMethod
    private void setCurrentScreen(String screenName, CallbackContext callbackContext) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        callbackContext.success();
    }

    @CordovaMethod
    private void setDefaultEventParameters(JSONObject params, CallbackContext callbackContext) throws JSONException {
        this.firebaseAnalytics.setDefaultEventParameters(parse(params));

        callbackContext.success();
    }

    private static Bundle parse(JSONObject params) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator<String> it = params.keys();

        while (it.hasNext()) {
            String key = it.next();
            Object value = params.get(key);

            if (value instanceof String) {
                bundle.putString(key, (String)value);
            } else if (value instanceof Integer) {
                bundle.putInt(key, (Integer)value);
            } else if (value instanceof Double) {
                bundle.putDouble(key, (Double)value);
            } else if (value instanceof Long) {
                bundle.putLong(key, (Long)value);
            } else {
                Log.w(TAG, "Value for key " + key + " not one of (String, Integer, Double, Long)");
            }
        }

        return bundle;
    }

	@CordovaMethod()
	private void getAppInstanceId(CallbackContext callbackContext) {
		Task<String> task = this.firebaseAnalytics.getAppInstanceId();

		task.addOnSuccessListener(new OnSuccessListener<String>() {
			@Override
			public void onSuccess(String string) {
				callbackContext.success(string);
			}
		});

		task.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				try {
					Log.e(TAG, "Error retrieving instance id: ", e);
					callbackContext.error(exceptionToJson(task.getException()));
				} catch (JSONException jsonErr) {
					Log.e(TAG, "Error when parsing json", jsonErr);
					callbackContext.error(jsonErr.getMessage());
				}

			}
		});
	}

	private JSONObject exceptionToJson(final Exception exception) throws JSONException {
		return new JSONObject() {
			{
				put("message", exception.getMessage());
				put("cause", exception.getClass().getName());
				put("stacktrace", exception.getStackTrace().toString());
			}
		};
	}
}
