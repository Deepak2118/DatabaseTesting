package anand.deepak.databasetesting

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import android.view.Menu
import android.view.MenuItem
import anand.deepak.databasetesting.databinding.ActivityMainBinding
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contact_details.*
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"
private const val REQUEST_CODE_READ_CONTACTS = 1

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    //private var readGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val hasReadContactPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        Log.d(TAG, "Oncreate: checkSelfPermission returned $hasReadContactPermission")

        if (hasReadContactPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Oncreate: permission granted")
            //readGranted = true
        } else {
            Log.d(TAG, "Oncreate: permission denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_READ_CONTACTS)
        }

        val database = baseContext.openOrCreateDatabase("TestDB", Context.MODE_PRIVATE, null)
        database.execSQL("DROP TABLE IF EXISTS contacts")
        var sql = "CREATE TABLE IF NOT EXISTS contacts(_id INTEGER PRIMARY KEY NOT NULL, name TEXT, phone INTEGER, email TEXT)"

        Log.d(TAG, "onCreate: Creating table with SQL $sql")
        database.execSQL(sql)

        sql = "INSERT INTO contacts(name, phone, email) VALUES('Deepak',7299457601,'deepak@gmail.com')"

        Log.d(TAG, "onCreate: Inserting a record in contacts table with SQL $sql")
        database.execSQL(sql)

        val values = ContentValues().apply {
            put("name", "Leonard")
            put("phone", 9644841243)
            put("email", "leonard@gmail.com")
        }

        val generatedId = database.insert(
            "contacts",
            null,
            values
        )
        Log.d(TAG, "onCreate: Inserting a record in contacts table with id $generatedId")

        val cursor = database.rawQuery("SELECT * from contacts", null)
        cursor.use {
            while (it.moveToNext()) {
                with(it) {
                    val id = getInt(0)
                    val name = getString(1)
                    val contact = getLong(2)
                    val email = getString(3)
                    val result = "ID: $id. Name: $name contact: $contact email: $email"
                    Log.d(TAG, "onCreate: fetching data from table : $result")
                }
            }
        }

        database.close()

        binding.fab.setOnClickListener { view ->

            Log.d(TAG, "fab: Onclick Listener starts")

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

                val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

                val contacts = ArrayList<String>()

                cursor?.use {
                    while (it.moveToNext()) {
                        contacts.add(it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)))
                    }
                }

                contacts[0] = "Leonard Hofstader"

                val adapter = ArrayAdapter<String>(this, R.layout.contact_details, R.id.contacts_name, contacts)
                Log.d(TAG, "${adapter.toString()} $contacts[0]")
                contact_list.adapter = adapter
            } else {
                Snackbar.make(view, "Please provide contacts permission to the application", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant Access") {
                        Log.d(TAG, "snackBar onClick: started")
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                            Log.d(TAG, "snackBar onClick: permission requested")
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS),
                                    REQUEST_CODE_READ_CONTACTS)
                        } else {
                            Log.d(TAG, "snackBar onClick: launching settings")
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", this.packageName, null)
                            intent.data = uri
                            Log.d(TAG, "snackBar onClick: Uri: $uri")
                            this.startActivity(intent)
                        }
                        Log.d(TAG, "snackBar onClick: ended")
                    }.show()
            }
        }
        Log.d(TAG, "Oncreate ends")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult starts with requestCode $requestCode")
        when (requestCode) {
            REQUEST_CODE_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission granted")
                    //true
                } else {
                    Log.d(TAG, "Permission denied")
                    //false
                }
                //fab.isEnabled = readGranted
            }
        }
        Log.d(TAG, "onRequestPermissionsResult ended")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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