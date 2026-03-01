package vn.edu.usth.classroomschedulemanagementapp.Student;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.classroomschedulemanagementapp.Calendar.CalendarFragment;
import vn.edu.usth.classroomschedulemanagementapp.NotificationsFragment;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.RetrofitClient;
import vn.edu.usth.classroomschedulemanagementapp.Student.Account.AccountInfoFragment;
import vn.edu.usth.classroomschedulemanagementapp.Student.Account.UserProfile;
import vn.edu.usth.classroomschedulemanagementapp.Student.AllCourse.AllCoursesFragment;
import vn.edu.usth.classroomschedulemanagementapp.Student.MyCourse.MyCoursesFragment;

public class StudentMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnOpenDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        btnOpenDrawer = findViewById(R.id.btn_menu);

        // Xử lý nút mở menu (Drawer)
        btnOpenDrawer.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        // --- XỬ LÝ HEADER (Click & Hiển thị thông tin) ---
        if (navigationView.getHeaderCount() > 0) {
            View headerView = navigationView.getHeaderView(0);

            // 1. Gọi hàm cập nhật thông tin lên Header từ API
            updateHeaderInfo(headerView);

            // 2. Bắt sự kiện click vào Header để mở AccountInfo
            headerView.setOnClickListener(v -> {
                // Đóng menu
                drawerLayout.closeDrawer(GravityCompat.START);

                // Chuyển sang màn hình Account Info
                replaceFragment(new AccountInfoFragment());

                // Bỏ chọn các item ở menu dưới (để người dùng biết đang ở màn hình khác)
                int size = navigationView.getMenu().size();
                for (int i = 0; i < size; i++) {
                    navigationView.getMenu().getItem(i).setChecked(false);
                }
            });
        }

        // Mặc định mở màn hình Calendar khi vào App
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CalendarFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_calender);
        }
    }

    // Hàm gọi API và điền thông tin vào Header
    private void updateHeaderInfo(View headerView) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", "");

        // Ánh xạ các View trong Header (đảm bảo ID khớp với file nav_header_main.xml)
        TextView tvName = headerView.findViewById(R.id.text_user_name);
        TextView tvId = headerView.findViewById(R.id.tv_user_id);
        TextView tvMajor = headerView.findViewById(R.id.tv_user_major);

        // Lấy tên tạm từ SharedPreferences (hiển thị trước khi API tải xong)
        String savedName = prefs.getString("FULL_NAME", "Student");
        if (tvName != null) {
            tvName.setText(savedName);
        }

        if (userId.isEmpty()) {
            return;
        }

        // Gọi API lấy Profile (để có Major và StudentCode chính xác)
        RetrofitClient.getService().getProfile(userId).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile user = response.body();

                    // Cập nhật giao diện với dữ liệu thật từ Server
                    if (tvName != null) {
                        tvName.setText(user.getFullName());
                    }
                    if (tvId != null) {
                        tvId.setText("ID: " + user.getStudentCode());
                    }
                    if (tvMajor != null) {
                        tvMajor.setText("Major: " + user.getMajor());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                // Nếu lỗi mạng, giữ nguyên tên đã load từ SharedPreferences
                // Có thể log lỗi ra Logcat nếu cần: t.printStackTrace();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_calender) {
            replaceFragment(new CalendarFragment());
        } else if (id == R.id.nav_all_courses) {
            replaceFragment(new AllCoursesFragment());
        } else if (id == R.id.nav_my_courses) {
            replaceFragment(new MyCoursesFragment());
        } else if (id == R.id.nav_notif) {
            replaceFragment(new NotificationsFragment());
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        // Thêm vào back stack để nút Back điện thoại hoạt động đúng
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
