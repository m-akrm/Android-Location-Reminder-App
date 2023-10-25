package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
        val geofencingClient = LocationServices.getGeofencingClient(this)
        var reminderItem = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem

        geofencingClient.removeGeofences(listOf(reminderItem.id))?.run {
            addOnSuccessListener {
                Toast.makeText(applicationContext, "geofence removed successfully", Toast.LENGTH_SHORT).show()
            }
            addOnFailureListener {
                Toast.makeText(applicationContext, "geofence removed failed ${it.toString()}", Toast.LENGTH_SHORT).show()
            }
        }
        val remindersLocalRepository: ReminderDataSource by inject()

        lifecycleScope.launch {
           withContext(Dispatchers.IO){
               remindersLocalRepository.deleteReminder(reminderItem.id)
           }
        }
        binding.reminderDataItem = reminderItem
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent=Intent(this,RemindersActivity::class.java)
        startActivity(intent)
    }
}
