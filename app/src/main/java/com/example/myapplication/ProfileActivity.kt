package com.example.myapplication

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.callbacks.GetUserCallback
import com.example.myapplication.callbacks.GetUserCallback.IGetUserResponse
import com.example.myapplication.entities.User
import com.example.myapplication.requests.UserRequest
class ProfileActivity : Activity(), IGetUserResponse {
    private lateinit var mProfilePhotoView: ImageView
    private lateinit var mName: TextView
    private lateinit var mId: TextView
    private lateinit var mEmail: TextView
    private lateinit var mPermissions: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)
        mProfilePhotoView = findViewById(R.id.profile_photo)
        mName = findViewById(R.id.name)
        mId = findViewById(R.id.id)
        mEmail = findViewById(R.id.email)
        mPermissions = findViewById(R.id.permissions)
    }

    override fun onResume() {
        super.onResume()
        UserRequest.makeUserRequest(GetUserCallback(this@ProfileActivity).callback)
    }

    override fun onCompleted(user: User) {
        mProfilePhotoView.setImageURI(user.picture)
        mName.text = user.name
        mId.text = user.id
        if (user.email == null) {
            mEmail.setText(R.string.no_email_perm)
            mEmail.setTextColor(Color.RED)
        } else {
            mEmail.text = user.email
            mEmail.setTextColor(Color.BLACK)
        }
        mPermissions.text = user.permissions
    }
}