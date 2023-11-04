package kr.ac.kumoh.ce.s20190622.s23w04carddealer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import kr.ac.kumoh.ce.s20190622.s23w04carddealer.databinding.ActivityMainBinding

data class CardType(var type: Int, var nums: MutableList<Int>)
data class CardNum(var num: Int, var types: MutableList<Int>)
data class CardsState(var code: Int, var cardText: String)

class MainActivity : AppCompatActivity() {
    private lateinit var main: ActivityMainBinding
    private lateinit var model: CardDealerViewModel

    private lateinit var layoutCards: Array<ImageView>
    private var newCards = Array(5){ 0 }
    private var newCardsNum = IntArray(5){ 0 }
    private var firstTime = 0

    companion object {
        val ROYAL_STRAIGHT_FLUSH: CardsState = CardsState(0, "!!!!!Royal Straight Flush!!!!!")
        val BACK_STRAIGHT_FLUSH:  CardsState = CardsState(1, "!!!!Back Straight Flush!!!!")
        val STRAIGHT_FLUSH:       CardsState = CardsState(2, "!!!!Straight Flush!!!!")
        val FOUR_CARD:            CardsState = CardsState(3, "!!!Four Card!!!")
        val FULL_HOUSE:           CardsState = CardsState(4, "!!!Full House!!!")
        val FLUSH:                CardsState = CardsState(5, "!!!Flush!!!")
        val MOUNTAIN:             CardsState = CardsState(6, "!!Mountain!!")
        val BACK_STRAIGHT:        CardsState = CardsState(7, "!!Back Straight!!")
        val STRAIGHT:             CardsState = CardsState(8, "!Straight!")
        val TRIPLE:               CardsState = CardsState(9, "!Triple!")
        val TWO_PAIR:             CardsState = CardsState(10, "Two Pair")
        val ONE_PAIR:             CardsState = CardsState(11, "One Pair")
        val TOP:                  CardsState = CardsState(12, "Top..")
    }

    fun detectCards(): CardsState {
        // 같은 모양, 숫자에 따른 카운터
        var typeC = mutableListOf<CardType>() // 모양 4개
        var numC  = mutableListOf<CardNum>()  // 숫자 13개
        var isflush: Boolean = false          // 플러시인지 판단
        fun makeCounter(c_type: Int, c_number: Int){
            // 타입카운터
            val filteredT  = typeC.filter{it.type == c_type  }
            if (filteredT.isEmpty()) { typeC.add(CardType(c_type, listOf<Int>(c_number).toMutableList())) }
            else filteredT[0]!!.nums.add(c_number)
            // 숫자카운터
            val filteredN = numC.filter {it.num  == c_number}
            if (filteredN.isEmpty()) { numC.add(CardNum(c_number, listOf<Int>(c_type).toMutableList())) }
            else filteredN[0]!!.types.add(c_type)
        }

        // 주어진 5개의 카드에 대해 카운터 생성
        for (i in 1..newCardsNum.count()){
            val card: Int = newCardsNum[i-1]
            makeCounter(card/13, card%13)
        }

        // 문양 종류가 1개인지 확인 - 로스플 / 백스플 / 스플
        if(typeC.size == 1) {
            val del_A_Sorted = typeC[0].nums.filter{it in 1..12}.sorted().toList()
            when(del_A_Sorted.size){
                4 -> { // Detect Royal/Back_Straight_Flush
                    if (del_A_Sorted[0] == 9){ return ROYAL_STRAIGHT_FLUSH }
                    if (del_A_Sorted[3] == 4){ return BACK_STRAIGHT_FLUSH }
                }
                5 -> { // Detect Straight_Flush
                    if(del_A_Sorted[0]+4 == del_A_Sorted[4]){ return STRAIGHT_FLUSH }
                }
            }
            isflush = true // Anyway Flush
        }
        // 숫자 종류가 2개인지 확인 - 포카드 / 풀하우스
        if (numC.size == 2){ // Detect Four_Card, Full_House
            when(numC[0].types.size){ 1, 4 -> return FOUR_CARD; 2, 3 -> return FULL_HOUSE }
        }
        // 플러시
        if (isflush == true){ return FLUSH }
        // 숫자 종류가 5개인지 확인 - 마운틴 / 백스트레이트 / 스트레이트
        if (numC.size == 5){
            val del_A_Sorted = numC.filter{it.num in 1..12}.sortedBy { it.num }.toList()
            when(del_A_Sorted.size){
                4 -> { // Detect Mountain, Back_Straight
                    if(del_A_Sorted[0].num == 9){ return MOUNTAIN }
                    if(del_A_Sorted[3].num == 4){ return BACK_STRAIGHT }
                }
                5 -> { // Detect Straight
                    if(del_A_Sorted[0].num+4 == del_A_Sorted[4].num){ return STRAIGHT }
                }
            }
        }
        // 나머지
        var num_of_pair = 0
        for (item in numC){ // Detect Triple, Two_Pair, One_Pair
            if(item.types.size == 3){ return TRIPLE }
            if(item.types.size == 2){ num_of_pair += 1 }
        }; when(num_of_pair){ 2 -> return TWO_PAIR; 1 -> return ONE_PAIR; else -> return TOP }
    }
    fun printRes (){
        model.states.observe(this,{ main.mainText!!.setText(it.cardText) })
        for (i in 0..4){ layoutCards[i].setImageResource(newCards[i]) }
    }
    fun showDialogs(){
        val dialog = ResultDialog(model)
        dialog.show(supportFragmentManager, "ResultDialog")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        main = ActivityMainBinding.inflate(layoutInflater)
        setContentView(main.root)
        layoutCards = arrayOf(main.card1, main.card2, main.card3, main.card4, main.card5)

        model = ViewModelProvider(this)[CardDealerViewModel::class.java]
        model.cards.observe(this, {
            for (i in newCards.indices) {
                newCards[i] = resources.getIdentifier( getCardName(it[i]),"drawable", packageName )
            }; newCardsNum = it
            // 시작, 회전 시 검사 방지
            if (firstTime != 0) { model.setStates(detectCards()) } else {firstTime = 1}
            printRes()
        })
        
        main.btnShuffle.setOnClickListener { model.shuffle() }
        main.btnShow.setOnClickListener{ showDialogs() }
        main.btnSimulation.setOnClickListener {
            var count = 0
            do { model.shuffle() } while ( ++count < 100000)
            showDialogs()
        }
    }

    override fun onPause() {
        val dialog = supportFragmentManager.findFragmentByTag("ResultDialog") as DialogFragment?
        dialog?.dismiss()
        super.onPause()
    }
    private fun getCardName(c: Int) : String {
        var shape = when (c / 13) {
            0 -> "spades"; 1 -> "diamonds"; 2 -> "hearts"; 3 -> "clubs"
            else -> "error"
        }
        val number = when (c % 13){
            in 1..9 -> (c % 13 + 1).toString()
            0 -> "ace"; 10 -> "jack"; 11 -> "queen"; 12 -> "king"
            else -> "error"
        }

        if (c == -1) { return "c_black_joker" }
        else if ( c % 13 in 10..12) { return "c_${number}_of_${shape}2" }
        else { return "c_${number}_of_${shape}" }
    }
}