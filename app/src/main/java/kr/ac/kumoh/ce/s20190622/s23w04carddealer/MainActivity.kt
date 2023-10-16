package kr.ac.kumoh.ce.s20190622.s23w04carddealer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import kr.ac.kumoh.ce.s20190622.s23w04carddealer.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var main: ActivityMainBinding
    private lateinit var model: CardDealerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        main = ActivityMainBinding.inflate(layoutInflater)
        setContentView(main.root)

        model = ViewModelProvider(this)[CardDealerViewModel::class.java]
        model.cards.observe(this, {
            val res = IntArray(5)
            for (i in res.indices) {
                res[i] = resources.getIdentifier(
                    //getCardName(model.cards.value!![0]), // null이 될 수가 없다는 확신의 !!
                    getCardName(it[i]),
                    "drawable", packageName
                )
            }
            main.card1.setImageResource(res[0])
        })

        main.btnShuffle.setOnClickListener {
            model.shuffle()
        }

//        val c = Random.nextInt(52)
//        Log.i("Text","$c : ${getCardName(c)}")
//
//        val res = resources.getIdentifier(getCardName(c),"drawble", packageName)
//        main.card1.setImageResource(res)
    }


    private fun getCardName(c: Int) : String {
        var shape = when (c / 13) {
            0 -> "spades"
            1 -> "diamonds"
            2 -> "hearts"
            3 -> "clubs"
            else -> "error"
        }

        val number = when (c % 13){
            0 -> "ace"
            in 1..9 -> (c % 13 + 1).toString()
            10 -> "jack"
            11 -> "queen"
            12 -> "king"
            else -> "error"
        }

        if ( c % 13 in 10..12)
            return "c_${number}_of_${shape}2"
        else
            return "c_${number}_of_${shape}"

    }


}