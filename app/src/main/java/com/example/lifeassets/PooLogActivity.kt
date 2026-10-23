package com.example.lifeassets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.lifeassets.poolog.PooLogApplication
import com.example.lifeassets.poolog.ui.PooLogApp
import com.example.lifeassets.poolog.ui.PooLogTheme
import com.example.lifeassets.poolog.ui.PooLogViewModel

class PooLogActivity : ComponentActivity() {
    private val viewModel: PooLogViewModel by viewModels {
        PooLogViewModel.factory((application as PooLogApplication).visitRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PooLogTheme {
                PooLogApp(viewModel)
            }
        }
    }
}
