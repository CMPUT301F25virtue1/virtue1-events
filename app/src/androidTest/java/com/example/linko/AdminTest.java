package com.example.linko;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AdminTest extends BaseTestClass{

    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule = new ActivityScenarioRule<>(AdminActivity.class);

    @Test
    public void testBasicUI(){
        Intent intent = new Intent(androidx.test.core.app.ApplicationProvider.getApplicationContext(), AdminActivity.class);
        ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.button_event)).check(matches(isDisplayed()));

        onView(withId(R.id.button_event)).perform(click());
        onView(withId(R.id.recycler_all_events)).check(matches(isDisplayed()));

        onView(withId(R.id.button_profiles)).perform(click());
        onView(withId(R.id.recycler_all_profiles)).check(matches(isDisplayed()));

        onView(withId(R.id.button_images)).perform(click());
        onView(withId(R.id.recycler_event_posters)).check(matches(isDisplayed()));

        onView(withId(R.id.button_logs)).perform(click());
        onView(withId(R.id.recycler_notification_logs)).check(matches(isDisplayed()));

    }
}
