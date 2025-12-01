package com.example.linko;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.runner.RunWith;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MyEventsActivityTest extends BaseTestClass{

    //Edit Per Class
    @Rule
    public ActivityScenarioRule<MyEventsActivity> activityRule = new ActivityScenarioRule<>(MyEventsActivity.class);


    //NEED MORE FOR ALL FUNCTIONALITY. JUST ADDING SOME FOR NOW TO MAKE SURE ACTIVITY WORKS

    @Test
    public void testOrganizedTab() {
        onView(withId(R.id.button_organized)).perform(click());

    }

    @Test
    public void testRegisteredTab() {
        onView(withId(R.id.button_registered)).perform(click());

    }


    @Test
    public void testSystemTab() {
        onView(withId(R.id.button_system)).perform(click());

    }

}
