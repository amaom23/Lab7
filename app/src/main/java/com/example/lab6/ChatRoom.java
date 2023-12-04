package com.example.lab6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lab6.databinding.ActivityChatRoomBinding;
import com.example.lab6.databinding.ReceiveMessageBinding;
import com.example.lab6.databinding.SentMessageBinding;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import Data.ChatRoomViewModel;

public class ChatRoom extends AppCompatActivity {

    ActivityChatRoomBinding binding;
    ArrayList<ChatMessage> messages;
    ChatRoomViewModel chatModel;

    private ChatMessageDAO mDAO;
    private RecyclerView.Adapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));


        // Open the Room Database
        MessageDatabase db = Room.databaseBuilder(getApplicationContext(),
                MessageDatabase.class, "database-name").build();
        mDAO = db.cmDAO();

        chatModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        // Retrieves the ArrayList from the ViewModel
        messages = chatModel.messages.getValue();

        if (messages == null) {
            chatModel.messages.setValue(messages = new ArrayList<>());

            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                messages.addAll(mDAO.getAllMessages());
                runOnUiThread(() -> binding.recycleView.setAdapter(myAdapter));
                //loads the recycler viewer
            });
        }

        binding.sendButton.setOnClickListener(click -> {
            String typedMessage = binding.textInput.getText().toString();

            if (!typedMessage.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd-MMM-yyyy hh-mm-ss a");
                String currentDateandTime = sdf.format(new Date());

                ChatMessage chatMessage = new ChatMessage(typedMessage, currentDateandTime, true);
                //Insert new message into database
                Executor thread = Executors.newSingleThreadExecutor();
                thread.execute(() -> {
                 mDAO.insertMessage(chatMessage);
                });

                // Add the ChatMessage to the list
             messages.add(chatMessage);
             chatModel.messages.postValue(messages);

             // Notify the adapter about the new item
             myAdapter.notifyItemInserted(messages.size() - 1);

            // Clear the previous text
             binding.textInput.setText("");
            } else {
            // Handle the case when the message is empty
            }
            });

        binding.receiveButton.setOnClickListener(click -> {

            String receivedMessage = binding.textInput.getText().toString();

            if (!receivedMessage.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd-MMM-yyyy hh-mm-ss a");
                String currentDateandTime = sdf.format(new Date());

                ChatMessage chatMessage = new ChatMessage(receivedMessage, currentDateandTime, false);
                //Insert new message into database
                Executor thread = Executors.newSingleThreadExecutor();
                thread.execute(() -> {
                    mDAO.insertMessage(chatMessage);
                });

                // Add the ChatMessage to the list
                messages.add(chatMessage);
                chatModel.messages.postValue(messages);


                myAdapter.notifyItemInserted(messages.size() - 1);

                // Clear the previous text
                binding.textInput.setText("");
            } else {

            }
        });

        binding.recycleView.setAdapter(myAdapter = new RecyclerView.Adapter<MyRowHolder>() {
            @NonNull
            @Override
            public MyRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == 0) {
                    SentMessageBinding binding = SentMessageBinding.inflate(getLayoutInflater());
                    return new MyRowHolder(binding.getRoot());
                } else {
                    ReceiveMessageBinding binding = ReceiveMessageBinding. inflate(getLayoutInflater());
                    return new MyRowHolder (binding.getRoot());
                }
            }

            @Override
            public void onBindViewHolder(@NonNull MyRowHolder holder, int position) {
                holder.timeText.setText(messages.get(position).getTimeSent());
                String obj =messages.get(position).getMessage();
                holder.messageText.setText(obj);
            }

            //returns the number of rows in the list
            @Override
            public int getItemCount() {
                return messages.size();
            }
            // returns an int which is the parameter which gets passed in to the onCreateViewHolder
            @Override
            public int getItemViewType(int position) {
                ChatMessage chatMessage = messages.get(position);
                return chatMessage.isSendorReceive() ? 0 : 1;
                //Return 0 if the chatMessage is sent, and 1 if it is received
            }

        });

        binding.recycleView.setAdapter(myAdapter);
    }



     class MyRowHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        public MyRowHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.timeText);
            // Finds the ID of the TextView for time sent

            itemView.setOnClickListener(clk -> {
                int position = getAbsoluteAdapterPosition();
                //shows which row position this row is currently in the adapter object

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoom.this);
                builder.setMessage("Do you want to delete this message: " +messageText.getText());
                builder.setTitle("Question: ");


                //if you choose not to delete the message
                builder.setNegativeButton("No",(dialog, cl) -> { });

                //if you choose to delete the message
                builder.setPositiveButton("Yes",(dialog, cl) -> {


                    ChatMessage m= messages.get(position);

                    Executor thread = Executors.newSingleThreadExecutor();
                    thread.execute(() ->{
                        mDAO.deleteMessage(m);
                     });

                     messages.remove(position);
                     myAdapter.notifyItemRemoved(position);


                    Snackbar.make(messageText, "You deleted message #" + position, Snackbar.LENGTH_LONG)
                    .setAction("Undo", v -> {

                        messages.add(position,m);
                        thread.execute(() ->{

                            mDAO.insertMessage(m);
                        });


                            myAdapter.notifyItemInserted(position);
                        }).show();

                });

                builder.create().show();

                 });

        }
    }
}