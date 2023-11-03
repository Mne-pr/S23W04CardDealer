package kr.ac.kumoh.ce.s20190622.s23w04carddealer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.random.Random
//import kr.ac.kumoh.ce.s20190622.s23w04carddealer.MainActivity

class CardDealerViewModel : ViewModel() {
    private var _cards        = MutableLiveData(IntArray(5) { -1 })
    private var _stateString  = MutableLiveData(CardsState(100,"GO!"))
    private var _stateCounter = MutableLiveData(IntArray(14){ 0 })

    val cards: LiveData<IntArray>    get() = _cards
    val states: LiveData<CardsState> get() = _stateString
    val counts: LiveData<IntArray>   get() = _stateCounter

    fun stateCounterAdd(v: Int){
        _stateCounter.value!![v] += 1
        _stateCounter.value!![13] += 1
    }
    fun stateCounterInit(){
        _stateCounter.value = IntArray(13){ 0 }
    }
    fun setStates(data: CardsState){
        _stateString = MutableLiveData<CardsState>(data)
        stateCounterAdd(_stateString.value!!.code)
//        Log.i("what States","${data} -> ${_stateString.value!!.cardText}")
//        for (t in 0..12){
//            Log.i("stateeee ${t}",_stateCounter.value!!.get(t).toString())
//        }
    }

    fun shuffle() {
        var num = 0
        val newCards = IntArray(5) { -1 }

        for (i in newCards.indices) {
            do {
                num = Random.nextInt(52)
            } while (num in newCards)
            newCards[i] = num
        }
        newCards.sort()
        _cards.value = newCards // important
    }


}