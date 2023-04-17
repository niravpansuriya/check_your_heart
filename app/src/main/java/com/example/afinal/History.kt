package com.example.afinal

//import HistoryTable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseError

// this class represents history fragment
class History : Fragment(R.layout.fragment_history) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        var tableDataList = listOf<TableData>()

        // get history data
        getHistory(
            { data: MutableList<Map<String, Any>>?->

                if(data != null) {
                    tableDataList = data.map { map ->
                        TableData(
                            date = map["date"] as String,
                            time = map["time"] as String,
                            result = map["result"] as String
                        )
                    }
                }
                recyclerView.adapter = TableDataAdapter(tableDataList)
            },
            {error: DatabaseError->

                Toast.makeText(requireContext(), "There is something wrong", Toast.LENGTH_SHORT)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TableDataAdapter(tableDataList)


    }
}

