package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource( var data: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {


    var forced_error = false

    fun seterror() {
        forced_error = true
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (forced_error) {
            forced_error = false
            return Result.Error("an error occur")
        }
        return Result.Success(data)

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        data.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (forced_error) {
            forced_error = false
            return Result.Error("an error occur")
        }
        data.find { it.id == id }?.let {
            return Result.Success(it)
        }
        return Result.Error("Reminder didn't found")
    }

    override suspend fun deleteAllReminders() {
        data.clear()
    }

    override suspend fun deleteReminder(reminderId: String) {
        data.find { it.id == reminderId }?.let {
            data.remove(it)
        }
    }
}