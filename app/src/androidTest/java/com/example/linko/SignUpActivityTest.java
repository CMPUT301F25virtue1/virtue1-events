package com.example.linko;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;

import org.junit.Test;

public class SignUpActivityTest extends BaseTestClass{

    @Test
    public void testBasicUI(){
        Intent intent = new Intent(androidx.test.core.app.ApplicationProvider.getApplicationContext(), SignUpActivity.class);
        ActivityScenario<SignUpActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.image_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.input_firstname)).check(matches(isDisplayed()));
        onView(withId(R.id.input_lastname)).check(matches(isDisplayed()));
        onView(withId(R.id.input_email)).check(matches(isDisplayed()));
        onView(withId(R.id.input_phonenumber)).check(matches(isDisplayed()));
        onView(withId(R.id.button_signup)).check(matches(isDisplayed()));
    }
}
