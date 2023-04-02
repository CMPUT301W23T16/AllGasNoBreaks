package com.example.allgasnobrakes;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.allgasnobrakes.models.PlayerProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class QRListFragmentTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
        Thread.sleep(2000);
        try {
            onView(withId(R.id.username_edittext)).perform(typeText("Test User"));
            onView(withId(R.id.email_edittext)).perform(typeText("user@test.com"));
            onView(withId(R.id.password_edittext)).perform(typeText("test"));
            Espresso.closeSoftKeyboard();
            onView(withId(R.id.registerbutton)).perform(click());

        } catch (NoMatchingViewException e) {
            onView(withId(R.id.roll_out_button)).perform(click());
        }
    }

    @Test
    public void testAddQrUiChanges() {
//        MainActivity.getCurrentUser().addQR();
    }
}
