package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.junit.Assert.*

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    //initiallize the datasource and view model
    fun init(){
        fakeDataSource=FakeDataSource()
        saveReminderViewModel= SaveReminderViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)
    }
    //check if it return false if no tilte has set
    @Test
    fun validateEnteredData_nulltitle_falseoutput(){
        //when
       val data= ReminderDataItem(null,"desc","loc",31.1234,54.778)

       val output=saveReminderViewModel.validateEnteredData(data)

        assertEquals(false,output)
    }
    //check if it return false if no location has set

    @Test
    fun validateEnteredData_nulllocation_falseoutput(){
        //when
        val data= ReminderDataItem("tit","desc",null,31.1234,54.778)

        val output=saveReminderViewModel.validateEnteredData(data)

        assertEquals(false,output)
    }
    // check if it return true if the data is valid
    @Test
    fun validateEnteredData_validData_trueoutput(){
        //when
        val data= ReminderDataItem("tit","desc","loc",31.1234,54.778)

        val output=saveReminderViewModel.validateEnteredData(data)

        assertEquals(true,output)
    }
    // check if all the data has cleared
    @Test
    fun onClear_checkIfTheLifeDataIsActuallyCleared_nullOutput(){
        //random data
        val data= ReminderDataItem("tit","desc","loc",31.1234,54.778)
        //putting the data
        saveReminderViewModel.reminderTitle.value=data.title
        saveReminderViewModel.reminderDescription.value=data.description
        saveReminderViewModel.latitude.value=data.latitude
        saveReminderViewModel.longitude.value=data.longitude

        //calling the function
        saveReminderViewModel.onClear()

        //assert that the data has cleared
        assertEquals(null,saveReminderViewModel.reminderTitle.getOrAwaitValue())
        assertEquals(null,saveReminderViewModel.reminderDescription.getOrAwaitValue())
        assertEquals(null,saveReminderViewModel.latitude.getOrAwaitValue())
        assertEquals(null,saveReminderViewModel.longitude.getOrAwaitValue())

    }
    // check if the data can be save and retrieve
    @Test
    fun saveReminder_checkingIfSaveIsDone(){
        //when
        val data= ReminderDataItem("tit","desc","loc",31.1234,54.778)
        val dataDTO=ReminderDTO(data.title,
            data.description,
            data.location,
            data.latitude,
            data.longitude,
            data.id)

        saveReminderViewModel.saveReminder(data) //calling the function to save data

        //getting the saved item from the fakedatabase
        var output: Result<ReminderDTO>? =null
        runBlockingTest {
            output=fakeDataSource.getReminder(data.id)
        }
        //checking if the return is not error
        assertNotEquals(Result.Error("Reminder didn't found"),output)
        //checking if the getreminder worked
        assertNotEquals(null,output)
        //checking if there is actually a data in the fakedatabase
        assertNotEquals(null,fakeDataSource.data)
        // checking if the the data is actually stored correctly
        assertEquals(dataDTO,fakeDataSource.data?.get(0))
    }

    @After
    fun stopDown() {
        stopKoin()
    }
}