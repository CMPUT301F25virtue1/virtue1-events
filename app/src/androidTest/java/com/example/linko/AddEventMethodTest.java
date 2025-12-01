package com.example.linko;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
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
public class AddEventMethodTest extends BaseTestClass{

    @Rule
    public ActivityScenarioRule<AddEventActivity> activityRule = new ActivityScenarioRule<>(AddEventActivity.class);


    @Test
    public void addEventFirestore() throws InterruptedException {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("events").document();
            String eventId = documentReference.getId();
            Event event = new Event();
            event.setName("Test");
            event.setDescription("Testing");

            CountDownLatch latch = new CountDownLatch(1);

            activityRule.getScenario().onActivity(activity -> {
                activity.addEvent(event, documentReference, eventId);
            });

            db.collection("events").document(eventId).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    assertNotNull(snapshot.get("name"));
                } else {
                    throw new AssertionError("Could not find event");
                }
                latch.countDown();

            }).addOnFailureListener(x -> {
                x.printStackTrace();
                throw new AssertionError("Failed: " + x.getMessage());

            });

            try {
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


    }

    @Test
    public void testShowDescription() {
        onView(withId(R.id.click_event_description)).perform(click());
        onView(withId(R.id.text_event_description)).check(matches(isDisplayed()));
    }


}
