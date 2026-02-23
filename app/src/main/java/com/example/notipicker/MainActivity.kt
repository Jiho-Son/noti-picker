package com.example.notipicker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notipicker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repo: KeywordRepository
    private lateinit var adapter: KeywordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repo = KeywordRepository(this)
        adapter = KeywordAdapter(
            onRemove = { keyword ->
                repo.removeKeyword(keyword)
                refreshKeywords()
            }
        )

        binding.keywordList.layoutManager = LinearLayoutManager(this)
        binding.keywordList.adapter = adapter

        binding.addButton.setOnClickListener {
            val text = binding.keywordInput.text.toString()
            repo.addKeyword(text)
            binding.keywordInput.text?.clear()
            refreshKeywords()
        }

        binding.openNotificationAccessButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        refreshKeywords()
    }

    private fun refreshKeywords() {
        val items = repo.getKeywords()
        adapter.submit(items)
        binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }
}

private class KeywordAdapter(
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<KeywordViewHolder>() {

    private val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return KeywordViewHolder(view, onRemove)
    }

    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submit(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}

private class KeywordViewHolder(
    itemView: View,
    private val onRemove: (String) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val textView: TextView = itemView.findViewById(android.R.id.text1)
    private var currentKeyword: String? = null

    init {
        itemView.setOnLongClickListener {
            currentKeyword?.let { onRemove(it) }
            true
        }
    }

    fun bind(keyword: String) {
        currentKeyword = keyword
        textView.text = keyword
    }
}

