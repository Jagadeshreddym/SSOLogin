package com.example.myapplication.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.example.myapplication.FacebookLoginActivity
import com.example.myapplication.ProfileActivity
import com.example.myapplication.R
import com.example.myapplication.utils.AuthResultContract
import com.example.myapplication.viewmodel.AuthViewModel
import com.facebook.AccessToken.Companion.getCurrentAccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    context:Context
) {
    val coroutineScope = rememberCoroutineScope()
    var text by remember { mutableStateOf<String?>(null) }
    val user by remember(authViewModel) { authViewModel.user }.collectAsState()
    val signInRequestCode = 1

    val authResultLauncher =
        rememberLauncherForActivityResult(contract = AuthResultContract()) { task ->
            try {
                val account = task?.getResult(ApiException::class.java)
                val acct = GoogleSignIn.getLastSignedInAccount(context)
                if(acct == null)
                {
                    text = "Google sign in failed"
                }
                if (account == null) {
                    text = "Google sign in failed"
                } else {
                    coroutineScope.launch {
                        authViewModel.signIn(
                            email = account.email,
                            displayName = account.displayName,
                        )
                    }
                }
            } catch (e: ApiException) {
                text = "Google sign in failed"
            }
        }

    AuthView(
        errorText = text,
        onClick = {
            text = null
            authResultLauncher.launch(signInRequestCode)
        }
    )

    user?.let {
        HomeScreen(user = it)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthView(
    errorText: String?,
    onClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    Scaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignInButton(
                text = "Sign in with Google",
                loadingText = "Signing in...",
                isLoading = isLoading,
                icon = painterResource(id = R.drawable.ic_launcher_background),
                onClick = {
                    isLoading = true
                    onClick()
                }
            )

            errorText?.let {
                isLoading = false
                Spacer(modifier = Modifier.height(30.dp))
                Text(text = it)
            }



        }
    }


}

