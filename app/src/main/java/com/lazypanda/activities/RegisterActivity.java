package com.lazypanda.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;
import android.os.VibrationEffect;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lazypanda.R;
import com.lazypanda.databinding.ActivityRegisterBinding;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

	private ActivityRegisterBinding binding;
	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityRegisterBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		mAuth = FirebaseAuth.getInstance();

		binding.registerBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String email = binding.emailInput.getText().toString().trim();
				String phoneNumber = binding.phnNInput.getText().toString().trim();
				String name = binding.nameInput.getText().toString().trim();
				String password = binding.passwordInput.getText().toString().trim();
				if (email.isEmpty()) {
					vibrateNShareOnError(binding.emailInput);
				} else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
					vibrateNShareOnError(binding.emailInput);
					binding.emailInput.setError("Invalid Email");
					binding.emailInput.setFocusable(true);
				} else if (phoneNumber.isEmpty()) {
					vibrateNShareOnError(binding.phnNInput);
				} else if (name.isEmpty()) {
					vibrateNShareOnError(binding.nameInput);
				} else if (password.isEmpty()) {
					vibrateNShareOnError(binding.passwordInput);
				} else if (password.length() < 6) {
					vibrateNShareOnError(binding.passwordInput);
					binding.passwordInput.setError("Must Be >6");
					binding.passwordInput.setFocusable(true);
				} else {
					registerUser(email, password);
				}
			}
		});
	}

	// register user with email password
	public void registerUser(String email, String password) {
		mAuth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							FirebaseUser user = mAuth.getCurrentUser();

							String email = user.getEmail();
							String uId = user.getUid();
							HashMap<Object, String> data = new HashMap<>();

							data.put("email", email);
							data.put("uId", uId);
							data.put("name", binding.nameInput.getText().toString().trim());
							data.put("phoneN", binding.phnNInput.getText().toString().trim());
							data.put("image", "");
                            data.put("onlineStatus", "online");
                            data.put("typingStatus", "noOne");
							data.put("password", password);

							FirebaseDatabase database = FirebaseDatabase.getInstance();
							DatabaseReference databaseRefer = database.getReference("Users");
							databaseRefer.child(uId).setValue(data);
							startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
							finish();
						} else {
							Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG)
									.show();
						}
					}
				});
	}

	//show shake effect and vibration effect when some thing went wrong
	private void vibrateNShareOnError(View view) {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(VibrationEffect.createOneShot(256, VibrationEffect.DEFAULT_AMPLITUDE));
		YoYo.with(Techniques.Shake).duration(700).playOn(view);
	}

	// going to previous activity
	public void goBack(View v) {
		onBackPressed();
	}

	// goto login screen
	public void goToLogin(View v) {
		Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		// startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.binding = null;
	}
}