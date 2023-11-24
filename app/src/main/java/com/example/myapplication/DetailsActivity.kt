package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val linkedinId = intent.getStringExtra("linkedin_id")
        val linkedinFirstName = intent.getStringExtra("linkedin_first_name")
        val linkedinLastName = intent.getStringExtra("linkedin_last_name")
        val linkedinEmail = intent.getStringExtra("linkedin_email")
        val linkedinProfilePicURL = intent.getStringExtra("linkedin_profile_pic_url")
        val linkedinAccessToken = intent.getStringExtra("linkedin_access_token")

        val linkedin_id_textview = findViewById<TextView>(R.id.linkedin_id_textview)
        linkedin_id_textview.text = linkedinId
        findViewById<TextView>(R.id.linkedin_first_name_textview).text = linkedinFirstName
        findViewById<TextView>(R.id.linkedin_last_name_textview).text = linkedinLastName
                findViewById<TextView>(R.id.linkedin_email_textview).text = linkedinEmail
        if (linkedinProfilePicURL == "") {
            findViewById<TextView>(R.id.linkedin_profile_pic_url_textview).text = "Not Exist"
        } else {
            findViewById<TextView>(R.id.linkedin_profile_pic_url_textview).text = linkedinProfilePicURL
        }
        findViewById<TextView>(R.id.linkedin_access_token_textview).text = linkedinAccessToken
    }
}
