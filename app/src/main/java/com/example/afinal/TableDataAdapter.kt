package com.example.afinal

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

// this draws a table in history fragment
class TableDataAdapter(private val tableDataList: List<TableData>) : RecyclerView.Adapter<TableDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_table, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // columns name
        if (position == 0) {
            // Set column titles
            holder.dateTextView.text = "Date"
            holder.timeTextView.text = "Time"
            holder.resultTextView.text = "Result"
        } else {

            val tableData = tableDataList[position-1]
            holder.dateTextView.text = tableData.date
            holder.timeTextView.text = tableData.time

            holder.resultTextView.text = tableData.result
            holder.resultTextView.setTextColor(
                // if result is normal, then text color is Green
                if (tableData.result == "normal") holder.itemView.context.getColor(R.color.normal_result)

                // if result is abnormal, then text color is Red
                else holder.itemView.context.getColor(R.color.abnormal_result)
            )
        }
    }

    override fun getItemCount(): Int = tableDataList.size + 1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val resultTextView: TextView = view.findViewById(R.id.resultTextView)
    }
}
