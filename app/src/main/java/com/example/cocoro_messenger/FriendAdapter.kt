package com.example.cocoro_messenger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendAdapter(private val friends: MutableList<Friends>) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.friend_name)
        val emailTextView: TextView = itemView.findViewById(R.id.friend_email)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_list_item, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.nameTextView.text = friend.name
        holder.emailTextView.text = friend.email
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    fun addFriend(friend: Friends) {
        friends.add(friend)
        notifyItemInserted(friends.size - 1)
    }

    fun updateFriends(newFriends: List<Friends>) {
        friends.clear()
        friends.addAll(newFriends)
        notifyDataSetChanged()
    }
}