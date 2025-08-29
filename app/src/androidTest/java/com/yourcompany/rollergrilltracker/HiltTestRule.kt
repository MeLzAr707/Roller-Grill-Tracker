package com.egamerica.rollergrilltracker

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule

/**
 * Base class for tests that use Hilt for dependency injection.
 */
@HiltAndroidTest
abstract class HiltTestRule {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    /**
     * Launch a fragment with Hilt injection.
     */
    inline fun <reified T : Fragment> launchFragmentInHiltContainer(
        crossinline action: FragmentScenario<T>.() -> Unit = {}
    ) {
        hiltRule.inject()
        val scenario = FragmentScenario.launchInContainer(T::class.java)
        scenario.action()
    }

    /**
     * Launch an activity with Hilt injection.
     */
    inline fun <reified T : androidx.activity.ComponentActivity> launchActivityInHiltContainer(
        crossinline action: ActivityScenario<T>.() -> Unit = {}
    ) {
        hiltRule.inject()
        val scenario = ActivityScenario.launch(T::class.java)
        scenario.action()
    }
}