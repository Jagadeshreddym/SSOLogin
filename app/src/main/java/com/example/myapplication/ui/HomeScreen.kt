package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import  com.example.myapplication.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(user: User) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hello, ${user.displayName}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 30.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = user.email)
        }
    }
}

