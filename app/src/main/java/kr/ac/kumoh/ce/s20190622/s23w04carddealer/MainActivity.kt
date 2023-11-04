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

    private var tempstate = 0

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
        var typeC = mutableListOf<CardType>() // 타입 4개에 대한 카운터
        var numC  = mutableListOf<CardNum>()  // 숫자 13개에 대한 카운터
        var isflush = false // 플러시인지 판단

        fun makeCounter(c_type: Int, c_number: Int){
            // 카운터값 존재x->카운터에 추가
            // 카운터값 존재o->해당 카운터 값(nums, types)에 추가
            val filteredT = typeC.filter{ it.type == c_type }
            if (filteredT.isEmpty()) { typeC.add(CardType(c_type, listOf<Int>(c_number).toMutableList())) }
            else filteredT[0]!!.nums.add(c_number)

            val filteredN = numC.filter { it.num  == c_number }
            if (filteredN.isEmpty()) { numC.add(CardNum(c_number, listOf<Int>(c_type).toMutableList())) }
            else filteredN[0]!!.types.add(c_type)
        }

        // 주어진 5개의 카드에 대해 카운터 생성
        for (i in 1..newCardsNum.count()){
            val card: Int = newCardsNum[i-1]
            makeCounter(card/13, card%13)
        }

        // 문양 종류가 1개인지 확인 - * 플러시
        if(typeC.size == 1) {
            // A를 제외하고 카드의 숫자를 정렬
            val del_A_Sorted = typeC[0].nums.filter{it in 1..12}.sorted().toList()
            when(del_A_Sorted.size){
                5 -> { // Search Straight_Flush
                    if (del_A_Sorted[0]+4 == del_A_Sorted[4]) { return STRAIGHT_FLUSH } // 첫 숫자+4가 마지막숫자? -> 나머지 카드번호 사이의 간격이 모두 1
                }
                4 -> { // Search Royal_Straight_Flush or Back_Straight_Flush
                    if (del_A_Sorted[3] == 4) { return BACK_STRAIGHT_FLUSH } // 마지막 숫자가 4? -> 1,2,3,4
                    if (del_A_Sorted[0] == 9) { return ROYAL_STRAIGHT_FLUSH }// 첫 숫자가 9? -> 9,10,11,12
                }
            }
            isflush = true // 플러시 플래그 on
        }

        // 숫자 종류가 2개인지 확인 - 포카드 or 풀하우스
        if (numC.size == 2){
            when(numC[0].types.size){ // Search Four_Card or Full_House
                1, 4 -> { return FOUR_CARD }; 2, 3 -> { return FULL_HOUSE }
            }
        }

        // 다음 우선 족보 - 플러시
        if (isflush == true){ return FLUSH }

        // 숫자 종류가 5개인지 확인 - 마운틴 or 백 스트레이트 or 스트레이트
        if (numC.size == 5){
            // A를 제외하고 카드의 숫자를 정렬
            val del_A_Sorted = numC.filter{it.num in 1..12}.sortedBy { it.num }.toList()
            when(del_A_Sorted.size){
                5 -> { // Search Straight
                    if (del_A_Sorted[0].num+4 == del_A_Sorted[4].num) { return STRAIGHT }
                }
                4 -> { // Search Back_Straight or Mountain
                    if (del_A_Sorted[3].num == 4) { return BACK_STRAIGHT }
                    if (del_A_Sorted[0].num == 9) { return MOUNTAIN }
                }
            }
        }

        // Search Triple, Two_Pair, One_Pair
        var num_of_pair = 0
        for (item in numC){ // 트리플, 페어 확인
            when(item.types.size){ 3 -> return TRIPLE; 2 -> num_of_pair += 1 }
        }
        when(num_of_pair){ 2 -> return TWO_PAIR; 1 -> return ONE_PAIR; else -> return TOP }
    }
    fun printRes (){
        model.states.observe(this,{ main.mainText!!.setText(it.cardText) })
        for (i in 0..4){ layoutCards[i]!!.setImageResource(newCards[i]) }
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
            if (firstTime != 0) { model.setStates(detectCards()) }  // 시작, 회전 시 검사 방지
            else {firstTime = 1}; printRes()
        })


        main.btnShuffle.setOnClickListener { model.shuffle() }
        main.btnSimulation.setOnClickListener {
            var count = 0
            do { model.shuffle() } while( ++count < 100000 )
            showDialogs()
        }
        main.btnShow.setOnClickListener{ showDialogs() }
    }

    override fun onPause() {
        val dialog = supportFragmentManager.findFragmentByTag("ResultDialog") as DialogFragment?
        dialog?.dismiss(); super.onPause()
    }

    private fun getCardName(c: Int) : String {
        var shape = when(c / 13){
            0 -> "spades"; 1 -> "diamonds"; 2 -> "hearts"; 3 -> "clubs"
            else -> "error"
        }
        val number = when(c % 13){
            in 1..9 -> (c % 13 + 1).toString()
            0 -> "ace"; 10 -> "jack"; 11 -> "queen"; 12 -> "king"
            else -> "error"
        }

        if (c == -1){ return "c_black_joker" }
        else if (c % 13 in 10..12){ return "c_${number}_of_${shape}2" }
        else{ return "c_${number}_of_${shape}" }
    }

}