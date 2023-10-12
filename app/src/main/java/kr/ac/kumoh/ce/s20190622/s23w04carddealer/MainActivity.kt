package kr.ac.kumoh.ce.s20190622.s23w04carddealer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.ac.kumoh.ce.s20190622.s23w04carddealer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var main: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        main = ActivityMainBinding.inflate(layoutInflater)
        setContentView(main.root)

        main.card1.setImageResource(R.drawable.c_2_of_clubs)
    }


}