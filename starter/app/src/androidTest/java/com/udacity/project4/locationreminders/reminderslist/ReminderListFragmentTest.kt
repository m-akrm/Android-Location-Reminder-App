package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.data.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers
import org.mockito.Mockito.*



@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        fakeDataSource = FakeDataSource()
        remindersListViewModel =RemindersListViewModel(getApplicationContext(), fakeDataSource)

        stopKoin()
        val myModule = module {
            single {
                remindersListViewModel
            }
        }
        startKoin { modules(listOf(myModule))}
    }
    @After
    fun stopDown() {
        stopKoin()
    }


    @Test
    fun checkNoDataTextView_NoDataExist_MustShow(){
        //no data exist in the database
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        //nodatatextview must be shown
        onView(withId(R.id.noDataTextView)).check(matches(isEnabled()))
    }

    @Test
    fun checknoDataTextView_DataExist_Mustgone()  {
        runBlockingTest{
            //save data in the database
            val data= ReminderDTO("tit","desc","loc",31.1234,54.778)
            fakeDataSource.saveReminder(data)

            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            //nodatatextview must disappear
            onView(withId(R.id.noDataTextView)).check { view, noViewFoundException ->
                view?.let {
                    assertEquals(View.GONE, view.visibility)
                    return@check
                }
                Error(noViewFoundException)
            }
        }
    }

    @Test
    fun checkIfTheDataDisplayedCorrectly(){
        runBlockingTest {
            val data = ReminderDTO("tit", "desc", "loc", 31.1234, 54.778)
            fakeDataSource.saveReminder(data)

            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            //check if the recycler view show the data correctly
            onView(withText(data.title)).check(matches(isDisplayed()))
            onView(withText(data.description)).check(matches(isDisplayed()))
            onView(withText(data.location)).check(matches(isDisplayed()))
        }
    }


    @Test
    //this function is called only for testing and it doesnt depend on the firebase
    // never used in the program
    fun navigateAddReminder_CheckingNavigationOccurWithoutFirebase(){

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
            it.testnavigationwithoutfirebase()
        }
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}