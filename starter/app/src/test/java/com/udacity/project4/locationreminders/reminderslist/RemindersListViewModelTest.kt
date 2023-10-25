package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    //intiallize the datasource and the viewmodel
    @Before
    fun init(){
        fakeDataSource= FakeDataSource()
        remindersListViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)
    }
        //assert that showNoData livedata is true when there is no data other wise it is false
    @Test
    fun loadReminders_checkWithNoDataExist(){
        //no data exist
        // call the function
        remindersListViewModel.loadReminders()
        //assert
        assertEquals(true,remindersListViewModel.showNoData.getOrAwaitValue())
    }
    //assert that showNoData livedata is true when there is no data other wise it is false
    @Test
    fun loadReminders_checkWithNoDataExistThenExist()= runBlockingTest{
        //start with no data
        remindersListViewModel.loadReminders()
        assertEquals(true,remindersListViewModel.showNoData.getOrAwaitValue())
        val data1= ReminderDTO("tut1","desc1","loc",31.1234,54.778)
        //then save the data
        fakeDataSource.saveReminder(data1)
        //call the function
        remindersListViewModel.loadReminders()
        // assert that the value changed
        assertEquals(false,remindersListViewModel.showNoData.getOrAwaitValue())

    }

    @Test
    fun loadReminders_checkWhenDataExist_alistWithTheSavedDAta(){
        val data1= ReminderDTO("tut1","desc1","loc",31.1234,54.778)
        val data2= ReminderDTO("tut2","desc2","loc",31.1234,54.778)
        val data3= ReminderDTO("tut3","desc3","loc",31.1234,54.778)

        val data1item= ReminderDataItem("tut1","desc1","loc",31.1234,54.778,data1.id)
        val data2item= ReminderDataItem("tut2","desc2","loc",31.1234,54.778,data2.id)
        val data3item= ReminderDataItem("tut3","desc3","loc",31.1234,54.778,data3.id)

        fakeDataSource= FakeDataSource(mutableListOf(data1,data2,data3))
        remindersListViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

        remindersListViewModel.loadReminders()

        assertEquals(false,remindersListViewModel.showNoData.value)
        assertEquals(listOf(data1item,data2item,data3item),remindersListViewModel.remindersList.getOrAwaitValue())

        //intiallizing the viewmodel again
        fakeDataSource.data?.clear()
        remindersListViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

    }
    @Test
    fun loadReminders_checkloadingstate_true() {
        //disable coroutine
        mainCoroutineRule.pauseDispatcher()
        // load reminder should be true and since the coroutine never occur it wont be false
        remindersListViewModel.loadReminders()
        assertEquals(true,remindersListViewModel.showLoading.getOrAwaitValue())

        mainCoroutineRule.resumeDispatcher()
        //starting coroutin the task finished and loadReminder should be true
        assertEquals(false,remindersListViewModel.showLoading.getOrAwaitValue())
    }

    @Test
    fun loadReminders_CheckTheResultWhenThereIsError_returnError() {
        // no data in the database
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
        fakeDataSource.seterror()

        //call the function
        remindersListViewModel.loadReminders()
        //check the snackbar message appear only if error occur
        assertEquals("an error occur",remindersListViewModel.showSnackBar.getOrAwaitValue())

    }

    @After
    fun stopDown() {
        stopKoin()
    }
}