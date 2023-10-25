package com.udacity.project4

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matchers
import org.junit.*
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var viewModel: SaveReminderViewModel
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()
        //get the save viewmodel
        viewModel=get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    //Todo check the firebase is disabled before run

    @Test
    fun checkSnackBar_test(){
            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)
            //check that no data exist
            onView(withId(R.id.noDataTextView))
                .check(matches(isDisplayed()))
            //navigate to saveReminderFragment
            onView(withId(R.id.addReminderFAB)).perform(click())
            //click on save button
            onView(withId(R.id.saveReminder)).perform(click())
            //no title have been written so snack bar should appear
            onView(withId(R.id.snackbar_text))
                .check(matches(withText("please enter a title")))
            //wait until the snackbar disappear
            Thread.sleep(4000)
            //set the title
            onView(withId(R.id.reminderTitle)).perform(replaceText("test"))
             //click on save button
            onView(withId(R.id.saveReminder)).perform(click())
             //no discription have been written so snack bar should appear
             onView(withId(R.id.snackbar_text))
                .check(matches(withText("please enter a discritption")))
            activityScenario.close()
    }


    @Test
    fun wholeProgramTest()= runBlocking{
        //intiallize the data
        repository.saveReminder(ReminderDTO("tit","desc","loc",31.1234,54.778))
        //start  activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        //check that data exist
        onView(withId(R.id.noDataTextView)).check { view, noViewFoundException ->
            view?.let {
                Assert.assertEquals(View.GONE, view.visibility)
                return@check
            }
            Error(noViewFoundException)
        }
        //navigate to the new fragment
        onView(withId(R.id.addReminderFAB)).perform(click())
        //set title
        onView(withId(R.id.reminderTitle)).perform(replaceText("test tit"))
        //set description
        onView(withId(R.id.reminderDescription)).perform(replaceText("test des"))
        // set location rather than entering the map fragment
        viewModel.longitude.value=15.1234
        viewModel.latitude.value=15.1234
        viewModel.reminderSelectedLocationStr.value="test name"
        //save reminder
        onView(withId(R.id.saveReminder)).perform(click())
        //check if the reminder appear in the recycler view
        onView(withText("test tit")).check(matches(isDisplayed()))
        activityScenario.close()

    }
    //check if the toast message appear when the user doesnt enter a tilte
    @Test
    fun saveremindervalidation_CheckTheToastMessage() = runBlockingTest {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        var decorView:View?=null
        dataBindingIdlingResource.monitorFragment(scenario)
        scenario.onFragment {
            decorView = it.activity?.window?.decorView
        }
        //invoke saving event
        onView(withId(R.id.saveReminder)).perform(click())

        // checking if the toast message appear when no title is insert
        onView(withText("please enter a title"))
            .inRoot(RootMatchers.withDecorView(Matchers.not(decorView)))
            .check(matches(isDisplayed()))

        onView(withId(R.id.reminderTitle)).perform(replaceText("test tit"))
        Thread.sleep(5000)
        onView(withId(R.id.saveReminder)).perform(click())

        // checking if the toast message appear when no description is insert

        onView(withText("please enter a discritption"))
            .inRoot(RootMatchers.withDecorView(Matchers.not(decorView)))
            .check(matches(isDisplayed()))
    }
}
