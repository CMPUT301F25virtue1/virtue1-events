package com.example.linko;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.provider.ContactsContract;

import androidx.test.core.app.ActivityScenario;

import org.junit.Test;

public class ProfileActivityTest extends BaseTestClass{

    @Test
    public void testBasicUI(){
        Intent intent = new Intent(androidx.test.core.app.ApplicationProvider.getApplicationContext(), ProfileActivity.class);
        ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.text_user_name)).check(matches(isDisplayed()));
        onView(withId(R.id.text_user_email)).check(matches(isDisplayed()));
        onView(withId(R.id.text_user_number)).check(matches(isDisplayed()));
        onView(withId(R.id.image_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.button_edit_profile)).check(matches(isDisplayed()));
    }
}
