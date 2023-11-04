package kr.ac.kumoh.ce.s20190622.s23w04carddealer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ResultDialog(private val model: CardDealerViewModel) : DialogFragment() {
    private val stateList = arrayOf(
        R.id.royal_straight_flush_value, R.id.back_straight_flush_value,
        R.id.straight_flush_value, R.id.four_card_value, R.id.full_house_value,
        R.id.flush_value, R.id.mountain_value, R.id.back_straight_value,
        R.id.straight_value, R.id.triple_value, R.id.two_pair_value,
        R.id.one_pair_value, R.id.top_value
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.result_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnOk: Button = view.findViewById<Button>(R.id.btn_ok)
        model.counts.observe(this, {
            var i = 0;
            view.findViewById<TextView>(R.id.text_total).setText("${it.get(13)}")
            for (s in stateList){
                if (it.get(13) == 0) {view.findViewById<TextView>(s).setText("0 (0.0000%)")}
                else {view.findViewById<TextView>(s)
                    .setText("${it.get(i).toString()} (${String.format("%.4f",100.0 * it.get(i) / it.get(13))}%)")}
                i++
            }
        })

        btnOk.setOnClickListener{ dismiss() }
    }
}