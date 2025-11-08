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
public class MyEventsActivityTest {
    // Firestore emulator port
    static int firestorePort = 8080; // CHANGE IF NEEDED - Depends on your emulator config
    // Specific address for emulated device to access our localHost
    static String androidLocalhost = "10.0.2.2";

    //Edit Per Class
    @Rule
    public ActivityScenarioRule<MyEventsActivity> activityRule = new ActivityScenarioRule<>(MyEventsActivity.class);

    @BeforeClass
    public static void setup(){
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

    //NEED MORE FOR ALL FUNCTIONALITY. JUST ADDING SOME FOR NOW TO MAKE SURE ACTIVITY WORKS
    //Edit per class

    //NEED TO FIGURE OUT HOW TO WORK WITH HIDDEN UI
//    @Test
//    public void testUIDisplay() {
//        onView(withId(R.id.button_registered)).check(matches(isDisplayed()));
//
//        onView(withId(R.id.button_organized)).perform(click());
//        onView(withId(R.id.recycler_organized_events)).check(matches(isDisplayed()));
//        onView(withId(R.id.text_no_event_organized)).check(matches(isDisplayed()));
//
//        onView(withId(R.id.button_organize_event)).check(matches(isDisplayed()));
//        onView(withId(R.id.recycler_registered_events)).check(matches(isDisplayed()));
//        onView(withId(R.id.recycler_organized_events)).check(matches(isDisplayed()));
//    }

    @Test
    public void testOrganizedTab() {
        onView(withId(R.id.button_organized)).perform(click());

    }

    @Test
    public void testRegisteredTab() {
        onView(withId(R.id.button_registered)).perform(click());

    }

    //NEED TO FIGURE OUT HOW TO TEST WHEN ANIMATIONS ARE INVOLVED
//    @Test
//    public void testOrganizeEventButton() {
//        onView(withId(R.id.button_organize_event)).perform(click());
//
//    }

}
