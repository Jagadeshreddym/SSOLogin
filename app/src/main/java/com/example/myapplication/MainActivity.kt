package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.AuthScreen
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.ui.theme.ComposeGoogleSignInTheme
import com.facebook.AccessToken
import com.facebook.FacebookSdk
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContent {
            ComposeGoogleSignInTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                   // AuthScreen(authViewModel, LocalContext.current)


                    Button(onClick = {

                        if (AccessToken.getCurrentAccessToken() == null) {
                            val profileIntent = Intent(this, FacebookLoginActivity::class.java)
                            startActivityForResult(profileIntent, RESULT_PROFILE_ACTIVITY)
                        } else {
                            val profileIntent = Intent(this, ProfileActivity::class.java)
                            startActivity(profileIntent)
                        }

                    }) {
                        Text(text = "Login with FaceBook")
                    }
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_PROFILE_ACTIVITY ->
                if (resultCode == RESULT_OK) {
                    val profileIntent = Intent(this@MainActivity, ProfileActivity::class.java)
                    startActivity(profileIntent)
                }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val RESULT_PROFILE_ACTIVITY = 1
    }
}

