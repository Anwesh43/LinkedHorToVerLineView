package com.anwesh.uiprojects.linkedhortoverlineview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.hortoverlineview.HorToVerLineView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HorToVerLineView.create(this)
    }
}
