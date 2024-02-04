package com.lazypanda.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lazypanda.R;
import com.lazypanda.activities.ChatActivity;
import com.lazypanda.models.ModelUsers;
import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.UserHolder> {
    
    Context context;
    List<ModelUsers> usersList;
    
    public AdapterUser(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }
    
    @Override
    public int getItemCount() {
        return usersList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUser.UserHolder userHolder, int i) {
        final String hisUID = usersList.get(i).getUId();
        String userImage = usersList.get(i).getImage();
        String userName = usersList.get(i).getName();
        //String userEmail = usersList.get(i).getEmail();
        
        userHolder.row_user_name_tv.setText(userName);
        //String maskedEmail = maskEmail(userEmail);
        //userHolder.row_user_email_tv.setText(maskedEmail);
        try {
        	Glide.with(context).load(userImage).diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop().into(userHolder.row_user_imagev);
        } catch(Exception err) {
        	Glide.with(context).load(R.drawable.p).diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop().into(userHolder.row_user_imagev);
        }
        
        userHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, ""+userName, Toast.LENGTH_LONG).show(); 
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUID);
                intent.putExtra("hisName", userName);
                intent.putExtra("hisImage", userImage);   
                context.startActivity(intent);
            }
        });
    }

    /*public static String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length == 2) {
            String maskedName = parts[0].substring(0, Math.min(5, parts[0].length())) + "*********";
            return maskedName + "@" + parts[1];
        }
        return email; // Handle invalid emails gracefully
    }*/

    @NonNull
    @Override
    public com.lazypanda.adapters.AdapterUser.UserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);
        return new UserHolder(view);
    }
    
    class UserHolder extends RecyclerView.ViewHolder {
        
        ImageView row_user_imagev;
        TextView row_user_name_tv, row_user_email_tv;
        
        public UserHolder(View itemView) {
            super(itemView);
            
            row_user_imagev = itemView.findViewById(R.id.row_users_image_view);
            row_user_name_tv = itemView.findViewById(R.id.row_users_name);
            //row_user_email_tv = itemView.findViewById(R.id.row_users_email);
        }    
    }

}
