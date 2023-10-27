package kr.ac.kumoh.ce.s20190622.s23w04carddealer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import kr.ac.kumoh.ce.s20190622.s23w04carddealer.databinding.ActivityMainBinding
import kotlin.math.log

data class cardType(var type: Int, var nums: MutableList<Int>)
data class cardNum(var num: Int, var types: MutableList<Int>)

class MainActivity : AppCompatActivity() {
    private lateinit var main: ActivityMainBinding
    private lateinit var model: CardDealerViewModel
    private var newCards = Array(5){ 0 }
    private var newCardsNum = IntArray(5){ 0 }
    private var firstTime = true
    private var status: String = ""

    companion object {
        const val ROYAL_STRAIGHT_FLUSH: String = "!!!!!Royal Straight Flush!!!!!"

        const val BACK_STRAIGHT_FLUSH:  String = "!!!!Back Straight Flush!!!!"
        const val STRAIGHT_FLUSH:       String = "!!!!Straight Flush!!!!"

        const val FOUR_CARD:            String = "!!!Four Card!!!"
        const val FULL_HOUSE:           String = "!!!Full House!!!"
        const val FLUSH:                String = "!!!Flush!!!"

        const val MOUNTAIN:             String = "!!Mountain!!"
        const val BACK_STRAIGHT:        String = "!!Back Straight!!"

        const val STRAIGHT:             String = "!Straight!"
        const val TRIPLE:               String = "!Triple!"

        const val TWO_PAIR:             String = "Two Pair"
        const val ONE_PAIR:             String = "One Pair"
        const val TOP:                  String = "Top.."

    }

    fun detectCards(){
        // 같은 모양, 숫자에 따른 카운터
        var typeC = mutableListOf<cardType>() // 모양 4개
        var numC  = mutableListOf<cardNum>()  // 숫자 13개
        var isRight: Boolean = false // 검사 결과가 맞는지를 담당
        var isflush: Boolean = false // 플러시인지 판단

        fun makeCounter(c_type: Int, c_number: Int){
            // 카운터에 추가하기 전, 이미 있는 항목은 아닌지 검사
            val filteredShape  = typeC.filter{it.type == c_type  }
            val filteredNumber = numC.filter {it.num  == c_number}

            // 같은 모양 - 카운터에 이미 있는 항목(type)인지에 따라 적절하게 추가
            if (filteredShape.isEmpty())
                typeC.add( cardType( c_type, listOf<Int>(c_number).toMutableList() ) )
            else filteredShape[0]!!.nums.add(c_number)

            // 같은 숫자 - 카운터에 이미 있는 항목(num)인지에 따라 적절하게 추가
            if (filteredNumber.isEmpty())
                numC.add( cardNum( c_number, listOf<Int>(c_type).toMutableList() ) )
            else filteredNumber[0]!!.types.add(c_type)
        }

        // 주어진 5개의 카드에 대해 카운터 생성
        for (i in 1..newCardsNum.count()){
            val card: Int = newCardsNum[i-1]
            makeCounter(card/13, card%13)
        }

        var i = 0
        for (item in typeC){ Log.i("typeC[${i++}]", "type : ${item.type}, nums : ${item.nums}") }; i = 0
        for (item in numC){ Log.i("numC[${i++}]", "num : ${item.num}, types : ${item.types}") }


        // 문양 종류가 1개인지 확인 - * 플러시
        if(typeC.size == 1) {

            // Search Royal Straight Flush
            // Log.i("where", ROYAL_STRAIGHT_FLUSH)
            isRight = true
            val arrayForRSF = mutableListOf<Int>(0, 10, 11, 12, 9) // A, K, Q, J, 10
            for (i in typeC[0].nums){ // typeC 크기는 1이므로 0번째 것 확인
                if (i in arrayForRSF) { arrayForRSF.remove(i) }
                else { isRight = false; break  }
            }
            if (isRight == true) { model.setStates(ROYAL_STRAIGHT_FLUSH); return }

            // Search Back Straight Flush
            // Log.i("where", BACK_STRAIGHT_FLUSH)
            isRight = true
            val arrayForBSF = mutableListOf<Int>(0 ,1 ,2 ,3 ,4) // A, 2, 3, 4, 5
            for (i in typeC[0].nums) {
                if (i in arrayForBSF){ arrayForBSF.remove(i) }
                else { isRight = false; break }
            }
            if (isRight == true){ model.setStates(BACK_STRAIGHT_FLUSH); return }

            // Search Straight Flush
            // Log.i("where", STRAIGHT_FLUSH)
            isRight = true
            val filterForStraight = typeC[0].nums.filter{it in 1..12}.sorted().toList()
            if (filterForStraight.size == 5){ // A 제거해도 5장이면 조건충족
                for (i in 0..3){ // [i+1] == [i] 반복검사
                    if (filterForStraight[i]+1 != filterForStraight[i+1]) { isRight = false; break }
                }
                if (isRight == true){ model.setStates(STRAIGHT_FLUSH); return }
            }

            // 아무튼 플러시 달성. 하지만 플러시보다 더 높은 족보 있으므로 리턴 보류
            isflush = true
        }

        // 숫자 종류가 2개인지 확인 - 포카드 or 풀하우스
        if (numC.size == 2){
            // Search Four Card
            // Log.i("where", FOUR_CARD) // 요소들의 nums가 1 4 면 포카드
            if (numC[0].types.size == 4 || numC[1].types.size == 4) { model.setStates(FOUR_CARD); return }

            // Search Full House
            // Log.i("where", FULL_HOUSE) // 요소들의 nums가  2 3 면 풀하우스
            var full_house = 0
            if (numC[0].types.size == 2 && numC[1].types.size == 3)      {full_house = 1}
            else if (numC[0].types.size == 3 && numC[1].types.size == 2) {full_house = 1}
            if (full_house == 1) { model.setStates(FULL_HOUSE); return }
        }

        // 플러시의 차례가 돌아왔다.
        // Log.i("where", FLUSH)
        if (isflush == true){ model.setStates(FLUSH); return }

        // 숫자 종류가 5개인지 확인 - 마운틴 or 백 스트레이트 or 스트레이트
        if (numC.size == 5){
            // Search Mountain - 문제
            // Log.i("where", MOUNTAIN)
            isRight = true
            var arrayForMountain = mutableListOf<Int>(0, 10, 11, 12, 9) // A, K, Q, J, 10
            for (item in numC){ // 모양 상관없이 숫자만 존재하는지
                if (!(item.num in arrayForMountain)) { isRight = false; break }
                else { arrayForMountain.remove(item.num) }
            }
            if (isRight == true){ model.setStates(MOUNTAIN); return }

            // Search Back Straight - 문제
            // Log.i("where", BACK_STRAIGHT)
            isRight = true
            var arrayForStraight = mutableListOf<Int>(0, 1, 2, 3, 4) // A, 2, 3, 4, 5
            for (item in numC){ // 모양 상관없이 해당 숫자만 중복없이 존재하는지
                if(!(item.num in arrayForStraight)) { isRight = false; break }
                else { arrayForStraight.remove(item.num) }
            }
            if (isRight == true){ model.setStates(BACK_STRAIGHT); return }

            // Search Straight
            // Log.i("where", STRAIGHT)
            isRight = true
            val sortedByNum = numC.filter{it.num in 1..12}.sortedBy { it.num }.toList()
            if(sortedByNum.size == 5){ // A를 쳐내도 5장이어야 함
                for (item in 0..3){ // 숫자가 연속적으로 있는지 확인
                    if (sortedByNum[item].num+1 != sortedByNum[item+1].num) { isRight = false; break }
                }
                if (isRight == true){ model.setStates(STRAIGHT); return }
            }
        }

        // Search Triple, Two Pair, One Pair
        // Log.i("where", "${TRIPLE} ${TWO_PAIR} ${ONE_PAIR}")
        var num_of_pair: Int = 0
        for (item in numC){ // numC의 types 크기가 2개인지 확인
            // 트리플 확인 -> 바로 리턴
            if(item.types.size == 3) { model.setStates(TRIPLE); return }
            // 페어 확인 -> 개수 카운트
            if(item.types.size == 2) { num_of_pair += 1 }
        }
        when(num_of_pair){
            2 ->    {model.setStates(TWO_PAIR); return}
            1 ->    {model.setStates(ONE_PAIR); return}
            else -> {model.setStates(TOP);      return}
        }

        // 마지막에 숫자가 가장 높은 카드, 탑 - numberCounter의 가장 큰 숫자, 그 타입을 읽어오면 끝. 여러 장 있을수도..

        // 페어 계열
        // 숫자가 같은 카드 2장 1쌍 - 원 페어 - numberCounter에서 각 구조체의 타입이 2개인지 확인
        // 숫자가 같은 카드 2장 2쌍 - 투 페어 - numberCounter에서 각 구조체의 타입이 2개, 2쌍인지 확인
        // 숫자가 같은 카드 3장 1쌍 - 트리플 - numberCounter에서 각 구조체의 타입이 3개인지 확인

        // 스트레이트 계열
        // 숫자만 이어지는 카드 5장 - 스트레이트 - numberCounter에서 각 구조체 넘버가 연속적으로 있는지 확인
        // okA,2,3,4,5 이어지는 카드 - 백 스트레이트 - numberCounter에서 모두 있는지 확인
        // okA,K,Q,J,10 이어지는 카드 - 마운틴 - numberCounter에서 모두 있는지 확인

        // 플러시 계열
        // ok무늬가 같은 카드 5장 - 플러시 - typeCounter에서 요소가 하나만 있는지 확인
        // ok원페어+트리플 - 풀하우스 - typeCounter에서 원 페어를 위의 방법대로 찾고, 나머지 카드 3장이 한 요소에 있는지 확인
        // ok숫자가 같은 카드 4장 - 포 카드 - numCounter에서 요소가 2개인지 검사, 하나는 type이 3, 나머지는 2여야
        // ok스트레이트, 무늬가 같음 - 스트레이트 플러시 - typeCounter에서 요소가 하나인지 검사, 그 요소의 num이 연속인지
        // ok백스트레이트, 무늬가 같음 - 백 스트레이트 플러시 - typeCounter에서 요소가 하나인지, 그 요소가 백스트레이트인지
        // ok마운틴, 무늬가 같음 - 로열 스트레이트 플러시 - typeCounter에서 요소가 하나인지, 그 요소가 마운틴인지

    }
    fun printRes (){
        main.mainText!!.setText(this.status)
        main.card1!!.setImageResource(newCards[0])
        main.card2!!.setImageResource(newCards[1])
        main.card3!!.setImageResource(newCards[2])
        main.card4!!.setImageResource(newCards[3])
        main.card5!!.setImageResource(newCards[4])
    }
    fun readText(){
        model.states.observe(this,{ status = it })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        main = ActivityMainBinding.inflate(layoutInflater)
        setContentView(main.root)

        model = ViewModelProvider(this)[CardDealerViewModel::class.java]
        model.cards.observe(this, {
            for (i in newCards.indices) {
                newCards[i] = resources.getIdentifier(
                    //getCardName(model.cards.value!![0]), // null이 될 수가 없다는 확신의 !!
                    getCardName(it[i]),"drawable", packageName
                )
            }
            newCardsNum = it

            if (firstTime == true) {
//                if (!(status.equals(TOP) || status.equals(ONE_PAIR) || status.equals(TWO_PAIR) || status.equals(TRIPLE)) || status.equals("Start!")){
//                    main.btnShuffle.isEnabled = false
//                    Handler(Looper.getMainLooper()).postDelayed({main.btnShuffle.isEnabled = true}, 1500)
//                }
                firstTime = false
            }
            else detectCards()
            readText()
            printRes()
        })


        main.btnShuffle.setOnClickListener {
            //model.shuffle()
            do { model.shuffle() } while(!status.equals(STRAIGHT))//while(status.equals(TOP) || status.equals(ONE_PAIR) || status.equals(TWO_PAIR))
        }
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

        if (c == -1)
            return "c_black_joker"
        else if ( c % 13 in 10..12)
            return "c_${number}_of_${shape}2"
        else
            return "c_${number}_of_${shape}"
    }


}