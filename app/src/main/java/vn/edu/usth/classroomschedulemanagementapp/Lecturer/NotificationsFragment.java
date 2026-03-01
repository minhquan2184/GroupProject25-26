package vn.edu.usth.classroomschedulemanagementapp.Lecturer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.usth.classroomschedulemanagementapp.R;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Nạp layout notification.xml
        View view = inflater.inflate(R.layout.notification, container, false);

        // 2. Ánh xạ các thành phần từ XML
        tvTitle = view.findViewById(R.id.tvTitle);
        recyclerView = view.findViewById(R.id.recyclerNotification);

        // 3. Thiết lập tiêu đề (Nếu cần thay đổi động)
        if (tvTitle != null) {
            tvTitle.setText("Notifications");
        }

        // 4. Cấu hình RecyclerView để hiển thị danh sách
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            /* Ghi chú: Sau này khi bạn tạo NotificationAdapter, hãy bỏ comment dòng dưới:
               NotificationAdapter adapter = new NotificationAdapter(yourDataList);
               recyclerView.setAdapter(adapter);
             */
        }

        return view;
    }
}
