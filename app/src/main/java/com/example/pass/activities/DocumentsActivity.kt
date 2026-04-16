package com.example.pass.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pass.R
import com.example.pass.adapters.DocumentAdapter
import com.example.pass.database.AppDatabase
import com.example.pass.database.documents.TypeDocument
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class DocumentsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)

        val nameTypeDocumentationList: List<String>? = intent.getStringArrayListExtra("nameTypeDocumentsList")
        val typeDocumentationList: List<TypeDocument> = nameTypeDocumentationList?.map { TypeDocument.valueOf(it) } ?: listOf()
        val currUserId: Long = intent.getLongExtra("currUserId", -1)
        val nameActivity: String = intent.getStringExtra("nameActivity") ?: "Документация"

        findViewById<TextView>(R.id.nameActivityDocumentation).text = nameActivity

        val returnButton: ImageView = findViewById(R.id.returnButton)
        val addDocumentsButton: FrameLayout = findViewById(R.id.addDocumentsButton)
        val database: AppDatabase = AppDatabase.getDatabase(this)

        createDocumentList(database, this, typeDocumentationList)

        addDocumentsButton.setOnClickListener { Animates().animatesButton(it) { addDocumentsButton(this, currUserId) } }

        returnButton.setOnClickListener {
            Animates().animatesButton(it) { finish() }
        }
    }

    private fun addDocumentsButton(context: Context, currUserId: Long) {
        val intent = Intent(context, CreateDocumentsActivity::class.java)

        intent.putExtra("currUserId", currUserId)
        intent.putExtra("nameTypeDocumentsList", arrayListOf(TypeDocument.SHOPPING_PLAN.name, TypeDocument.FORECAST_OF_PLANNED_COSTS.name))

        startActivity(intent)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun createDocumentList(database: AppDatabase, context: Context, typesDocumentationList: List<TypeDocument>) {
        val recyclerView: RecyclerView = findViewById(R.id.oborudViewer)
        val searchInput: EditText = findViewById(R.id.search_document)

        val adapter = DocumentAdapter { documentDescription, name, date ->
            saveHtmlAsPdf(documentDescription, name, date)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val searchQuery = MutableStateFlow("")

        searchInput.addTextChangedListener {
            searchQuery.value = it.toString()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchQuery
                    .debounce(150)
                    .flatMapLatest { text ->
                        database.documentDao().getDocumentsFlow(typesDocumentationList, text)
                    }
                    .collect { list ->
                        adapter.submitList(list)
                    }
            }
        }
    }

    private fun saveHtmlAsPdf(htmlString: String, name: String, date: String) {
        val webView = WebView(this)
        val repName = name.replace(" ", "_")
        val repDate = date.replace(".", "_")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "${repName}_${repDate}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)

                printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, htmlString, "text/html", "utf-8", null)
    }
}