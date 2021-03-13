package com.catnemo.camerastudy

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DemoListActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_list)
        val rv = findViewById<RecyclerView>(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = DemoListAdapter(this)

        rv.addItemDecoration(object : RecyclerView.ItemDecoration() {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            init {
                paint.color = Color.GRAY
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
            }

            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                val count = parent.childCount
                for (i in 0 until count) {
                    val view = parent.getChildAt(i)
                    c.drawLine(view.left.toFloat(), view.bottom.toFloat(), view.right.toFloat(), view.bottom.toFloat(), paint)
                }
            }

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.bottom = 10
                outRect.top = 10
                outRect.left = 5
                outRect.right = 5
            }
        })
    }
}

private class DemoListAdapter(val activity: Activity) : RecyclerView.Adapter<ViewHolder>() {

    private val list = arrayListOf(Item("相机", MainActivity::class.java))
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(TextView(parent.context))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position]) { clazz ->
            activity.startActivity(Intent(activity, clazz))
        }
    }
}

private class ViewHolder(private val text: TextView) : RecyclerView.ViewHolder(text) {
    init {
        text.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        text.textSize = 28f
        text.gravity = Gravity.CENTER_VERTICAL
    }

    fun bindData(item: Item, click: (Class<out Activity>) -> Unit) {
        text.text = item.name
        text.setOnClickListener {
            click(item.clazz)
        }
    }
}

private data class Item(val name: String, val clazz: Class<out Activity>)