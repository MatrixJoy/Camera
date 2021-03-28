package com.catnemo.avstudy

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.catnemo.avstudy.util.UIUtils
import com.catnemo.zfilter.filter.*

/**
 * @description 特效view
 * @author  matrixJoy
 * @date    2019-12-13   20:03
 */
class EffectView : FrameLayout {

    private var recyclerView: RecyclerView? = null
    private var mEffectAdapter: EffectAdapter? = null

    companion object {
        val datalist = arrayListOf(Effect("NONE", BaseFilter()),
                Effect("双屏", TwoScreenFilter()),
                Effect("三屏", ThreeScreenFilter()),
                Effect("三屏高斯", ThreeBlurScreenFilter()),
                Effect("四屏", FourScreenFilter()),
                Effect("九屏", NineScreenFilter()),
                Effect("镜像H", HorizontalFlipFilter()),
                Effect("镜像V", VerticalFlipFilter()),
                Effect("模糊", BlurScreenFilter())

        )
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        recyclerView = RecyclerView(context)
        recyclerView?.layoutManager = GridLayoutManager(context, 5)
        recyclerView?.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.bottom += UIUtils.px2dip(parent.context, 20f)
            }
        })
        mEffectAdapter = EffectAdapter()
        recyclerView?.adapter = mEffectAdapter
        val rvLp = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtils.dip2px(context, 200f))
        addView(recyclerView, rvLp)
    }

    fun init(effectClick: EffectAdapter.OnEffectClick) {
        mEffectAdapter?.mEffectList = datalist
        mEffectAdapter?.mOnFlingListener = effectClick
        mEffectAdapter?.notifyDataSetChanged()
    }
}

class EffectAdapter : RecyclerView.Adapter<EffectAdapter.ViewHolder>() {
    var mEffectList: ArrayList<Effect> = ArrayList()
    var mOnFlingListener: OnEffectClick? = null
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
                mOnFlingListener?.onEffectClick(mEffectList[adapterPosition])
            }
        }
    }

    interface OnEffectClick {
        fun onEffectClick(effect: Effect)
    }
}

data class Effect(val name: String, val filter: BaseFilter)