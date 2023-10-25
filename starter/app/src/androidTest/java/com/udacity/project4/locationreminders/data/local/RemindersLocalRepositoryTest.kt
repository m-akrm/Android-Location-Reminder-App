package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.junit.Assert.*
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase
    //intiallize database and repositry
    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(getApplicationContext(),RemindersDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        remindersLocalRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }
    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    //check if database can save and retrieve data
    fun saveReminder_getReminder() = runBlocking {
        //save the data
        val data= ReminderDTO("tit","desc","loc",31.1234,54.778)
        remindersLocalRepository.saveReminder(data)
        //get the data
        val result = remindersLocalRepository.getReminder(data.id)
        result as Result.Success

        //check data id
        assertThat(result.data.id, `is`(data.id))
        //check data title
        assertThat(result.data.title, `is`(data.title))
        //check data description
        assertThat(result.data.description, `is`(data.description))
        // check data latitude
        assertThat(result.data.latitude, `is`(data.latitude))
        // check data longitude
        assertThat(result.data.longitude, `is`(data.longitude))
        // check data location
        assertThat(result.data.location, `is`(data.location))
    }


    @Test
    fun updateReminderAndGetById()= runBlockingTest {

        // Insert a task into the DAO.
        val data= ReminderDTO("tut1","desc1","loc",31.1234,54.778)
        remindersLocalRepository.saveReminder(data)

        //update the data
        data.title="new title"
        remindersLocalRepository.saveReminder(data)
        //check if the data have been updated in database
        val loaded = remindersLocalRepository.getReminder(data.id)
        loaded as Result.Success
        assertThat(loaded, CoreMatchers.notNullValue())
        assertThat(loaded.data.title, `is`(data.title))
    }

    @Test
    fun deleteReminderById()= runBlockingTest{
        val data= ReminderDTO("tut1","desc1","loc",31.1234,54.778)
        remindersLocalRepository.saveReminder(data)
        remindersLocalRepository.deleteReminder(data.id)

        val loaded = remindersLocalRepository.getReminder(data.id)

        assertEquals(loaded is Result.Error,true)
        loaded as Result.Error

        assertEquals(loaded.message,"Reminder not found!")
    }
}