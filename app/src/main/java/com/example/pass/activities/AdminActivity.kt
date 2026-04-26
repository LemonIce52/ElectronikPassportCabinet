package com.example.pass.activities

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
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
import com.example.pass.otherClasses.Animates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        val currUserId = intent.getLongExtra("currUserId", -1)
        val currUserRole = Role.valueOf(intent.getStringExtra("currUserRole") ?: Role.ADMIN.name)

        val database: AppDatabase = AppDatabase.getDatabase(this)
        val addButton: FrameLayout = findViewById(R.id.addPersonalButton)
        val documentsButton: FrameLayout = findViewById(R.id.documentsButton)

        createCardList(database, this, currUserRole)

        addButton.setOnClickListener {
            Animates().animatesButton(it) { getAddPersonalActivity(this, currUserRole) }
        }

        documentsButton.setOnClickListener {
            Animates().animatesButton(it) { getDocumentsActivity(this, currUserId) }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun createCardList(database: AppDatabase, context: Context, role: Role) {
        val recyclerView: RecyclerView = findViewById(R.id.oborudViewer)
        val searchInput: EditText = findViewById(R.id.search_users)

        val adapter = UserAdapter { userId ->
            val intent = Intent(this, EditUserActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("currUserRole", role.name)
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
                        if (role == Role.MAIN_ADMIN) database.usersDao()
                            .getAllUsers(listOf(Role.TECH_SPECIALIST, Role.ADMIN), text)
                        else database.usersDao().getAllUsers(listOf(Role.TECH_SPECIALIST), text)
                    }
                    .collect { list ->
                        adapter.submitList(list)
                    }
            }
        }
    }

    private fun getAddPersonalActivity(context: Context, role: Role) {
        val intent = Intent(context, AddUserActivity::class.java)
        intent.putExtra("currUserRole", role.name)
        startActivity(intent)
    }

    private fun getDocumentsActivity(context: Context, userId: Long) {
        val intent = Intent(context, DocumentsActivity::class.java)

        intent.putExtra(
            "nameTypeDocumentsList",
            arrayListOf(
                TypeDocument.SHOPPING_PLAN.name,
                TypeDocument.FORECAST_OF_PLANNED_COSTS.name
            )
        )
        intent.putExtra("currUserId", userId)
        intent.putExtra("nameActivity", getString(R.string.name_documentations))

        startActivity(intent)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

}