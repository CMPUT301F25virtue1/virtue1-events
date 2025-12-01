package com.example.linko;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;

import org.junit.Test;

public class NotificationsTest extends BaseTestClass{

    @Test
    public void testBasicUI(){
        Intent intent = new Intent(androidx.test.core.app.ApplicationProvider.getApplicationContext(), NotificationsActivity.class);
        ActivityScenario<NotificationsActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.recycler_notifications)).check(matches(isDisplayed()));
        onView(withId(R.id.text_no_notifications)).check(matches(isDisplayed()));
    }

}
