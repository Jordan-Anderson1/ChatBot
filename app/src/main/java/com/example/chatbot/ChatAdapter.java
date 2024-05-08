package com.example.chatbot;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private JSONArray chatHistoryArray;
    RecyclerView recyclerView;

    public ChatAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void setChatHistory(JSONArray chatHistory){
        this.chatHistoryArray = chatHistory;
        notifyDataSetChanged();
        scrollToLastItem();
    }

    // Method to scroll to the last item in the RecyclerView
    private void scrollToLastItem() {
        if (chatHistoryArray.length() > 0) {
            recyclerView.smoothScrollToPosition(chatHistoryArray.length() - 1);
        }
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {

        try {
            JSONObject messageObject = chatHistoryArray.getJSONObject(position);
            String llamaMessage = messageObject.getString("Llama");
            String userMessage = messageObject.getString("User");

            holder.llamaText.setText(llamaMessage);
            holder.userText.setText(userMessage);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getItemCount() {
        return (chatHistoryArray != null) ? chatHistoryArray.length() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView llamaText, userText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            llamaText = itemView.findViewById(R.id.llamaText);
            userText = itemView.findViewById(R.id.userText);
        }
    }
}
