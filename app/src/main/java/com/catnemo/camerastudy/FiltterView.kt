package com.catnemo.camerastudy

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.catnemo.camerastudy.util.UIUtils

/**
 * @description 特效view
 * @author  franticzhou
 * @date    2019-12-13   20:03
 */
class FilterView : LinearLayout {

    private var recyclerView: RecyclerView? = null
    private var mEffecrAdapter: FilterAdapter? = null
    var onFilterProgressChanged: OnFilterProgressChanged? = null

    interface OnFilterProgressChanged {
        fun onProgess(process: Float)
    }

    companion object {
        val datalist = arrayListOf(Filter("NONE", 0),
                Filter("透亮", R.raw.lookup_touliang5),
                Filter("清凉", R.raw.lookup_qinliang5),
                Filter("红润", R.raw.lookup_hongrun5),
                Filter("白嫩", R.raw.lookup_bainen5),
                Filter("复古", R.raw.lookup_fugu5),
                Filter("黑白", R.raw.lookup_heibai5),
                Filter("清晨", R.raw.qingchen),
                Filter("少女", R.raw.shaonv2),
                Filter("桃子", R.raw.lookup_taozi5),
                Filter("水润", R.raw.lookup_shuirun5),
                Filter("水光", R.raw.lookup_shuiguang5),
                Filter("圣代", R.raw.lookup_shengdai5),
                Filter("自然", R.raw.loopup_ziran5)
        )
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        orientation = VERTICAL
        val seekBar = SeekBar(context)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    onFilterProgressChanged?.onProgess(progress / 100f)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        val slp = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        slp.bottomMargin = UIUtils.dip2px(context, 20f)
        slp.topMargin = UIUtils.dip2px(context, 20f)
        addView(seekBar, slp)
        recyclerView = RecyclerView(context)
        recyclerView?.layoutManager = GridLayoutManager(context, 5)
        recyclerView?.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.bottom += UIUtils.px2dip(parent.context, 20f)
            }
        })
        mEffecrAdapter = FilterAdapter()
        recyclerView?.adapter = mEffecrAdapter
        val rvLp = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtils.dip2px(context, 200f))
        addView(recyclerView, rvLp)
    }

    fun init(effecrClick: FilterAdapter.OnFilterClick) {
        mEffecrAdapter?.mEffectList = datalist
        mEffecrAdapter?.onFlingListener = effecrClick
        mEffecrAdapter?.notifyDataSetChanged()
    }
}

class FilterAdapter : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {
    var mEffectList: ArrayList<Filter> = ArrayList()
    var onFlingListener: OnFilterClick? = null
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = mEffectList[position].name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = TextView(parent.context)
        textView.setBackgroundColor(Color.WHITE)
        textView.gravity = Gravity.CENTER
        val lp = RecyclerView.LayoutParams(UIUtils.dip2px(parent.context, 50f), UIUtils.dip2px(parent.context, 50f))
        textView.layoutParams = lp
        return ViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return mEffectList.size
    }

    inner class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
        init {
            textView.setOnClickListener {
                onFlingListener?.onFilterClick(mEffectList[adapterPosition])
            }
        }
    }

    interface OnFilterClick {
        fun onFilterClick(effect: Filter)
    }
}

data class Filter(val name: String, val lutPath: Int)
