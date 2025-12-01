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
public class EventDetailsActivityTest extends BaseTestClass{


    //Edit Per Class
    @Rule
    public ActivityScenarioRule<EventDetailsActivity> activityRule = new ActivityScenarioRule<>(EventDetailsActivity.class);


    private Event testerEvent;
    @Before
    public void testEventMaker() {

        testerEvent = new Event();
        testerEvent.setEventId("EventId");
        testerEvent.setName("Event");
        testerEvent.setEventCapacity(50);
        testerEvent.setEntrantLimit(50);
        testerEvent.setEntrants(new ArrayList<>());
        testerEvent.setGeolocationRequired(false);
        testerEvent.setEventTime(new Date(System.currentTimeMillis() + 3600000));
        testerEvent.setRegistrationStart(new Date(System.currentTimeMillis() - 3600000));
        testerEvent.setRegistrationEnd(new Date(System.currentTimeMillis() + 1800000));
        testerEvent.setDescription("Test");
        testerEvent.setGuidelines("Guidelines");
        testerEvent.setOwnerId("OwnerUserId");
        testerEvent.setEventPosterURL(null);
    }

    @Test
    public void testUIElements(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", testerEvent.getEventId());

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)){
            onView(withId(R.id.button_join_waitlist)).check(matches(isDisplayed()));
        }

    }



}
