package com.lazypanda.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazypanda.R;
import com.lazypanda.models.ModelChat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChats extends RecyclerView.Adapter<AdapterChats.ChatHolder> {

    private static final int MESSAGE_TYPE_LEFT = 0;
    private static final int MESSAGE_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    private List<Integer> selectedMessages = new ArrayList<>();
    String imageUrl;
    private Dialog dialog;
    FirebaseUser fUser;

    public AdapterChats(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @Override
    public com.lazypanda.adapters.AdapterChats.ChatHolder onCreateViewHolder(
            @NonNull ViewGroup viewGroup, int i) {
        if (i == MESSAGE_TYPE_RIGHT) {
            View view =
                    LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
            return new ChatHolder(view);
        } else {
            View view =
                    LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new ChatHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterChats.ChatHolder chatHolder, int i) {
        String message = chatList.get(i).getMessage();
        String timeStamp = chatList.get(i).getTimeStamp();
        long timeStampMills = Long.parseLong(timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date result = new Date(timeStampMills);
        String time = sdf.format(result);
        chatHolder.user_message_view.setText(message);
        chatHolder.showTimeStamp.setText(time);

        try {
            Glide.with(context).load(imageUrl).into(chatHolder.user_image_view);
        } catch (Exception err) {
        }

        if (i == chatList.size() - 1) {
            if (chatList.get(i).getIsSeen()) {
                chatHolder.textSeenOrNot.setText("Seen");
            } else {
                chatHolder.textSeenOrNot.setText("Delivered");
            }
        } else {
            //chatHolder.textSeenOrNot.setVisibility(View.GONE);
        }

        chatHolder.showMessagesLayout.setOnClickListener(
                v -> {
                    String userMsg = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    if (chatList.get(i).getSender().equals(userMsg)) {
                        showDeleteDialog(i);
                    }
                });
        
        if(selectedMessages.contains(i)) {
        	chatHolder.itemView.setBackgroundColor(Color.parseColor("#1a808080"));
        } else {
            chatHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void showDeleteDialog(int position) {
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.delete_dialog_bg);
        dialog.setContentView(R.layout.layout_delete_messages_dialog);
        dialog.setCanceledOnTouchOutside(false);
        Button cancelDialogBtn = dialog.findViewById(R.id.cancelDeleteMsgBtn);
        Button deleteMsgBtn = dialog.findViewById(R.id.deleteMessageBtn);
        cancelDialogBtn.setOnClickListener(
                cancelDialog -> {
                    dialog.dismiss();
                });
        deleteMsgBtn.setOnClickListener(
                deleteMsg -> {
                    deleteMessages(position);
                });
        dialog.show();
    }

    private void deleteMessages(int position) {
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String messageTimeStamp = chatList.get(position).getTimeStamp();
        DatabaseReference dbRefer = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRefer.orderByChild("timeStamp").equalTo(messageTimeStamp);
        query.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapShot) {
                        for (DataSnapshot ds : snapShot.getChildren()) {
                            String senderUID = ds.child("sender").getValue(String.class);
                            if (senderUID != null && senderUID.equals(currentUser)) {
                                if (ds.child("message").exists()) {
                                    HashMap<String, Object> deleteMap = new HashMap<>();
                                    deleteMap.put("message", "This Message Was Deleted !!!");
                                    ds.getRef().updateChildren(deleteMap);
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(
                                                    context,
                                                    "message does not exist",
                                                    Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError arg0) {}
                });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MESSAGE_TYPE_RIGHT;
        } else {
            return MESSAGE_TYPE_LEFT;
        }
    }

    class ChatHolder extends RecyclerView.ViewHolder {

        ImageView user_image_view;
        TextView user_message_view, showTimeStamp, textSeenOrNot;
        LinearLayout showMessagesLayout;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);
            user_image_view = itemView.findViewById(R.id.chat_user_image);
            user_message_view = itemView.findViewById(R.id.chat_user_message);
            showTimeStamp = itemView.findViewById(R.id.txtTimeStamp);
            showMessagesLayout = itemView.findViewById(R.id.messageLayout);
            textSeenOrNot = itemView.findViewById(R.id.textSeen);
        }
    }
}
