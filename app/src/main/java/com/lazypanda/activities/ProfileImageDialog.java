package com.lazypanda.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.lazypanda.R;

public class ProfileImageDialog extends Dialog {

    private Context context;
    private String imageResource;

    public ProfileImageDialog(@NonNull Context context, String imageResource) {
        super(context);
        this.context = context;
        this.imageResource = imageResource;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.show_profile_img_dialog);

        ImageView imageViewDialog = findViewById(R.id.imageViewDialog);
        DrawableCrossFadeFactory dcff = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
        Glide.with(context).load(imageResource).diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop().transition(DrawableTransitionOptions.withCrossFade(dcff)).into(imageViewDialog);
        
        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageViewDialog);
        photoViewAttacher.update();

        // Set the dialog size based on the screen dimensions (adjust as needed).
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
}
