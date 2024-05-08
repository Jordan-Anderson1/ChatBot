package com.example.chatbot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ChatActivity extends AppCompatActivity {


    EditText editText;
    ImageButton submitButton;
    FirebaseFirestore fStore;

    JSONArray chatHistory;

    JSONObject myjsonObject;
    RecyclerView recyclerView;
    ChatAdapter chatAdapter;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //get Username from intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String welcomeMessage = "welcome, " + username;

        editText = findViewById(R.id.editText);
        submitButton = findViewById(R.id.submitButton);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(recyclerView);
        recyclerView.setAdapter(chatAdapter);



        //set Welcome message


        //Initialise JSON variables
        chatHistory = new JSONArray();
        myjsonObject = new JSONObject();

        //initialise Firestore
        fStore = FirebaseFirestore.getInstance();


        //get chat history from firestore and set chat history JSONArray to chat history.
        fStore.collection("users").document(username)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                       String jsonString = documentSnapshot.getString("content");
                       if(jsonString != null){
                           try{
                               chatHistory = new JSONArray(jsonString);
                               chatAdapter.setChatHistory(chatHistory);
                               Log.d("JSON", "The chat history is: " + chatHistory);
                           } catch (JSONException e) {
                               throw new RuntimeException(e);
                           }
                       }
                       else{
                           Log.d("JSON", "The chat history is: " + chatHistory);
                       }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d("JSON", "Unable to obtain chat history. The chat history is: " + chatHistory);
                    }
                });



        //handle logic for submitting chat. adds message and chat history to JSON object for API post. Adds message
        //and response to chat history and updates Firestore.
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = editText.getText().toString();
                editText.setText("");

                //API endpoint
                String url = "http://10.0.2.2:5000/chat";

                RequestQueue requestQueue = Volley.newRequestQueue(ChatActivity.this);

                //add user message and chat history to JSON object
                try {
                    myjsonObject.put("userMessage", message);
                    myjsonObject.put("chatHistory", chatHistory);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                //send API request
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, myjsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                        try {
                            String response = jsonObject.getString("message");

                            JSONObject temp = new JSONObject();
                            temp.put("User", message);
                            temp.put("Llama", jsonObject.getString("message"));
                            chatHistory.put(temp);
                            myjsonObject.put("chatHistory", chatHistory);

                            Log.d("JSON", "before fireStore");

                            Map<String, Object> json = new HashMap<>();
                            json.put("content", chatHistory.toString());

                            fStore.collection("users").document(username)
                                    .set(json)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("JSON", "myJSONObject added to db");
                                            chatAdapter.setChatHistory(chatHistory);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("JSON", "myJSONObject was NOT added to the db");
                                        }
                                    });

                            Log.d("JSON", "after fireStore");


                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d("MAIN", "API POST ERR");
                    }
                });

                requestQueue.add(jsonObjectRequest);


            }
        });


    }
}