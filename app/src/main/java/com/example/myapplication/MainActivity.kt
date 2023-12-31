package com.example.myapplication

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.model.LinkedInEmailModel
import com.example.myapplication.model.LinkedInProfileModel
import com.example.myapplication.ui.AuthScreen
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.ui.theme.ComposeGoogleSignInTheme
import com.facebook.AccessToken
import com.facebook.FacebookSdk
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.json.JSONTokener
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    lateinit var linkedinAuthURLFull: String
    lateinit var linkedIndialog: Dialog
    lateinit var linkedinCode: String

    var id = ""
    var firstName = ""
    var lastName = ""
    var email = ""
    var profilePicURL = ""
    var accessToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //FacebookSdk.sdkInitialize(this.getApplicationContext());

        setContent {
            ComposeGoogleSignInTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                   // AuthScreen(authViewModel, LocalContext.current)

                    val state = "linkedin" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

                    linkedinAuthURLFull =
                        LinkedInConstants.AUTHURL + "?response_type=code&client_id=" + LinkedInConstants.CLIENT_ID + "&scope=" + LinkedInConstants.SCOPE + "&state=" + state + "&redirect_uri=" + LinkedInConstants.REDIRECT_URI
                    Log.i("Test","url ----->"+linkedinAuthURLFull)


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

                    Button(onClick = {


                        setupLinkedinWebviewDialog(linkedinAuthURLFull)

                    }) {
                        Text(text = "Login with LinkedIn")

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

    // Show LinkedIn login page in a dialog
    @SuppressLint("SetJavaScriptEnabled")
    fun setupLinkedinWebviewDialog(url: String) {
        linkedIndialog = Dialog(this)
        val webView = WebView(this)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.webViewClient = LinkedInWebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
        linkedIndialog.setContentView(webView)
        linkedIndialog.show()
    }

    // A client to know about WebView navigations
    // For API 21 and above
    @Suppress("OverridingDeprecatedMember")
    inner class LinkedInWebViewClient : WebViewClient() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (request?.url.toString().startsWith(LinkedInConstants.REDIRECT_URI)) {
                Log.i("Test","Code -->"+request?.url.toString())
                handleUrl(request?.url.toString())

                // Close the dialog after getting the authorization code
                if (request?.url.toString().contains("?code=")) {
                    linkedIndialog.dismiss()
                }
                return true
            }
            return false
        }

        // For API 19 and below
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith(LinkedInConstants.REDIRECT_URI)) {
                Log.i("Test","Code url -->"+url)
                handleUrl(url)

                // Close the dialog after getting the authorization code
                if (url.contains("?code=")) {
                    linkedIndialog.dismiss()
                }
                return true
            }
            return false
        }

        // Check webview url for access token code or error
        private fun handleUrl(url: String) {
            val uri = Uri.parse(url)

            if (url.contains("code")) {
                linkedinCode = uri.getQueryParameter("code") ?: ""
                linkedInRequestForAccessToken()
            } else if (url.contains("error")) {
                val error = uri.getQueryParameter("error") ?: ""
                Log.e("Error: ", error)
            }
        }
    }


    fun linkedInRequestForAccessToken() {
        GlobalScope.launch(Dispatchers.Default) {
            val grantType = "authorization_code"
            val postParams =
                "grant_type=" + grantType + "&code=" + linkedinCode + "&redirect_uri=" + LinkedInConstants.REDIRECT_URI + "&client_id=" + LinkedInConstants.CLIENT_ID + "&client_secret=" + LinkedInConstants.CLIENT_SECRET
            val url = URL(LinkedInConstants.TOKENURL)
            val httpsURLConnection = withContext(Dispatchers.IO) {url.openConnection() as HttpsURLConnection}
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.setRequestProperty(
                "Content-Type",
                "application/x-www-form-urlencoded"
            )
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true
            val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
            withContext(Dispatchers.IO) {
                outputStreamWriter.write(postParams)
                outputStreamWriter.flush()
            }

            val responseCode = httpsURLConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = httpsURLConnection.inputStream.bufferedReader()
                    .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response))
                    Log.i("Pretty Printed JSON :", prettyJson)
                    val jsonObject = JSONTokener(response).nextValue() as JSONObject

                    val accessToken = jsonObject.getString("access_token") //The access token
                    Log.d("accessToken is: ", accessToken)

                    val expiresIn = jsonObject.getInt("expires_in") //When the access token expires
                    Log.d("expires in: ", expiresIn.toString())


                    withContext(Dispatchers.Main) {
                        // Get user's id, first name, last name, profile pic url
                        fetchlinkedInUserProfile(accessToken)
                    }

                }
            } else {
                Log.e("HTTPURLCONNECTION_ERROR", responseCode.toString())
            }
        }
    }

    fun fetchlinkedInUserProfile(token: String) {
        GlobalScope.launch(Dispatchers.Default) {
            val tokenURLFull =
                "https://api.linkedin.com/v2/me?projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))&oauth2_access_token=$token"
            val url = URL(tokenURLFull)
            val httpsURLConnection = withContext(Dispatchers.IO) {url.openConnection() as HttpsURLConnection}
            httpsURLConnection.requestMethod = "GET"
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = false
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8
            val linkedInProfileModel =
                Json.decodeFromString(LinkedInProfileModel.serializer(), response)
            withContext(Dispatchers.Main) {
                Log.d("LinkedIn Access Token: ", token)
                accessToken = token

                // LinkedIn Id
                val linkedinId = linkedInProfileModel.id
                Log.d("LinkedIn Id: ", linkedinId)
                id = linkedinId

                // LinkedIn First Name
                val linkedinFirstName = linkedInProfileModel.firstName.localized.enUS
                Log.d("LinkedIn First Name: ", linkedinFirstName)
                firstName = linkedinFirstName

                // LinkedIn Last Name
                val linkedinLastName = linkedInProfileModel.lastName.localized.enUS
                Log.d("LinkedIn Last Name: ", linkedinLastName)
                lastName = linkedinLastName

                // LinkedIn Profile Picture URL
                /*
                     Change row of the 'elements' array to get diffrent size of the profile pic
                     elements[0] = 100x100
                     elements[1] = 200x200
                     elements[2] = 400x400
                     elements[3] = 800x800
                */

                val linkedinProfilePic =
                    linkedInProfileModel.profilePicture.displayImage.elements.get(2)
                        .identifiers.get(0).identifier
                Log.d("LinkedIn Profile URL: ", linkedinProfilePic)
                profilePicURL = linkedinProfilePic

                // Get user's email address
                fetchLinkedInEmailAddress(token)
            }
        }
    }

    fun fetchLinkedInEmailAddress(token: String) {
        val tokenURLFull =
            "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))&oauth2_access_token=$token"

        GlobalScope.launch(Dispatchers.Default) {
            val url = URL(tokenURLFull)
            val httpsURLConnection = withContext(Dispatchers.IO) {url.openConnection() as HttpsURLConnection }
            httpsURLConnection.requestMethod = "GET"
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = false
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8
            val linkedInProfileModel =
                Json.decodeFromString(LinkedInEmailModel.serializer(), response)
            withContext(Dispatchers.Main) {
                // LinkedIn Email
                val linkedinEmail = linkedInProfileModel.elements.get(0).elementHandle.emailAddress
                Log.d("LinkedIn Email: ", linkedinEmail)
                email = linkedinEmail

                openDetailsActivity()
            }
        }
    }

    fun openDetailsActivity() {
        val myIntent = Intent(baseContext, DetailsActivity::class.java)
        myIntent.putExtra("linkedin_id", id)
        myIntent.putExtra("linkedin_first_name", firstName)
        myIntent.putExtra("linkedin_last_name", lastName)
        myIntent.putExtra("linkedin_email", email)
        myIntent.putExtra("linkedin_profile_pic_url", profilePicURL)
        myIntent.putExtra("linkedin_access_token", accessToken)
        startActivity(myIntent)
    }


    companion object {
        private const val RESULT_PROFILE_ACTIVITY = 1
    }
}

