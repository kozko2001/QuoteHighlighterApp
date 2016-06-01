package net.coscolla.highlight.utils;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticsUtils {

  private final FirebaseAnalytics analytics;

  public AnalyticsUtils(FirebaseAnalytics firebaseAnalytics) {
    this.analytics = firebaseAnalytics;
  }

  public void logEvent(String event) {
    Bundle params = new Bundle();
    analytics.logEvent(event, params);
  }

  public void setUserProperty(String key, String value) {
    analytics.setUserProperty(key, value);
  }
}
