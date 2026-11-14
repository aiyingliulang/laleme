package com.example.lifeassets.poolog

import android.app.Application
import com.example.lifeassets.poolog.data.PooLogDatabase
import com.example.lifeassets.poolog.data.ToiletVisitRepository

class PooLogApplication : Application() {
    val visitRepository: ToiletVisitRepository by lazy {
        ToiletVisitRepository(PooLogDatabase.create(this).toiletVisitDao())
    }
}
