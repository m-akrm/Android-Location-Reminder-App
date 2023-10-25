package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.Assert.*
import junit.runner.Version.id

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - Insert a task.
        val data= ReminderDTO("tut1","desc1","loc",31.1234,54.778)
        database.reminderDao().saveReminder(data)

        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminderById(data.id)
        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(data.id))
        assertThat(loaded.title, `is`(data.title))
        assertThat(loaded.description, `is`(data.description))
        assertThat(loaded.location, `is`(data.location))
        assertThat(loaded.latitude, `is`(data.latitude))
        assertThat(loaded.longitude, `is`(data.longitude))
    }

    @Test
    fun updateReminderAndGetById()= runBlockingTest {

        // Insert a task into the DAO.
        val data= ReminderDTO("tut1","desc1","loc",31.1234,54.778)
        database.reminderDao().saveReminder(data)
        //update the data
        data.title="new title"
        database.reminderDao().saveReminder(data)

        val loaded = database.reminderDao().getReminderById(data.id)
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.title, `is`(data.title))

    }

    @Test
    fun deleteReminderById()= runBlockingTest{
        val data= ReminderDTO("tut1","desc1","loc",31.1234,54.778)
        database.reminderDao().saveReminder(data)

        database.reminderDao().deleteReminders(data.id)

        val loaded = database.reminderDao().getReminderById(data.id)
        assertEquals(null,loaded)
    }
}