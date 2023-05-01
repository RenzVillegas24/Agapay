package com.agapay.app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class main_menu : Fragment() {
    private var userUUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userUUID = it.getString("userUUID")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)
        val btnLogout_v = view.findViewById<MaterialButton>(R.id.btnLogout)
        val btnAccount_v = view.findViewById<MaterialButton>(R.id.btnAccount)


        btnLogout_v.setOnClickListener {
            MaterialAlertDialogBuilder(requireActivity())
                .setMessage("Are you sure you want to logout your account?")
                .setPositiveButton("Leave") { dialog, which ->
                    mAuth!!.signOut()
                    findNavController().popBackStack()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setMessage("Do you want to logout the account before leaving?")
                        .setPositiveButton("Logout Account") { dialog, which ->
                            mAuth!!.signOut()
                            requireActivity().finishAndRemoveTask()
                        }
                        .setNeutralButton("Exit Only") { dialog, which ->
                            requireActivity().finishAndRemoveTask()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
            )


        btnAccount_v.setOnClickListener {

            val userId = mAuth!!.currentUser!!.uid
            val bundle = Bundle()

            mDatabaseReference!!
                .orderByKey()
                .equalTo(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (d in dataSnapshot.children){
                            bundle.putString("number", d.child("number").value!!.toString())
                            bundle.putSerializable(
                                "snapshotInf",
                                user(
                                    d.child("number").value!!.toString(),
                                    d.child("username").value!!.toString(),
                                    d.child("password").value!!.toString(),
                                    d.child("firstName").value!!.toString(),
                                    d.child("lastName").value!!.toString(),
                                    d.child("birthdayMillis").value!!.toString().toLong(),
                                    d.child("countryId").value!!.toString(),
                                    d.child("regionId").value!!.toString(),
                                    d.child("provinceId").value!!.toString(),
                                    d.child("muniId").value!!.toString(),
                                    d.child("barangayId").value!!.toString(),
                                    d.child("unit").value!!.toString(),
                                    d.child("numberId").value!!.toString(),
                                    d.child("email").value!!.toString()
                                )
                            )

                            Thread {
                                var url = URL("https://parseapi.back4app.com/classes/Continentscountriescities_Country/${d.child("countryId").value!!}?keys=name,emoji,code,phone")
                                var urlConnection = url.openConnection() as HttpURLConnection
                                urlConnection.setRequestProperty(appId[0], appId[1])
                                urlConnection.setRequestProperty(restKy[0], restKy[1])

                                try {
                                    val item = JSONObject(urlConnection.inputStream.bufferedReader().use { it.readText() })

                                    val objectID = item.getString("objectId")
                                    val name = item.getString("name")
                                    val code = item.getString("code")
                                    val phone = item.getString("phone").split(',')[0]
                                    val emoji = item.getString("emoji")

                                    bundle.putSerializable(
                                        "countryInf",
                                        country_phone(objectID, name, emoji, code, phone)
                                        )
                                    Log.d("", "${bundle.getString("number")} ${bundle.getString("userUUID")} ${bundle.getSerializable<country_phone>("countryInf")}")
                                    findNavController().navigate(R.id.action_main_menu_to_register_screen, bundle)
                                    mDatabaseReference!!.removeEventListener(this);
                                } catch (e: Exception) {
                                    Log.e("MainActivity", e.toString())
                                } finally {
                                    urlConnection.disconnect()
                                }
                            }.start()



                            break
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        throw databaseError.toException()
                    }

                })



        }


        return view
    }

}