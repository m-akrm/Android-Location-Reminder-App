package com.udacity.project4.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    private lateinit var menu:Menu
    val firebaseAuth =FirebaseAuth.getInstance()
    private var authStateListener = FirebaseAuth.AuthStateListener {
        Log.i("test",it.currentUser.toString())
        if(::menu.isInitialized) {
            menu.findItem(R.id.logout).title = if (it.currentUser == null) "sign in" else "Log out"
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
           //Todo dont forget to disable the firebase before testing the end to end and remove the tests comments

          //  navigatewithfirebase()
            testnavigationwithoutfirebase()
        }
    }
    //this function is the main oone that the app normally should use
    fun navigatewithfirebase(){
        if(FirebaseAuth.getInstance().currentUser==null){
            Toast.makeText(this.requireContext(),"please sign in first",Toast.LENGTH_LONG).show()
        }
        else{
            navigateToAddReminder()
        }
    }
    fun testnavigationwithoutfirebase(){
        navigateToAddReminder()
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        this.menu=menu
        if(firebaseAuth.currentUser==null){
            menu.findItem(R.id.logout).title="sign in"
        }
        else {
            menu.findItem(R.id.logout).title= "Log out"
        }
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                if(item.title=="Log out"){
                    firebaseAuth.signOut()
                    Toast.makeText(this.requireContext(),"Log out",Toast.LENGTH_LONG).show()
                }
                else{
                   val intent=Intent(requireActivity(),AuthenticationActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }
}
