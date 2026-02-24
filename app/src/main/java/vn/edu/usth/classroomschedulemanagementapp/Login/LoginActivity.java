package vn.edu.usth.classroomschedulemanagementapp.Login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.classroomschedulemanagementapp.Lecturer.LecturerMainActivity;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.RetrofitClient;
import vn.edu.usth.classroomschedulemanagementapp.Student.StudentMainActivity;
import vn.edu.usth.classroomschedulemanagementapp.User;

public class LoginActivity extends AppCompatActivity {

    private EditText emailBox;
    private EditText passwordBox;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        emailBox = findViewById(R.id.email_box);
        passwordBox = findViewById(R.id.password_box);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = emailBox.getText().toString().trim();
        String password = passwordBox.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // request
        LoginRequest request = new LoginRequest(email, password);

        RetrofitClient.getService().login(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String role = user.getRole();

                    SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("USER_ID", user.getId());
                    editor.putString("USER_NAME", user.getFullName());
                    editor.putString("USER_ROLE", role);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Welcome " + user.getFullName(), Toast.LENGTH_SHORT).show();

                    navigateBasedOnRole(role);

                } else {
                    Toast.makeText(LoginActivity.this, "Login failed! Check credentials.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("LoginError", t.getMessage());
            }
        });
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;

        if ("STUDENT".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, StudentMainActivity.class);

        } else if ("LECTURER".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, LecturerMainActivity.class);

        } else {
            Toast.makeText(this, "Who are you???!!!: " + role, Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(intent);
        finish();
    }
}
