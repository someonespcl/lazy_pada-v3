package com.lazypanda.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;
import android.os.VibrationEffect;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lazypanda.R;
import com.lazypanda.databinding.ActivityLoginBinding;
import com.lazypanda.utils.CustomLoadingDialog;

public class LoginActivity extends AppCompatActivity {

	private ActivityLoginBinding binding;
	private FirebaseAuth mAuth;
	private SharedPreferences sharedPreferences;
	private static final String LOCAL_CREDENTIALS = "localCredentials";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_REMEMBER_ME = "rememberMe";
	private CheckBox checkBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityLoginBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		mAuth = FirebaseAuth.getInstance();

		checkBox = findViewById(R.id.rememberMe);

		// sharedPreferences = getApplicationContext().getPreferences(LOCAL_CREDENTIALS,
		// Context.MODE_PRIVATE);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
		checkBox.setChecked(rememberMe);

		if (rememberMe) {
			String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
			String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
			binding.eInput.setText(savedEmail);
			binding.passInput.setText(savedPassword);
		}
	}

	// login user
	public void loginUser(View v) {
		String email = binding.eInput.getText().toString().trim();
		String password = binding.passInput.getText().toString().trim();
		if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			vibrateNShareOnError(binding.eInput);
			binding.eInput.setError("Invalid Email");
			binding.eInput.setFocusable(true);
		} else if (email.isEmpty()) {
			vibrateNShareOnError(binding.eInput);
		} else if (password.isEmpty()) {
			vibrateNShareOnError(binding.passInput);
		} else if (password.length() < 6) {
			vibrateNShareOnError(binding.passInput);
			binding.passInput.setError("Must Be >6");
			binding.passInput.setFocusable(true);
		} else {
			CustomLoadingDialog.showLoadingDialog(LoginActivity.this);
			loginUserWithEmailPassword(email, password);
		}
	}

	// login user with email
	private void loginUserWithEmailPassword(String email, String password) {
		if (checkBox.isChecked()) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(KEY_EMAIL, email);
			editor.putString(KEY_PASSWORD, password);
			editor.putBoolean(KEY_REMEMBER_ME, true);
			editor.apply();
		} else {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.remove(KEY_EMAIL);
			editor.remove(KEY_PASSWORD);
			editor.putBoolean(KEY_REMEMBER_ME, false);
			editor.apply();
		}
		mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
				new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							FirebaseUser user = mAuth.getCurrentUser();
							if (user != null) {
								CustomLoadingDialog.hideLoadingDialog();
								startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
							} else {
								CustomLoadingDialog.hideLoadingDialog();
								Toast.makeText(LoginActivity.this, "User Does Not Exist.", Toast.LENGTH_LONG).show();
							}
						} else {
							// If sign in fails, display a message to the user.
							Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	// forget password
	public void forgetPassword(View v) {
		String email = binding.eInput.getText().toString().trim();
		if (email.isEmpty()) {
			vibrateNShareOnError(binding.eInput);
		} else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			vibrateNShareOnError(binding.eInput);
			binding.eInput.setError("Invalid Email");
			binding.eInput.setFocusable(true);
		} else {
			CustomLoadingDialog.showLoadingDialog(LoginActivity.this);
			passwordRecover(email);
		}
	}

	// send forget password email
	private void passwordRecover(String email) {
		mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, new OnCompleteListener<Void>() {
			@Override
			public void onComplete(Task<Void> task) {
				if (task.isSuccessful()) {
					CustomLoadingDialog.hideLoadingDialog();
					Toast.makeText(LoginActivity.this, "Forget password email link sent Successfully.",
							Toast.LENGTH_LONG).show();
				} else {
					CustomLoadingDialog.hideLoadingDialog();
					Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
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

	// go to previous activity
	public void goBack(View v) {
		onBackPressed();
	}

	// goto register
	public void goToRegister(View v) {
		Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		// startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.binding = null;
	}
}