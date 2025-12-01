

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

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import android.util.Log;

import com.example.linko.AddEventActivity;
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
/*
To do tests, you will NEED to know lambda expressions for some parts as you might have to use call backs
Here are some youtube videos that helped me understand it.
https://www.youtube.com/watch?v=lIXs4Y8sJCk
https://www.youtube.com/watch?v=tj5sLSFjVj4

Also refer to the class lab for tests for extra info and you should look at that before starting anything
 */
/**
 * To start, This section and everything bellow up until the line (------) is for setting up the emulator
 * If you are touching the database at all this is a MUST DO NOT FORGET IT
 * All the imports above are needed as well.
 * The emulator should work fine but if it doesn't for some reason, youtube is your best friend.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExampleTestDeleteLater {
    // Firestore emulator port
    static int firestorePort = 8080; // CHANGE IF NEEDED - Depends on your emulator config
    // Specific address for emulated device to access our localHost
    static String androidLocalhost = "10.0.2.2";

    @Rule
    public ActivityScenarioRule<AddEventActivity> activityRule = new ActivityScenarioRule<>(AddEventActivity.class);

    @BeforeClass
    public static void setup() {
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, 4400);
    }

    @After
    public void tearDown() {
        String projectId = "linko-234f5"; // CHANGE TO YOUR PROJECT ID - Can be found under Project Settings in the Firebase Console
        URL url = null;
        try {
            url = new URL("http://127.0.0.1:" + firestorePort + "/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /** -------------------------------------------------------------------------------------------------------------------------------------------------------
     *
     * Alright, now if you added the emulator, the first order of business is to make sure its actually working right.
     * To do this, we simply add any form of basic check.
     * I like the run the one bellow but its entirely up to you what you run, as long as it works and checks what
     * it actually needs to check.
     */


    /*
    This test adds event to firestore and checks if its there.
    When we test firestore, make sure to add a "throws (insert needed exception)" in the method declaration
    IMPORTANT: when we test Firestore, our items were adding are NOT getting added immediately.
        To fix this, we use whats called a CountDownLatch. This allows us to set a timed delay to allow
        Firestore to properly add the item before we check if its there or try to use it.
     */
    @Test
    public void addEventFirestore() throws InterruptedException {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("events").document();
            String eventId = documentReference.getId();
            Event event = new Event();
            event.setName("Test");
            event.setDescription("Testing");

            //The CountDownLatch I mentioned
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

    /**
     * Alright, now we have the emulator set up, time to run some tests.
     * The above test would in fact be an example of a database test so I'll just leave that
     * with some comments as an example of that.
     *
     * If we're checking UI elements, heres a simple example test:
     */
    @Test
    public void testTypingName() {
        onView(withId(R.id.text_event_name)).perform(typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.text_event_name)).check(matches(withText("test")));
    }

    @Test   //As you can see, we always start with @Test with an uppercase T
    public void testUIDisplay() {   //Public void method

        //These all check to see if the UI element displayed in the view is the same one we have in the xml
        //This will check to see if unintended UI elements are present, or if they aren't displaying at all.
        onView(withId(R.id.input_firstname)).check(matches(isDisplayed()));
        onView(withId(R.id.input_lastname)).check(matches(isDisplayed()));
        onView(withId(R.id.input_email)).check(matches(isDisplayed()));
        onView(withId(R.id.input_phonenumber)).check(matches(isDisplayed()));
        onView(withId(R.id.button_save_changes)).check(matches(isDisplayed()));
        onView(withId(R.id.button_remove_phonenumber)).check(matches(isDisplayed()));
        onView(withId(R.id.button_back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.image_profile)).check(matches(isDisplayed()));
    }
    /**
     * IMPORTANT NOTE: when checking UI elements as shown above, it ONLY works like that if you are on the main page of the view
     * For example, if we are in the event tab, we can only check Registered events as we did above, to look at Organized events
     * we need to first actually move to the organized tab.
     *
     * onView(withId(R.id.button_organized)).perform(click());
     * Above is an example of how we perform a click action.
     *
     * Basically the idea with these tests is that we go through these basic steps:
     *  1. Figure out EXACTLY what were testing. i.e. search bar functionality. Keep it simple and don't worry about niche corner cases
     *  2. Figure out what would first need to be there to test it as a user. i.e. does there need to be an already existing event?
     *  3. Figure out what inputs the user would need to make to test what we want to test
     *  4. Check if the expected behaviour happened
     *      In the end, all were really doing is checking the app as we normally would, except instead of doing it by hand,
     *      were turning our actions into code so it can be automatically ran everytime we push the code.
     *
     */


    //Example of pre-making an event
    private Event testerEvent;
    @Before //This means the code bellow is run before all the other tests and successfully finishes before the other tests.
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

    /**
     * Finally, to test the code, lick right click on the class and click run.
     * IMPORTANT: Make sure to run ALL the tests after your test passes. Sometimes creating multiple instances of the emulator breaks the tests.
     *  I honestly forget the fix but I remember it being pretty simple.
     *  The official java documentation on tests is honestly a life saver and I highly recommend doing a quick glance at that.
     *
     *  Finally, if there are a million things to click or you just want to test what happens when the screen is clicked a lot (shouldn't need to worry about this)
     *      search up what a monkey clicker is, that will make life a lot easier.
     *      You don't need to test EVERYTHING just make sure to test the important stuff so we know nothing is broken.
     *      I think one of the tests doesn't work rn so if you run all the tests and it fails, check WHICH test failed as it might not be yours.
     *      Tags like @Before, @After, @LargeTest, @Rule, and so on will be your best friend so look into them
     *
     *      This should have the answers to most if not all of your questions.
     *      https://docs.junit.org/current/user-guide/
     */
}




