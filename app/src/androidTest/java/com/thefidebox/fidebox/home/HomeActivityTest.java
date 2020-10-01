package com.thefidebox.fidebox.home;

import android.app.Activity;
import android.app.Instrumentation;
import android.view.View;
import androidx.test.rule.ActivityTestRule;

import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.profile.ProfileActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;

public class HomeActivityTest {

    @Rule
    public ActivityTestRule<HomeActivity> mActivityTestRule=new ActivityTestRule<>(HomeActivity.class);

    private HomeActivity mActivity=null;

    Instrumentation.ActivityMonitor monitor= getInstrumentation().addMonitor(ProfileActivity.class.getName(),null,false);

    @Before
    public void setUp() throws Exception {
        mActivity=mActivityTestRule.getActivity();

    }

    @Test
    public void testLaunch(){
//        View view=mActivity.findViewById(R.id.nav_view);
//        assertNotNull(view);

        assertNotNull(mActivity.findViewById(R.id.profilebtn));
        onView(withId(R.id.bottom_navigation)).perform(click());

        Activity profileActivity=getInstrumentation().waitForMonitorWithTimeout(monitor,5000);
        assertNotNull(profileActivity);
        profileActivity.finish();
    }

    @After
    public void tearDown() throws Exception {
        mActivity=null;
    }
}