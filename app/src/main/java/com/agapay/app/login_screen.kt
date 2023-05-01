package com.agapay.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class login_screen : Fragment() {
    private var sel_countryId: country_phone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_screen, container, false)
        val btnRegister_v = view.findViewById<MaterialButton>(R.id.btnRegister)
        val btnLogin_v = view.findViewById<MaterialButton>(R.id.btnLogin)
        val txtBxNumber_v = view.findViewById<TextInputEditText>(R.id.txtBxNumber)
        val txtBxPassword_v = view.findViewById<TextInputEditText>(R.id.txtBxPassword)
        val bg_loading_v = view.findViewById<RelativeLayout>(R.id.bg_loading)
        val selCounty_v = view.findViewById<MaterialAutoCompleteTextView>(R.id.selCounty)

        var countries_ID = arrayOf<country_phone>()


        Thread {
            Log.d("isWorking", "YES")

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
                    val code = item.getString("code")
                    val phone = item.getString("phone").split(',')[0]
                    val emoji = item.getString("emoji")

                    countries_ID += country_phone(objectID, name, emoji, code, phone)

                }


                Handler(Looper.getMainLooper()).post {
                    val adapter = ArrayAdapter(activity as AppCompatActivity, android.R.layout.simple_list_item_1, countries_ID)
                    selCounty_v.setAdapter(adapter)

                    val anim_loading = bg_loading_v.animate().alpha(0f).setDuration(500).setInterpolator(
                        AccelerateInterpolator()
                    )

                    countries_ID.forEachIndexed { i, c ->
                        if (c.objectId == "pmnzGTREty") {
                            sel_countryId = c
                            //Log.d("","Selected [${i}/Expected[${countries_ID.size}];Actual[${selCounty_v.length()}]]: ${c}")

                            selCounty_v.setText(countries_ID[i].toString())
                        }
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
                    sel_countryId = it
                    break
                }
            }


        }

        btnRegister_v.setOnClickListener {
            findNavController().navigate(R.id.action_login_screen_to_register_number)
        }

        btnLogin_v.setOnClickListener {
            val bundle = Bundle()

            bundle.putSerializable("countryInf", sel_countryId)
            bundle.putSerializable("password", txtBxPassword_v.text.toString())
            bundle.putString("number", txtBxNumber_v.text.toString())

            if (txtBxNumber_v.text.isNullOrEmpty() || txtBxPassword_v.text.isNullOrEmpty())
                MaterialAlertDialogBuilder(requireActivity())
                    .setMessage("Input form error!\nCheck your input then try again.")
                    .setPositiveButton("Try Again", null)
                    .show()
            else
                findNavController().navigate(R.id.action_login_screen_to_register_otp, bundle)

        }



        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setMessage("Are you sure you want to leave?")
                        .setPositiveButton("Leave") { dialog, which ->
                            requireActivity().finishAndRemoveTask();
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    }
                }
            )

        return view
    }


}