package com.yourcompany.rollergrilltracker.ui.dashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yourcompany.rollergrilltracker.HiltTestRule
import com.yourcompany.rollergrilltracker.R
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.containsString
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class DashboardFragmentTest : HiltTestRule() {

    @Before
    fun setup() {
        // Initialize Hilt
        hiltRule.inject()
    }

    @Test
    fun dashboardDisplaysCorrectly() {
        // Launch the fragment
        launchFragmentInHiltContainer<DashboardFragment> {
            // Verify that the dashboard title is displayed
            onView(withId(R.id.dashboard_title))
                .check(matches(isDisplayed()))
                .check(matches(withText(containsString("Dashboard"))))

            // Verify that the sales summary card is displayed
            onView(withId(R.id.sales_summary_card))
                .check(matches(isDisplayed()))

            // Verify that the waste summary card is displayed
            onView(withId(R.id.waste_summary_card))
                .check(matches(isDisplayed()))

            // Verify that the suggestions card is displayed
            onView(withId(R.id.suggestions_card))
                .check(matches(isDisplayed()))

            // Verify that the time period selector is displayed
            onView(withId(R.id.time_period_selector))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun dashboardShowsCorrectTimePeriods() {
        // Launch the fragment
        launchFragmentInHiltContainer<DashboardFragment> {
            // Verify that the morning time period is displayed
            onView(withText(containsString("Morning")))
                .check(matches(isDisplayed()))

            // Verify that the midday time period is displayed
            onView(withText(containsString("Midday")))
                .check(matches(isDisplayed()))

            // Verify that the afternoon time period is displayed
            onView(withText(containsString("Afternoon")))
                .check(matches(isDisplayed()))

            // Verify that the evening time period is displayed
            onView(withText(containsString("Evening")))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun dashboardNavigationButtonsDisplayCorrectly() {
        // Launch the fragment
        launchFragmentInHiltContainer<DashboardFragment> {
            // Verify that the sales entry button is displayed
            onView(withId(R.id.sales_entry_button))
                .check(matches(isDisplayed()))

            // Verify that the waste tracking button is displayed
            onView(withId(R.id.waste_tracking_button))
                .check(matches(isDisplayed()))

            // Verify that the reports button is displayed
            onView(withId(R.id.reports_button))
                .check(matches(isDisplayed()))

            // Verify that the inventory button is displayed
            onView(withId(R.id.inventory_button))
                .check(matches(isDisplayed()))
        }
    }
}