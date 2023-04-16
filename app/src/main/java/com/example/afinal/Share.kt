package com.example.afinal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.io.File
import java.io.FileOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Share.newInstance] factory method to
 * create an instance of this fragment.
 */
class Share : Fragment() {

    private lateinit var shareButton: Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_share, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        shareButton = view.findViewById(R.id.shareHistoryButton)!!

        shareButton.setOnClickListener {
            // Call the shareCsv() function
            shareHistory()
        }
    }


    private fun shareHistory() {
        // Replace this with your actual data
        var historyData = listOf<Map<String, Any>>()
        getHistory(
            { data: MutableList<Map<String, Any>>?->
                if(data != null) {
                   historyData = data

                    val csvContent = generateCsvContent(historyData)

                    lifecycleScope.launch {
                        val csvFile = saveCsvToFile(requireContext(), "history.csv", csvContent)
                        shareCsvFile(requireContext(), csvFile)
                    }
                }
            },
            {error: DatabaseError ->
                Toast.makeText(requireContext(), "There is something wrong", Toast.LENGTH_SHORT)
            }
        );
    }

    private fun generateCsvContent(data: List<Map<String, Any>>): String {
        val header = "Date,Time,Result"
        val rows = data.joinToString("\n") { row ->
            "${row["date"]},${row["time"]},${row["result"]}"
        }
        return "$header\n$rows"
    }

    private suspend fun saveCsvToFile(context: Context, fileName: String, content: String): File =
        withContext(IO) {
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            file
        }

    private fun shareCsvFile(context: Context, file: File) {
        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(shareIntent, "Share History"))
    }
}