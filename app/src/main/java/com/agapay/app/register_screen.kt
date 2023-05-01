package com.agapay.app

import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_register_screen.txtBxMobileNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date


class register_screen : Fragment() {


    private var number: String? = null
    private var numberId: String? = null
    private var snapshotInf: user? = null
    private var country_phone: country_phone? = null
    private var country: country_r? = null
    private var region: addr_info? = null
    private var province: addr_info? = null
    private var muni: addr_info? = null
    private var barangay: addr_info? = null
    private var selected_birthDateTime: Long? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            country_phone = it.getSerializable<country_phone>("countryInf")
            number = it.getString("number")
            snapshotInf = it.getSerializable<user>("snapshotInf")
        }


        country = country_phone
        numberId = country_phone!!.phone

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Users")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_screen, container, false)

        val txtBxFirstname_v = view.findViewById<TextInputEditText>(R.id.txtBxFirstname)
        val txtBxLastname_v = view.findViewById<TextInputEditText>(R.id.txtBxLastname)
        val txtBxBirthDate_v = view.findViewById<TextInputEditText>(R.id.txtBxBirthDate)
        val selCounty_v = view.findViewById<MaterialAutoCompleteTextView>(R.id.selCounty)
        val selRegion_v = view.findViewById<MaterialAutoCompleteTextView>(R.id.selRegion)
        val selProvince_v = view.findViewById<MaterialAutoCompleteTextView>(R.id.selProvince)
        val selCityMunicipal_v = view.findViewById<MaterialAutoCompleteTextView>(R.id.selCityMunicipal)
        val selBarangay_v = view.findViewById<MaterialAutoCompleteTextView>(R.id.selBarangay)
        val txtBxUnitBlock_v = view.findViewById<TextInputEditText>(R.id.txtBxUnitBlock)
        val txtBxMobileNumber_v = view.findViewById<TextInputEditText>(R.id.txtBxMobileNumber)
        val txtBxEmail_v = view.findViewById<TextInputEditText>(R.id.txtBxEmail)
        val txtBxUsername_v = view.findViewById<TextInputEditText>(R.id.txtBxUsername)
        val txtBxPassword_v = view.findViewById<TextInputEditText>(R.id.txtBxPassword)
        val btnBack_v = view.findViewById<MaterialButton>(R.id.btnBack)
        val btnRegister_v = view.findViewById<MaterialButton>(R.id.btnRegister)
        val bg_loading_v = view.findViewById<RelativeLayout>(R.id.bg_loading)
        val txtHeader_v = view.findViewById<TextView>(R.id.txtHeader)


        if (snapshotInf != null) {

            txtHeader_v.text = "Account Details"
            btnRegister_v.text = "Modify"

            txtBxFirstname_v.setText(snapshotInf!!.firstName)
            txtBxLastname_v.setText(snapshotInf!!.lastName)
            txtBxUnitBlock_v.setText(snapshotInf!!.unit)
            txtBxMobileNumber_v.setText(snapshotInf!!.number)
            txtBxEmail_v.setText(snapshotInf!!.email)
            txtBxUsername_v.setText(snapshotInf!!.username)
            txtBxPassword_v.setText(snapshotInf!!.password)

            selected_birthDateTime = snapshotInf!!.birthdayMillis
            txtBxBirthDate_v.setText(SimpleDateFormat("MMMM d, yyyy").format(Date(selected_birthDateTime!!)))

            selCounty_v.setText((country_phone as country_r).toString())

            csvReader().open(resources.openRawResource(R.raw.ref_region)) {
                var regions = arrayOf<addr_info>()

                for ((i, it)  in  readAllAsSequence().withIndex()) {
                    if (i != 0)
                        regions += addr_info(it[3], it[2])
                    if (it[3].equals(snapshotInf!!.regionId)) {
                        selRegion_v.setText(it[2])
                        region = addr_info(it[3], it[2])
                    }
                }

                val adapter = ArrayAdapter(activity as AppCompatActivity, android.R.layout.simple_list_item_1, regions)
                selRegion_v.setAdapter(adapter)
            }
            csvReader().open(resources.openRawResource(R.raw.ref_province)) {
                var provinces = arrayOf<addr_info>()
                for ((i, it) in  readAllAsSequence().withIndex()) {
                    if (i != 0 && it[3] == snapshotInf!!.regionId)
                        provinces += addr_info(it[4], it[2])
                    if (it[4].equals(snapshotInf!!.provinceId)) {
                        selProvince_v.setText(it[2])
                        province = addr_info(it[4], it[2])
                    }
                }

                val adapter = ArrayAdapter(activity as AppCompatActivity, android.R.layout.simple_list_item_1, provinces)
                selProvince_v.setAdapter(adapter)
            }
            csvReader().open(resources.openRawResource(R.raw.ref_citymun)) {
                var munis = arrayOf<addr_info>()
                for ((i, it) in  readAllAsSequence().withIndex()) {
                    if (i != 0 && it[4] == snapshotInf!!.provinceId)
                        munis += addr_info(it[5], it[2])
                    if (it[5].equals(snapshotInf!!.muniId)) {
                        selCityMunicipal_v.setText(it[2])
                        muni = addr_info(it[5], it[2])
                    }
                }

                val adapter = ArrayAdapter(activity as AppCompatActivity, android.R.layout.simple_list_item_1, munis)
                selCityMunicipal_v.setAdapter(adapter)

            }
            csvReader().open(resources.openRawResource(R.raw.ref_rgy)) {
                var brgy = arrayOf<addr_info>()
                for ((i, it) in  readAllAsSequence().withIndex()) {
                    if (i != 0 && it[5] == snapshotInf!!.muniId)
                        brgy += addr_info(it[1], it[2])
                    if (it[1].equals(snapshotInf!!.barangayId)) {
                        selBarangay_v.setText(it[2])
                        barangay = addr_info(it[1], it[2])
                    }
                }

                val adapter = ArrayAdapter(activity as AppCompatActivity, android.R.layout.simple_list_item_1, brgy)
                selBarangay_v.setAdapter(adapter)
            }

        }
        else{

            txtBxMobileNumber_v.setText(number)
        }

        var countries_ID = arrayOf<country_r>()
        Thread {
            var url = URL("https://parseapi.back4app.com/classes/Continentscountriescities_Country?limit=300&order=name&keys=name,emoji,code,phone")
            var urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.setRequestProperty(appId[0], appId[1])
            urlConnection.setRequestProperty(restKy[0], restKy[1])

            try {
                val data = JSONObject(urlConnection.inputStream.bufferedReader().use { it.readText() }) // Here you have the data that you need
                val results = data.getJSONArray("results")

                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)

                    val objectID = item.getString("objectId")
                    val name = item.getString("name")
                    val emoji = item.getString("emoji")

                    countries_ID += country_r(objectID, name, emoji)
                }

                Handler(Looper.getMainLooper()).post {
                    val adapter = ArrayAdapter(activity as AppCompatActivity, android.R.layout.simple_list_item_1, countries_ID)
                    selCounty_v.setAdapter(adapter)

                    val anim_loading = bg_loading_v.animate().alpha(0f).setDuration(500).setInterpolator(
                        AccelerateInterpolator()
                    )

                    //selCounty_v.setText("${country?.emoji} ${country?.name}")
                    country = country_r("pmnzGTREty", "Philippines", "\uD83C\uDDF5\uD83C\uDDED")
                    selCounty_v.setText(country.toString())
                    csvReader().open(resources.openRawResource(R.raw.ref_region)) {
                        var regions = arrayOf<addr_info>()
                        readAllAsSequence().forEachIndexed { index, strings ->
                            if (index != 0)
                                regions += addr_info(strings[3], strings[2])
                        }

                        val adapter = ArrayAdapter(activity as AppCompatActivity, android.R.layout.simple_list_item_1, regions)
                        selRegion_v.setAdapter(adapter)
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        bg_loading_v.isVisible = false
                        anim_loading.cancel()
                    }, 500)

                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show()
                    Log.e("ERROR:", e.toString())
                }


            } finally {
                urlConnection.disconnect()

            }

        }.start()




        selCounty_v.setOnItemClickListener { adapterView, view, i, l ->
            for(it in countries_ID){

                if (it.toString().substring(5) == selCounty_v.text.substring(5)) {
                    Log.d("HMM", "YES")

                    selRegion_v.text.clear()
                    selBarangay_v.text.clear()
                    selCityMunicipal_v.text.clear()
                    selProvince_v.text.clear()

                    country = it
                    if(it.objectId.equals("pmnzGTREty"))
                    {
                        Log.d("HMM", "OHH")

                        ((selRegion_v.parent as View).parent as TextInputLayout).visibility = View.VISIBLE
                        ((selBarangay_v.parent as View).parent as TextInputLayout).visibility = View.VISIBLE

                        csvReader().open(getResources().openRawResource(R.raw.ref_region)) {
                            var regions = arrayOf<addr_info>()
                            readAllAsSequence().forEachIndexed { index, strings ->
                                if (index != 0)
                                    regions += addr_info(strings[3], strings[2])
                            }

                            val adapter = ArrayAdapter(activity as AppCompatActivity, android.R.layout.simple_list_item_1, regions)
                            selRegion_v.setAdapter(adapter)
                        }
                    } else {

                        ((selRegion_v.parent as View).parent as TextInputLayout).visibility = View.GONE
                        ((selBarangay_v.parent as View).parent as TextInputLayout).visibility = View.GONE
                    }

                    break
                }
            }
        }

        selRegion_v.setOnItemClickListener { adapterView, view, i, l ->
            var item = adapterView.getItemAtPosition(i)
            if (item is addr_info) {
                region = item

                selBarangay_v.text.clear()
                selCityMunicipal_v.text.clear()
                selProvince_v.text.clear()

                csvReader().open(getResources().openRawResource(R.raw.ref_province)) {
                    var provinces = arrayOf<addr_info>()
                    readAllAsSequence().forEachIndexed { index, strings ->
                        if (index != 0 && strings[3] == region!!.objectId)
                            provinces += addr_info(strings[4], strings[2])
                    }

                    val adapter = ArrayAdapter(activity as AppCompatActivity, android.R.layout.simple_list_item_1, provinces)
                    selProvince_v.setAdapter(adapter)
                }
            }
        }

        selProvince_v.setOnItemClickListener { adapterView, view, i, l ->
            var item = adapterView.getItemAtPosition(i)
            if (item is addr_info) {
                province = item

                selBarangay_v.text.clear()
                selCityMunicipal_v.text.clear()

                csvReader().open(getResources().openRawResource(R.raw.ref_citymun)) {
                    var muni = arrayOf<addr_info>()
                    readAllAsSequence().forEachIndexed { index, strings ->
                        if (index != 0 && strings[4] == province!!.objectId)
                            muni += addr_info(strings[5], strings[2])
                    }

                    val adapter = ArrayAdapter(activity as AppCompatActivity, android.R.layout.simple_list_item_1, muni)
                    selCityMunicipal_v.setAdapter(adapter)
                }
            }
        }

        selCityMunicipal_v.setOnItemClickListener { adapterView, view, i, l ->
            var item = adapterView.getItemAtPosition(i)
            if (item is addr_info) {
                muni = item

                selBarangay_v.text.clear()

                csvReader().open(getResources().openRawResource(R.raw.ref_rgy)) {
                    var brgy = arrayOf<addr_info>()
                    readAllAsSequence().forEachIndexed { index, strings ->
                        if (index != 0 && strings[5] == muni!!.objectId)
                            brgy += addr_info(strings[1], strings[2])
                    }

                    val adapter = ArrayAdapter(activity as AppCompatActivity, android.R.layout.simple_list_item_1, brgy)
                    selBarangay_v.setAdapter(adapter)
                }
            }
        }

        selBarangay_v.setOnItemClickListener { adapterView, view, i, l ->
            var item = adapterView.getItemAtPosition(i)
            if (item is addr_info) {
                barangay = item
            }
        }

        btnBack_v.setOnClickListener {
            val msg = "Are you sure you want to cancel account " + if (snapshotInf != null) "modification?" else "registration?"
            MaterialAlertDialogBuilder(requireActivity())
                .setMessage(msg)
                .setPositiveButton("Leave") { dialog, which ->
                    findNavController().popBackStack()
                    if (snapshotInf == null && mAuth!!.currentUser != null)
                        mAuth!!.signOut()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnRegister_v.setOnClickListener {

            txtBxMobileNumber.text = if (txtBxMobileNumber.text.isNullOrBlank()) null else txtBxMobileNumber.text
            txtBxUsername_v.text = if (txtBxUsername_v.text.isNullOrBlank()) null else txtBxUsername_v.text
            txtBxPassword_v.text = if (txtBxPassword_v.text.isNullOrBlank()) null else txtBxPassword_v.text
            txtBxFirstname_v.text = if (txtBxFirstname_v.text.isNullOrBlank()) null else txtBxFirstname_v.text
            txtBxLastname_v.text = if (txtBxLastname_v.text.isNullOrBlank()) null else txtBxLastname_v.text
            txtBxUnitBlock_v.text = if (txtBxUnitBlock_v.text.isNullOrBlank()) null else txtBxUnitBlock_v.text
            txtBxEmail_v.text = if (txtBxEmail_v.text.isNullOrBlank()) null else txtBxEmail_v.text


            if (txtBxMobileNumber_v.text.isNullOrBlank() ||
                txtBxUsername_v.text.isNullOrBlank() ||
                txtBxPassword_v.text.isNullOrBlank() ||
                txtBxFirstname_v.text.isNullOrBlank() ||
                txtBxLastname_v.text.isNullOrBlank() ||
                selected_birthDateTime == null ||
                country == null ||
                province == null ||
                muni == null ||
                txtBxUnitBlock_v.text.isNullOrBlank() ||
                numberId == null ||
                txtBxEmail_v.text.isNullOrBlank() ||
                country!!.objectId != "pmnzGTREty" && (barangay == null || region == null ))
            {
                MaterialAlertDialogBuilder(requireActivity())
                    .setMessage("Input form error!\nCheck your input then try again.")
                    .setPositiveButton("Try Again", null)
                    .setNegativeButton("Cancel") { dialog, which ->
                        findNavController().popBackStack()
                    }
                    .show()

            }
            else {

                // Sign in success, update UI with the signed-in user's information
                val userId = mAuth!!.currentUser!!.uid
                //Verify Email
                //verifyEmail()
                //update user profile information
                val usr = user(
                    txtBxMobileNumber.text.toString(),
                    txtBxUsername_v.text.toString(),
                    txtBxPassword_v.text.toString(),
                    txtBxFirstname_v.text.toString(),
                    txtBxLastname_v.text.toString(),
                    selected_birthDateTime,
                    country?.objectId,
                    region?.objectId,
                    province?.objectId,
                    muni?.objectId,
                    barangay?.objectId,
                    txtBxUnitBlock_v.text.toString(),
                    numberId,
                    txtBxEmail_v.text.toString()
                )


                val msg1 = if (snapshotInf != null) "Modification success!" else "Registration success!\n" + "Welcome ${txtBxFirstname_v.text}"
                val msg2 = if (snapshotInf != null) "Modification failed!" else "Registration failed!"

                mDatabaseReference!!
                    .child(userId)
                    .setValue(usr)
                    .addOnSuccessListener {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(msg1)
                            .setPositiveButton("Continue") { dialog, which ->
                                if (snapshotInf == null)
                                    findNavController().navigate(R.id.action_register_screen_to_main_menu)

                            }
                            .setNegativeButton("Cancel") { dialog, which ->
                                findNavController().popBackStack()
                            }
                            .show()

                        Log.d("signup/update", "success")
                    }
                    .addOnFailureListener {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setMessage("$msg2\nError: ${it.message!!}")
                            .setPositiveButton("Try Again", null)
                            .setNegativeButton("Cancel") { dialog, which ->
                                findNavController().popBackStack()
                            }
                            .show()

                        Log.d("signup/update", "failure" + it.message!!)
                    }


            }


        }

        txtBxBirthDate_v.setOnClickListener {

            val constraint = CalendarConstraints.Builder()
            val dateFormatter = SimpleDateFormat("MMMM d, yyyy")
            val c2 = Calendar.getInstance()

            c2.add(Calendar.YEAR, -13)
            constraint.setEnd(c2.timeInMillis)

            if (txtBxBirthDate_v.text.isNullOrBlank()) {
                constraint.setOpenAt(c2.timeInMillis)
            }
            else {
                constraint.setOpenAt(selected_birthDateTime!!)
            }

            c2.add(Calendar.YEAR, -137)
            constraint.setStart(c2.timeInMillis)


            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(constraint.build())
                .setTitleText("Select your birthday")


            if (!txtBxBirthDate_v.text.isNullOrBlank()) {
                datePicker.setSelection(selected_birthDateTime)
            }

            val datePickerBuilder = datePicker.build()

            datePickerBuilder.show(requireActivity().supportFragmentManager, "DatePicker")

            datePickerBuilder.addOnPositiveButtonClickListener {
                val date = Date(it)
                val cal = Calendar.getInstance()
                val date_formatted = dateFormatter.format(date)
                cal.time = date

                selected_birthDateTime = cal.timeInMillis
                txtBxBirthDate_v.setText(date_formatted)
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setMessage("Are you sure you want to cancel account registration?")
                        .setPositiveButton("Leave") { dialog, which ->
                            findNavController().popBackStack()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    }
                }
            )


        return view
    }

    private fun verifyEmail() {
        val mUser = mAuth!!.currentUser;
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this.context,
                        "Verification email sent to " + mUser.getEmail(),
                        Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("", "sendEmailVerification", task.exception)
                    Toast.makeText(this.context,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

}