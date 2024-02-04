package com.lazypanda.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lazypanda.adapters.AdapterChats;
import com.lazypanda.databinding.ActivityChatBinding;
import com.lazypanda.R;
import com.lazypanda.models.ModelChat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseDatabase fDatabase;
    private DatabaseReference usersRefernce, userForSeenRefer;

    ValueEventListener seenListener;
    private MediaPlayer sendMsgSound;

    List<ModelChat> chatList;
    AdapterChats adapterChat;

    String myUid;
    String hisUid;
    String hisImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        usersRefernce = fDatabase.getReference("Users");
        
        sendMsgSound = MediaPlayer.create(ChatActivity.this, R.raw.sent);
        
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");
        hisImage = intent.getStringExtra("hisImage");
        String hisName = intent.getStringExtra("hisName");
        binding.showUsersName.setText(hisName);

        binding.showUserChatProfile.setOnClickListener(
                v -> {
                    ProfileImageDialog showProfileImg =
                            new ProfileImageDialog(ChatActivity.this, hisImage);
                    showProfileImg.show();
                });

        try {
            Glide.with(ChatActivity.this).load(hisImage).into(binding.showUserChatProfile);
        } catch (Exception e) {
            Glide.with(ChatActivity.this).load(R.drawable.p).into(binding.showUserChatProfile);
        }

        binding.sendMessage.setOnClickListener(
                v -> {
                    String enteredMessage = binding.enterMessage.getText().toString().trim();
                    if (enteredMessage.isEmpty()) {
                        Toast.makeText(
                                        ChatActivity.this,
                                        "Cannot send Empty Message",
                                        Toast.LENGTH_LONG)
                                .show();
                    } else {
                        sendEnteredMessage(enteredMessage);
                    }
                });

        binding.enterMessage.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence arg0, int arg1, int arg2, int arg3) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(count > 0) {
                        	
                        } else {
                            
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable arg0) {}
                });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        binding.showMessages.setLayoutManager(linearLayoutManager);
        binding.showMessages.smoothScrollToPosition(0);
        showMessages();
        seenMessages();
    }

    private void showMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRefer = FirebaseDatabase.getInstance().getReference("Chats");
        dbRefer.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapShot) {
                        chatList.clear();
                        for (DataSnapshot ds : dataSnapShot.getChildren()) {
                            ModelChat chat = ds.getValue(ModelChat.class);
                            if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)
                                    || chat.getReceiver().equals(hisUid)
                                            && chat.getSender().equals(myUid)) {
                                chatList.add(chat);
                            }
                        }
                        adapterChat = new AdapterChats(ChatActivity.this, chatList, hisImage);
                        adapterChat.notifyDataSetChanged();
                        binding.showMessages.setAdapter(adapterChat);
                        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
                        itemAnimator.setAddDuration(500);
                        itemAnimator.setRemoveDuration(500);
                        itemAnimator.setChangeDuration(500);
                        binding.showMessages.setItemAnimator(itemAnimator);
                        binding.showMessages.scrollToPosition(adapterChat.getItemCount() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    // sending the messages to the user
    private void sendEnteredMessage(String message) {
        String timeStamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference databaseRefer = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> msgMap = new HashMap<>();
        msgMap.put("sender", myUid);
        msgMap.put("receiver", hisUid);
        msgMap.put("message", message);
        msgMap.put("timeStamp", timeStamp);
        msgMap.put("isSeen", false);
        databaseRefer.child("Chats").push().setValue(msgMap);
        playSentMessageSound();
        binding.enterMessage.setText(null);
    }
    
    private void playSentMessageSound() {
        if (sendMsgSound != null) {
            sendMsgSound.start();
        }
    }

    private void seenMessages() {
        userForSeenRefer = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener =
                userForSeenRefer.addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    ModelChat mC = ds.getValue(ModelChat.class);
                                    if (mC.getReceiver().equals(myUid)
                                            && mC.getSender().equals(hisUid)) {
                                        HashMap<String, Object> hashSeenMap = new HashMap<>();
                                        hashSeenMap.put("isSeen", true);
                                        ds.getRef().updateChildren(hashSeenMap);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError arg0) {}
                        });
    }

    // check whether the user logged in or not
    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            myUid = user.getUid();
        } else {
            startActivity(new Intent(ChatActivity.this, WelcomeActivity.class));
            finish();
        }
    }

    // check the user status when app starts
    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        userForSeenRefer.removeEventListener(seenListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
