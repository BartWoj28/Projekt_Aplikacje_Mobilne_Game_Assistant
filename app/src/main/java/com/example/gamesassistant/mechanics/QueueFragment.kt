package com.example.gamesassistant.mechanics

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamesassistant.R
import com.google.android.material.button.MaterialButton

class QueueFragment : Fragment() {

    private lateinit var llEditMode: LinearLayout
    private lateinit var llQueueMode: LinearLayout

    private lateinit var etPlayerName: EditText
    private lateinit var btnAddPlayer: MaterialButton
    private lateinit var btnToggleDelete: MaterialButton
    private lateinit var rvPlayers: RecyclerView
    private lateinit var btnStartQueue: MaterialButton

    private lateinit var tvQueueTurn: TextView
    private lateinit var tvCurrentPlayer: TextView
    private lateinit var btnPrev: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var btnCancelQueue: MaterialButton

    private var playersList = mutableListOf<String>()
    private var playQueue = listOf<String>()
    
    private var isDeleteMode = false
    private var isPlaying = false
    private var totalIndex = 0

    private lateinit var playerAdapter: PlayerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_queue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llEditMode = view.findViewById(R.id.llEditMode)
        llQueueMode = view.findViewById(R.id.llQueueMode)

        etPlayerName = view.findViewById(R.id.etPlayerName)
        btnAddPlayer = view.findViewById(R.id.btnAddPlayer)
        btnToggleDelete = view.findViewById(R.id.btnToggleDelete)
        rvPlayers = view.findViewById(R.id.rvPlayers)
        btnStartQueue = view.findViewById(R.id.btnStartQueue)

        tvQueueTurn = view.findViewById(R.id.tvQueueTurn)
        tvCurrentPlayer = view.findViewById(R.id.tvCurrentPlayer)
        btnPrev = view.findViewById(R.id.btnPrev)
        btnNext = view.findViewById(R.id.btnNext)
        btnCancelQueue = view.findViewById(R.id.btnCancelQueue)

        playerAdapter = PlayerAdapter(playersList)
        rvPlayers.layoutManager = LinearLayoutManager(requireContext())
        rvPlayers.adapter = playerAdapter

        if (savedInstanceState != null) {
            val savedPlayers = savedInstanceState.getStringArrayList("playersList")
            if (savedPlayers != null) {
                playersList.clear()
                playersList.addAll(savedPlayers)
            }
            
            val savedQueue = savedInstanceState.getStringArrayList("playQueue")
            if (savedQueue != null) {
                playQueue = savedQueue.toList()
            }
            
            isDeleteMode = savedInstanceState.getBoolean("isDeleteMode", false)
            isPlaying = savedInstanceState.getBoolean("isPlaying", false)
            totalIndex = savedInstanceState.getInt("totalIndex", 0)
        }

        playerAdapter.isDeleteMode = isDeleteMode
        updateDeleteButtonColor()

        btnAddPlayer.setOnClickListener {
            val name = etPlayerName.text.toString().trim()
            if (name.isNotEmpty()) {
                playersList.add(name)
                playerAdapter.notifyItemInserted(playersList.size - 1)
                rvPlayers.scrollToPosition(playersList.size - 1)
                etPlayerName.text.clear()
            }
        }

        btnToggleDelete.setOnClickListener {
            isDeleteMode = !isDeleteMode
            playerAdapter.isDeleteMode = isDeleteMode
            updateDeleteButtonColor()
        }

        btnStartQueue.setOnClickListener {
            if (playersList.isEmpty()) {
                Toast.makeText(requireContext(), "List is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            playQueue = playersList.shuffled()
            totalIndex = 0
            isPlaying = true
            
            // Wyłącz tryb usuwania by był zresetowany po powrocie
            isDeleteMode = false
            playerAdapter.isDeleteMode = false
            updateDeleteButtonColor()
            
            updateUIStates()
        }

        btnPrev.setOnClickListener {
            if (totalIndex > 0) {
                totalIndex--
                updateQueueDisplay()
            }
        }

        btnNext.setOnClickListener {
            if (playQueue.isNotEmpty()) {
                totalIndex++
                updateQueueDisplay()
            }
        }

        btnCancelQueue.setOnClickListener {
            isPlaying = false
            updateUIStates()
        }

        updateUIStates()
    }

    private fun updateDeleteButtonColor() {
        val color = if (isDeleteMode) Color.parseColor("#FFCDD2") else Color.parseColor("#E0E0E0")
        btnToggleDelete.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun updateUIStates() {
        if (isPlaying) {
            llEditMode.visibility = View.GONE
            llQueueMode.visibility = View.VISIBLE
            updateQueueDisplay()
        } else {
            llEditMode.visibility = View.VISIBLE
            llQueueMode.visibility = View.GONE
        }
    }

    private fun updateQueueDisplay() {
        if (playQueue.isEmpty()) return
        
        val actualIndex = totalIndex % playQueue.size
        val currentRound = (totalIndex / playQueue.size) + 1

        tvCurrentPlayer.text = playQueue[actualIndex]
        tvQueueTurn.text = getString(R.string.round_number, currentRound)

        btnPrev.isEnabled = totalIndex > 0
        btnNext.isEnabled = true
        
        btnPrev.alpha = if (btnPrev.isEnabled) 1.0f else 0.3f
        btnNext.alpha = 1.0f
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("playersList", ArrayList(playersList))
        outState.putStringArrayList("playQueue", ArrayList(playQueue))
        outState.putBoolean("isDeleteMode", isDeleteMode)
        outState.putBoolean("isPlaying", isPlaying)
        outState.putInt("totalIndex", totalIndex)
    }

    inner class PlayerAdapter(
        private val players: MutableList<String>
    ) : RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {

        var isDeleteMode = false
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvPlayerName)
            val ivDelete: ImageView = view.findViewById(R.id.ivDelete)

            init {
                view.setOnClickListener {
                    if (isDeleteMode) {
                        val pos = adapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            players.removeAt(pos)
                            notifyItemRemoved(pos)
                            notifyItemRangeChanged(pos, players.size)
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_player, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvName.text = players[position]
            holder.ivDelete.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
        }

        override fun getItemCount() = players.size
    }
}