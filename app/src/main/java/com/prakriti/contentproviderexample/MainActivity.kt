package com.prakriti.contentproviderexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.prakriti.contentproviderexample.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"

private const val READ_CONTACTS_REQUEST_CODE = 1 // private top level constant are private to this file

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
//    private var readGranted = false // avoid storing state in a variable, complicates code

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // ContextCompat checks API version -> before M, permission is auto-granted
//        val hasReadContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
//        Log.d(TAG, "onCreate: checkSelfPermission -> $hasReadContactsPermission") // 0 and -1

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: permission denied, requesting")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), READ_CONTACTS_REQUEST_CODE)
        }

        binding.fab.setOnClickListener {
            Log.i(TAG, "onCreate: fab clicked")
//            if (readGranted) {
            if(ContextCompat.checkSelfPermission(it.context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                // contentResolver holds ref to all content providers on device
                val cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    projection,
                    null,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

                val contactsList = ArrayList<String>()
                cursor?.use {
                    while (it.moveToNext()) {
                        contactsList.add(it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)))
                    }
                }
                val adapter = ArrayAdapter<String>(this, R.layout.contact_detail, R.id.contact_name, contactsList)
                contact_list.adapter = adapter
            } else {
                Snackbar.make(it, "App doesn't have required permission", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Allow Access", {
                        Log.d(TAG, "onCreate: snackbar clicked")
                        // IMP: only use this if you dont already have permission
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                            // if true - denied once, request permissions again
                            Log.d(TAG, "onCreate: snackbar: calling requestPermissions")
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), READ_CONTACTS_REQUEST_CODE)
                        } else {
                            // if false, user has ticked dont ask again (or device policies issue). open settings for app
                            Log.d(TAG, "onCreate: snackbar - launching settings")
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS // app settings
                            val uri = Uri.fromParts("package", this.packageName, null) // package schema needed
                            // fromParts() is used to build up the URI that is needed by this intent
                            Log.d(TAG, "onCreate: snackbar uri: $uri")
                            intent.data = uri
                            this.startActivity(intent)
                        }
                    }).show()
            }
        }
    }

/*    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult called")
        when (requestCode) {
            READ_CONTACTS_REQUEST_CODE -> {
//                readGranted = if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // granted, start task here
                    Log.d(TAG, "onRequestPermissionsResult: granted")
                } else {
                    // denied, disable functionality dependent on this permission
                    Log.d(TAG, "onRequestPermissionsResult: denied")
                }
            }
//                binding.fab.isEnabled = readGranted // avoid this and show message to user instead
        }
    }
*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}