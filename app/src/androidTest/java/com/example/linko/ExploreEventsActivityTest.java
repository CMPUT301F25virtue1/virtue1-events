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

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExploreEventsActivityTest extends BaseTestClass{


    //NEED MORE FOR ALL FUNCTIONALITY. JUST ADDING SOME FOR NOW TO MAKE SURE ACTIVITY WORKS
    //Edit per class

    //VISIBILITY ISSUE
//    @Test
//    public void testUIDisplay() {
//        onView(withId(R.id.input_search)).check(matches(isDisplayed()));
//        onView(withId(R.id.button_filter_events)).check(matches(isDisplayed()));
//        onView(withId(R.id.recycler_available_events)).check(matches(isDisplayed()));
//        onView(withId(R.id.button_qr_scanner)).check(matches(isDisplayed()));
//    }

    //Right now were failing due to no null catching in the search bar
    @Test
    public void testTypingSearchbar() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ExploreEventsActivity.class);

        try (ActivityScenario<ExploreEventsActivity> scenario = ActivityScenario.launch(intent)){
            onView(withId(R.id.input_search)).check(matches(isDisplayed()));

            onView(withId(R.id.input_search)).perform(typeText("test"), closeSoftKeyboard());
        }

    }

}
