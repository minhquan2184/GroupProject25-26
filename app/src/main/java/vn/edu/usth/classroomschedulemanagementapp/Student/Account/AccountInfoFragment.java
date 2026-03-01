package vn.edu.usth.classroomschedulemanagementapp.Student.Account;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.RetrofitClient;
import vn.edu.usth.classroomschedulemanagementapp.Login.LoginActivity;

public class AccountInfoFragment extends Fragment {

    private TextView tvName, tvStudentCode, tvMajor;
    private RecyclerView rcvGrades;
    private GradeAdapter adapter;
    private List<StudentGrade> gradeList;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_info, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvStudentCode = view.findViewById(R.id.tvStudentCode);
        tvMajor = view.findViewById(R.id.tvMajor);
        rcvGrades = view.findViewById(R.id.rcvGrades);
        btnLogout = view.findViewById(R.id.btnLogout);

        rcvGrades.setLayoutManager(new LinearLayoutManager(getContext()));
        gradeList = new ArrayList<>();
        adapter = new GradeAdapter(gradeList);
        rcvGrades.setAdapter(adapter);

        fetchData();
        btnLogout.setOnClickListener(v -> performLogout());
        return view;
    }

    private void performLogout() {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private void fetchData() {
        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", "");
        if (userId.isEmpty()) {
            return;
        }

        // 1. Lấy thông tin cá nhân
        RetrofitClient.getService().getProfile(userId).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile user = response.body();
                    tvName.setText(user.getFullName());
                    tvStudentCode.setText("ID: " + user.getStudentCode());
                    tvMajor.setText("Major: " + user.getMajor());
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
            }
        });

        // 2. Lấy điểm
        RetrofitClient.getService().getGrades(userId).enqueue(new Callback<List<StudentGrade>>() {
            @Override
            public void onResponse(Call<List<StudentGrade>> call, Response<List<StudentGrade>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    gradeList.clear();
                    gradeList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<StudentGrade>> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading grades", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
