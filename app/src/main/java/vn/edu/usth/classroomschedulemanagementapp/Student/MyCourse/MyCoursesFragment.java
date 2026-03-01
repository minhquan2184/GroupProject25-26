package vn.edu.usth.classroomschedulemanagementapp.Student.MyCourse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction; // Import transaction
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.RetrofitClient;
import vn.edu.usth.classroomschedulemanagementapp.Student.AllCourse.Subject;
import vn.edu.usth.classroomschedulemanagementapp.Student.MyCourse.CourseDetail.CourseDetailFragment; // Import Fragment mới

public class MyCoursesFragment extends Fragment {

    RecyclerView rcvCourse;
    MyCoursesAdapter adapter;
    List<Subject> courseList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.student_my_course, container, false);
        rcvCourse = view.findViewById(R.id.rcvCourse);

        rcvCourse.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup Adapter với sự kiện Click
        adapter = new MyCoursesAdapter(courseList, new MyCoursesAdapter.OnItemClickListener() {
            @Override
            public void onDetailClick(Subject subject) {
                // Tạo Fragment mới với dữ liệu truyền vào
                CourseDetailFragment detailFragment = CourseDetailFragment.newInstance(subject.getId(), subject.getName());

                // Thực hiện chuyển đổi Fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                // R.id.fragment_container là ID của FrameLayout trong StudentMainActivity
                transaction.replace(R.id.fragment_container, detailFragment);

                // Quan trọng: Thêm vào BackStack để bấm nút Back điện thoại sẽ quay lại list
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });

        rcvCourse.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMyCourses();
    }

    private void fetchMyCourses() {
        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", "");

        if (userId.isEmpty()) {
            Toast.makeText(getContext(), "Isn't login yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getService().getMyCourses(userId).enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courseList.clear();
                    courseList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Toast.makeText(getContext(), "Connection error!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
