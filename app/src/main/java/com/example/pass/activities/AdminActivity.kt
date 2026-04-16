package com.example.pass.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pass.R
import com.example.pass.adapters.UserAdapter
import com.example.pass.database.AppDatabase
import com.example.pass.database.users.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener
import com.example.pass.database.documents.TypeDocument
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        val currUserId = intent.getLongExtra("currUserId", -1)

        val database: AppDatabase = AppDatabase.getDatabase(this)
        val addButton: FrameLayout = findViewById(R.id.addPersonalButton)
        val documentsButton: FrameLayout = findViewById(R.id.documentsButton)

        createCardList(database, this)

        addButton.setOnClickListener {
            Animates().animatesButton(it) { getAddPersonalActivity(this) }
        }

        documentsButton.setOnClickListener {
            Animates().animatesButton(it) { getDocumentsActivity(this, currUserId) }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun createCardList(database: AppDatabase, context: Context) {
        val recyclerView: RecyclerView = findViewById(R.id.oborudViewer)
        val searchInput: EditText = findViewById(R.id.search_users)

        val adapter = UserAdapter { userId ->
            val intent = Intent(this, EditUserActivity::class.java).apply {
                putExtra("USER_ID", userId)
            }

            startActivity(intent)
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
                        database.usersDao().getAllUserOnTechnical(Role.TECH_SPECIALIST, text)
                    }
                    .collect { list ->
                        adapter.submitList(list)
                    }
            }
        }
    }

    private fun getAddPersonalActivity(context: Context) {
        startActivity(Intent(context, AddUserActivity::class.java))
    }

    private fun getDocumentsActivity(context: Context, userId: Long) {
        val intent = Intent(context, DocumentsActivity::class.java)

        intent.putExtra("nameTypeDocumentsList", arrayListOf(TypeDocument.SHOPPING_PLAN.name, TypeDocument.FORECAST_OF_PLANNED_COSTS.name))
        intent.putExtra("currUserId", userId)
        intent.putExtra("nameActivity", getString(R.string.name_documentations))

        startActivity(intent)
    }

}