package pers.zhc.tools.fourierseries

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fourier_series_epicycle_item.view.*
import kotlinx.android.synthetic.main.fourier_series_main.*
import pers.zhc.tools.BaseActivity
import pers.zhc.tools.R
import pers.zhc.tools.jni.JNI
import pers.zhc.tools.utils.AsyncTryDo
import pers.zhc.tools.utils.DialogUtils
import pers.zhc.tools.utils.ProgressDialog
import pers.zhc.tools.utils.ToastUtils

/**
 * @author bczhc
 */
class FourierSeriesActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var periodET: EditText
    private lateinit var epicycleNumET: EditText
    private lateinit var threadsNumET: EditText
    private lateinit var integralSegNumET: EditText
    private val epicycleData = Epicycles()
    private lateinit var listAdapter: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fourier_series_main)

        val drawButton = draw_btn!!
        val computeButton = compute_btn!!
        val startButton = start_btn!!
        integralSegNumET = integral_fragment_number!!.editText
        threadsNumET = threads_num!!.editText
        epicycleNumET = epicycles_number!!.editText
        periodET = period!!.editText
        recyclerView = recycler_view!!

        listAdapter = ListAdapter(this, epicycleData)
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // set the default values
        @Suppress("SetTextI18n")
        run {
            integralSegNumET.setText("100000")
            threadsNumET.setText(Runtime.getRuntime().availableProcessors().toString())
            epicycleNumET.setText("100")
            periodET.setText("100")
        }

        drawButton.setOnClickListener {
            startActivity(Intent(this, DrawingActivity::class.java))
        }
        computeButton.setOnClickListener {
            showComputeDialog()
        }
        startButton.setOnClickListener {
            startActivity(Intent(this, EpicycleDrawingActivity::class.java))
        }
    }

    private fun showComputeDialog() {
        val points = DrawingActivity.points
        if (points == null) {
            ToastUtils.show(this, R.string.fourier_series_no_curve_toast)
            return
        }

        epicycleData.clear()

        val epicycleNum = epicycleNumET.text.toString().toInt()
        val integralSegments = integralSegNumET.text.toString().toInt()
        val period = periodET.text.toString().toDouble()
        val threadsNum = threadsNumET.text.toString().toInt()

        val dialog = ProgressDialog(this).apply {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            DialogUtils.setDialogAttr(this, width = MATCH_PARENT)
        }
        val progressView = dialog.getProgressView().apply {
            setIsIndeterminateMode(false)
            setTitle(getString(R.string.fourier_series_computing_dialog_title))
        }
        dialog.show()

        val asyncTryDo = AsyncTryDo()
        val lock = Any()
        Thread {
            val array = points.toArray(Array(0) { return@Array InputPoint(0F, 0F) })
            JNI.FourierSeries.compute(
                array,
                integralSegments,
                period,
                epicycleNum,
                threadsNum
            ) { re, im, n, p ->
                synchronized(lock) {
                    val epicycle = Epicycle(n, ComplexValue(re, im), p)
                    Log.d(TAG, "showComputeDialog: $epicycle")
                    epicycleData.add(epicycle)
                    asyncTryDo.tryDo { _, notifier ->
                        runOnUiThread {
                            progressView.setProgress(epicycleData.size.toFloat() / epicycleNum.toFloat())
                        }
                        notifier.finish()
                    }
                }
            }
            runOnUiThread {
                dialog.dismiss()
                listAdapter.notifyDataSetChanged()
            }
        }.start()
    }

    class ListAdapter(private val context: Context, private val epicycleData: Epicycles) :
        RecyclerView.Adapter<ListAdapter.Holder>() {
        class Holder(val view: View) : RecyclerView.ViewHolder(view) {
            val textView = view.text_view!!
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val inflate = LayoutInflater.from(context).inflate(R.layout.fourier_series_epicycle_item, parent, false)
            return Holder(inflate)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.textView.text = epicycleData[position].toString()
        }

        override fun getItemCount(): Int {
            return epicycleData.size
        }
    }
}

typealias Epicycles = ArrayList<Epicycle>
