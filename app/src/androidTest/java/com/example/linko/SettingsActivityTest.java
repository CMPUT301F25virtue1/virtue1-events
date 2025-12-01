package com.example.linko;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;

import org.junit.Test;

public class SettingsActivityTest {

    @Test
    public void testBasicUI(){
        Intent intent = new Intent(androidx.test.core.app.ApplicationProvider.getApplicationContext(), SettingsActivity.class);
        ActivityScenario<SettingsActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.button_delete_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.button_enter_admin)).check(matches(isDisplayed()));
        onView(withId(R.id.all_notifications_card)).check(matches(isDisplayed()));
    }
}
