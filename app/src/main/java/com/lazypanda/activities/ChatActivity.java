package com.lazypanda.activities;

import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
                    if (!enteredMessage.isEmpty()) {
                        sendEnteredMessage(enteredMessage);
                    } else {

                        Toast.makeText(
                                        ChatActivity.this,
                                        "Cannot send Empty Message",
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });

        binding.enterMessage.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence arg0, int arg1, int arg2, int arg3) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().trim().length() > 0) {
                            checkTypingStatus(hisUid);
                        } else {
                            checkTypingStatus(null);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        binding.showMessages.setLayoutManager(linearLayoutManager);
        binding.showMessages.smoothScrollToPosition(0);
        showMessages();
        seenMessages();
        updateTypingStatus();
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
        binding.enterMessage.setText(null);
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRefer =
                FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> typingStatus = new HashMap<>();
        typingStatus.put("typingTo", typing);
        dbRefer.updateChildren(typingStatus);
    }
    
    private void updateTypingStatus() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        Query typingRefer = usersRef.child(hisUid); // Query the specific user
        typingRefer.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String typingStatus = snapshot.child("typingTo").getValue(String.class);
                            if (typingStatus != null) {
                                if (typingStatus.equals(myUid)) {
                                    // Show typing indicator for the other user on the right side
                                    binding.typingContainer.setVisibility(View.VISIBLE);
                                    binding.typingRightContainer.setVisibility(View.VISIBLE);
                                    binding.typingLeftContainer.setVisibility(View.GONE);
                                } else if (typingStatus.equals(hisUid)) {
                                    // Show typing indicator for the current user on the left side
                                    binding.typingContainer.setVisibility(View.VISIBLE);
                                    binding.typingLeftContainer.setVisibility(View.VISIBLE);
                                    binding.typingRightContainer.setVisibility(View.GONE);
                                    try {
                                        Glide.with(ChatActivity.this)
                                                .load(hisImage)
                                                .into(binding.typingLeftUserDp);
                                    } catch (Exception e) {

                                    }
                                } else {
                                    // Hide typing indicator
                                    binding.typingContainer.setVisibility(View.GONE);
                                    binding.typingLeftContainer.setVisibility(View.GONE);
                                    binding.typingRightContainer.setVisibility(View.GONE);
                                }
                            } else {
                                // Hide typing indicator
                                binding.typingContainer.setVisibility(View.GONE);
                                binding.typingLeftContainer.setVisibility(View.GONE);
                                binding.typingRightContainer.setVisibility(View.GONE);
                            }
                        } else {
                            // Handle the case when the user does not exist
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle database error
                    }
                });
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
        checkTypingStatus(null);
        if (userForSeenRefer != null && seenListener != null) {
            userForSeenRefer.removeEventListener(seenListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
