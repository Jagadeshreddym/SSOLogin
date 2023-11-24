package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton

class FacebookLoginActivity : Activity() {
    private var callbackManager = create()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_facebook_login)
        val loginButton: LoginButton = findViewById(R.id.login_button)
        loginButton.setReadPermissions(listOf(EMAIL))

        // Set the initial permissions to request from the user while logging in
       // loginButton.permissions = listOf(EMAIL, USER_POSTS)
        loginButton.authType = AUTH_TYPE

        // Register a callback to respond to the user
        loginButton.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.i("test","onSuccess")
                    setResult(RESULT_OK)
                    finish()
                }

                override fun onCancel() {
                    Log.i("test","onCancel")
                    setResult(RESULT_CANCELED)
                    finish()
                }

                override fun onError(error: FacebookException) {
                    // Handle exception
                    Log.i("test","error -->"+error.message)
                }
            })
    }

    companion object {
        private const val EMAIL = "email"
        private const val USER_POSTS = "user_posts"
        private const val AUTH_TYPE = "rerequest"
    }
}