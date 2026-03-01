package vn.edu.usth.classroomschedulemanagementapp.Lecturer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.classroomschedulemanagementapp.Calendar.CalendarFragment;
import vn.edu.usth.classroomschedulemanagementapp.NotificationsFragment;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.RetrofitClient;
import vn.edu.usth.classroomschedulemanagementapp.Student.Account.UserProfile;

public class LecturerMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        btnMenu = findViewById(R.id.btn_menu);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // --- XỬ LÝ HEADER GIẢNG VIÊN ---
        if (navigationView.getHeaderCount() > 0) {
            View headerView = navigationView.getHeaderView(0);
            updateLecturerHeader(headerView);

            headerView.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                // Chuyển sang fragment hồ sơ giảng viên
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LecturerAccountFragment())
                        .addToBackStack(null)
                        .commit();

                // Bỏ chọn menu
                int size = navigationView.getMenu().size();
                for (int i = 0; i < size; i++) {
                    navigationView.getMenu().getItem(i).setChecked(false);
                }
            });
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_calender) {
                selectedFragment = new CalendarFragment();
            } else if (id == R.id.nav_my_courses) {
                selectedFragment = new LecturerDashboardFragment();
            } else if (id == R.id.nav_notifications) {
                selectedFragment = new NotificationsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null)
                        .commit();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CalendarFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_calender);
        }
    }

    private void updateLecturerHeader(View headerView) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", "");

        TextView tvName = headerView.findViewById(R.id.text_user_name);
        TextView tvId = headerView.findViewById(R.id.tv_user_id);
        TextView tvDept = headerView.findViewById(R.id.tv_user_major);

        if (userId.isEmpty()) {
            return;
        }

        // Sử dụng chung API getProfile vì server trả về linh hoạt theo userId
        RetrofitClient.getService().getProfile(userId).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();
                    if (tvName != null) {
                        tvName.setText(profile.getFullName());
                    }
                    if (tvId != null) {
                        tvId.setText("Staff ID: " + profile.getStudentCode());
                    }
                    if (tvDept != null) {
                        tvDept.setText("Dept: " + profile.getMajor());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
            }
        });
    }
}
