package com.agapay.app

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.values
import java.io.Serializable
import java.util.concurrent.TimeUnit



class register_otp : Fragment() {
    private var countryInf: country_phone? = null
    private var number: String? = null
    private var password: String? = null
    private var bg_loading_v: RelativeLayout? = null

    private var codeByFirebase: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            countryInf = it.getSerializable<country_phone>("countryInf")
            number = it.getString("number")
            password = it.getString("password")
        }

        mAuth?.setLanguageCode(countryInf!!.code)

        sendVerificationCodeToUser("+${countryInf!!.phone}${number!!}")

        if (password != null){

        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_otp, container, false)

        view.findViewById<TextView>(R.id.txtSub).text = "We've sent an SMS with an OTP from your phone number\n+${countryInf!!.phone}${number}."
        view.findViewById<MaterialButton>(R.id.btnVerify)?.setOnClickListener {
            val code = view.findViewById<TextInputEditText>(R.id.txtBxOTP).text.toString()
            if (code.isNotEmpty()) {
                verifyCode(code)
            }

        }
        bg_loading_v = view.findViewById(R.id.bg_loading)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setMessage("Are you sure you want to dismiss OTP confirmation?")
                        .setPositiveButton("Leave") { dialog, which ->
                            findNavController().popBackStack()

                            if (password == null && mAuth!!.currentUser != null)
                                mAuth!!.signOut()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        )

        return view
    }

    private fun sendVerificationCodeToUser(phoneNo: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth!!)
            .setPhoneNumber(phoneNo) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity((activity as AppCompatActivity)) // Activity (for callback binding)
            .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Toast.makeText(this@register_otp.activity, "Code has been sent...", Toast.LENGTH_SHORT).show()
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setMessage(e.message!!.split(".")[0] + ".")
                        .setPositiveButton("Retry") { dialog, which ->
                            findNavController().navigate(R.id.action_register_otp_to_register_number)
                        }
                        .show()
                }

                override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
                    super.onCodeSent(verificationId, token)
                    codeByFirebase = verificationId

                    bg_loading_v!!.animate().alpha(0f).setDuration(500).setInterpolator(
                        AccelerateInterpolator()
                    )
                    Toast.makeText(this@register_otp.activity, "Code sent...", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(codeByFirebase!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(activity as AppCompatActivity) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@register_otp.activity,
                        "Verification completed!",
                        Toast.LENGTH_SHORT
                    ).show()


                    val bundle = Bundle()
                    bundle.putSerializable("countryInf", countryInf)
                    bundle.putString("number", number)

                    if (password == null)
                        findNavController().navigate(R.id.action_register_otp_to_register_screen, bundle)
                    else {
                        val userId = mAuth!!.currentUser!!.uid

                        var vvv = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (d in dataSnapshot.children){
                                    Log.d("LIST SIZE", d.child("password").value!!.equals(password).toString())

                                    if (d.child("password").value!!.equals(password)){
                                        MaterialAlertDialogBuilder(requireActivity())
                                            .setMessage("Registration success.\nWelcome ${d.child("firstName").value}!")
                                            .setPositiveButton("Continue") { dialog, which ->
                                                findNavController().navigate(R.id.action_register_otp_to_main_menu, bundle)
                                                mDatabaseReference!!.removeEventListener(this);
                                            }
                                            .show()
                                    } else {
                                        MaterialAlertDialogBuilder(requireActivity())
                                            .setMessage("Password unmathched!\nWould you like to register an account instead?")
                                            .setPositiveButton("Register Account") { dialog, which ->
                                                findNavController().navigate(R.id.action_register_otp_to_register_number)
                                                mDatabaseReference!!.removeEventListener(this);
                                            }
                                            .setNegativeButton("Cancel", null)
                                            .show()
                                    }

                                    break
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                throw databaseError.toException()
                            }

                        }

                        mDatabaseReference!!
                            .orderByKey()
                            .equalTo(userId)
                            .addValueEventListener(vvv)
                    }

                } else {
                    Toast.makeText(
                        this@register_otp.activity,
                        "Verification not completed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}