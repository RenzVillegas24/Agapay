package com.agapay.app

import android.os.Build
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.getSerializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        getSerializable(key) as? T
    }
}


var appId = arrayOf("X-Parse-Application-Id", "p6hOTY5Wc4IYz0SECAg1s7Q5yXVe6TeeE31rUaro") // This is your app's application id
var restKy = arrayOf("X-Parse-REST-API-Key", "Ud0Mt7QlIhkgCDKSLA5OuIJFJy8iphO42yuJtPDi") // This is your app's REST API key

var mDatabaseReference: DatabaseReference? = null
var mDatabase: FirebaseDatabase? = null
var mAuth: FirebaseAuth? = null

open class country_r(objectId: String, name: String, emoji: String) :
    Serializable {
    var objectId: String = ""
    var name: String = ""
    var emoji: String = ""

    init {
        this.objectId = objectId
        this.name = name
        this.emoji = emoji
    }

    override fun toString(): String {
        return "${emoji} ${name}"
    }
}

class country_phone(objectId: String, emoji: String, name: String, code: String, phone: String) : country_r(objectId, emoji, name) {
    var code: String = ""
    var phone: String = ""

    init {
        this.code = code
        this.phone = phone
    }

    override fun toString(): String {
        return "${emoji} +${phone} ${name}"
    }
}

class addr_info(objectId: String, name: String){
    var objectId: String = ""
    var name: String = ""

    init {
        this.objectId = objectId
        this.name = name
    }

    override fun toString(): String {
        return name
    }
}

@IgnoreExtraProperties
data class user(
    val number: String? = null,
    val username: String? = null,
    val password: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val birthdayMillis: Long? = null,
    val countryId: String? = null,
    val regionId: String? = null,
    val provinceId: String? = null,
    val muniId: String? = null,
    val barangayId: String? = null,
    val unit: String? = null,
    val numberId: String? = null,
    val email: String? = null): Serializable{

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "number" to number,
            "username" to username,
            "password" to password,
            "firstName" to firstName,
            "lastName" to lastName,
            "birthdayMillis" to birthdayMillis,
            "countryId" to countryId,
            "regionId" to regionId,
            "provinceId" to provinceId,
            "muniId" to muniId,
            "barangayId" to barangayId,
            "unit" to unit,
            "numberId" to numberId,
            "email" to email
        )
    }

    @Exclude
    override fun toString(): String {
        return "%s %s %s %s %s %s %s %s %s %s %s %s ".format(
            number, username, password,  firstName, lastName, birthdayMillis, countryId, regionId, provinceId,muniId,
            barangayId, unit, email)
    }


}



