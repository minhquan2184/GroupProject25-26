package vn.edu.usth.classroomschedulemanagementapp.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.core.WeekDay;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.kizitonwose.calendar.view.WeekCalendarView;
import com.kizitonwose.calendar.view.WeekDayBinder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.classroomschedulemanagementapp.ApiService;
import vn.edu.usth.classroomschedulemanagementapp.R;
import vn.edu.usth.classroomschedulemanagementapp.RetrofitClient;

public class CalendarFragment extends Fragment {

    private CalendarView monthCalendarView;
    private WeekCalendarView weekCalendarView;
    private TextView tvMonthYear, tvDateSelected;
    private ImageButton btnNext, btnPrev;
    private CheckBox cbToggle;
    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;

    // Data
    private LocalDate selectedDate = LocalDate.now();
    // Map lưu trữ: Ngày -> Danh sách lịch học
    private HashMap<LocalDate, List<Schedule>> database = new HashMap<>();

    public CalendarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Ánh xạ View
        monthCalendarView = view.findViewById(R.id.calendarView);
        weekCalendarView = view.findViewById(R.id.weekCalendarView);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        tvDateSelected = view.findViewById(R.id.tvDateSelected);
        btnNext = view.findViewById(R.id.btnNext);
        btnPrev = view.findViewById(R.id.btnPrevious);
        cbToggle = view.findViewById(R.id.cbToggleWeekMonth);
        recyclerView = view.findViewById(R.id.recyclerViewSchedule);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScheduleAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // --- SETUP CALENDAR VIEW ---
        class MonthDayViewContainer extends ViewContainer {

            TextView textView;
            View dotView, frameLayout;
            CalendarDay day;

            public MonthDayViewContainer(View view) {
                super(view);
                textView = view.findViewById(R.id.calendarDayText);
                dotView = view.findViewById(R.id.calendarDayDot);
                frameLayout = view.findViewById(R.id.exOneDayFrame);
                view.setOnClickListener(v -> {
                    if (day.getPosition() == DayPosition.MonthDate) {
                        selectDate(day.getDate());
                    }
                });
            }
        }

        monthCalendarView.setDayBinder(new MonthDayBinder<MonthDayViewContainer>() {
            @NonNull
            @Override
            public MonthDayViewContainer create(@NonNull View view) {
                return new MonthDayViewContainer(view);
            }

            @Override
            public void bind(@NonNull MonthDayViewContainer container, CalendarDay calendarDay) {
                container.day = calendarDay;
                bindDayView(container.textView, container.frameLayout, container.dotView,
                        calendarDay.getDate(), calendarDay.getPosition() == DayPosition.MonthDate);
            }
        });

        class WeekDayViewContainer extends ViewContainer {

            TextView textView;
            View dotView, frameLayout;
            WeekDay day;

            public WeekDayViewContainer(View view) {
                super(view);
                textView = view.findViewById(R.id.calendarDayText);
                dotView = view.findViewById(R.id.calendarDayDot);
                frameLayout = view.findViewById(R.id.exOneDayFrame);
                view.setOnClickListener(v -> selectDate(day.getDate()));
            }
        }

        weekCalendarView.setDayBinder(new WeekDayBinder<WeekDayViewContainer>() {
            @NonNull
            @Override
            public WeekDayViewContainer create(@NonNull View view) {
                return new WeekDayViewContainer(view);
            }

            @Override
            public void bind(@NonNull WeekDayViewContainer container, WeekDay weekDay) {
                container.day = weekDay;
                bindDayView(container.textView, container.frameLayout, container.dotView,
                        weekDay.getDate(), true);
            }
        });

        // Setup time range
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(24);
        YearMonth endMonth = currentMonth.plusMonths(24);

        monthCalendarView.setup(startMonth, endMonth, DayOfWeek.MONDAY);
        monthCalendarView.scrollToMonth(currentMonth);

        LocalDate startWeek = startMonth.atDay(1);
        LocalDate endWeek = endMonth.atEndOfMonth();
        weekCalendarView.setup(startWeek, endWeek, DayOfWeek.MONDAY);
        weekCalendarView.scrollToWeek(LocalDate.now());

        // Listeners scroll
        monthCalendarView.setMonthScrollListener(calendarMonth -> {
            updateMonthHeader(calendarMonth.getYearMonth());
            return null;
        });

        weekCalendarView.setWeekScrollListener(week -> {
            LocalDate firstDate = week.getDays().get(0).getDate();
            updateMonthHeader(YearMonth.from(firstDate));
            return null;
        });

        // Toggle Week/Month
        cbToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                monthCalendarView.setVisibility(View.GONE);
                weekCalendarView.setVisibility(View.VISIBLE);
                weekCalendarView.scrollToWeek(selectedDate);
            } else {
                weekCalendarView.setVisibility(View.GONE);
                monthCalendarView.setVisibility(View.VISIBLE);
                monthCalendarView.scrollToMonth(YearMonth.from(selectedDate));
            }
        });

        // Navigation Buttons
        btnNext.setOnClickListener(v -> {
            if (monthCalendarView.getVisibility() == View.VISIBLE) {
                CalendarMonth current = monthCalendarView.findFirstVisibleMonth();
                if (current != null) {
                    monthCalendarView.smoothScrollToMonth(current.getYearMonth().plusMonths(1));
                }
            } else {
                weekCalendarView.smoothScrollToWeek(weekCalendarView.findFirstVisibleWeek().getDays().get(0).getDate().plusWeeks(1));
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (monthCalendarView.getVisibility() == View.VISIBLE) {
                CalendarMonth current = monthCalendarView.findFirstVisibleMonth();
                if (current != null) {
                    monthCalendarView.smoothScrollToMonth(current.getYearMonth().minusMonths(1));
                }
            } else {
                weekCalendarView.smoothScrollToWeek(weekCalendarView.findFirstVisibleWeek().getDays().get(0).getDate().minusWeeks(1));
            }
        });

        // Initial Selection
        selectDate(LocalDate.now());

        // GỌI API LẤY DỮ LIỆU THẬT
        fetchScheduleFromApi();

        return view;
    }

    // --- HÀM GỌI API ---
    private void fetchScheduleFromApi() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", "");

        if (userId.isEmpty()) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getService();
        apiService.getStudentSchedule(userId).enqueue(new Callback<List<ScheduleResponse>>() {
            @Override
            public void onResponse(Call<List<ScheduleResponse>> call, Response<List<ScheduleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processScheduleData(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to load schedule", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ScheduleResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Xử lý dữ liệu trả về từ server và đưa vào HashMap
    private void processScheduleData(List<ScheduleResponse> rawList) {
        database.clear(); // Xóa dữ liệu cũ

        // Định dạng ngày giờ từ Server (ISO 8601)
        // Ví dụ: 2023-12-14T08:00:00.000Z
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;

        // Định dạng hiển thị giờ (HH:mm)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (ScheduleResponse item : rawList) {
            try {
                // Parse chuỗi ngày giờ
                LocalDateTime startDateTime = LocalDateTime.parse(item.getStartTime(), isoFormatter);
                LocalDateTime endDateTime = LocalDateTime.parse(item.getEndTime(), isoFormatter);

                // Lấy ngày (LocalDate) để làm Key cho HashMap
                LocalDate dateKey = startDateTime.toLocalDate();

                // Tạo chuỗi hiển thị giờ: "08:00 - 10:00"
                String timeString = startDateTime.format(timeFormatter) + " - " + endDateTime.format(timeFormatter);

                // Tạo đối tượng Schedule (để hiển thị lên RecyclerView)
                Schedule schedule = new Schedule(
                        item.getScheduleId(),
                        item.getSubjectName(),
                        timeString,
                        item.getRoomName(),
                        item.getLecturerName(),
                        item.getStartTime(),
                        item.getCategory()
                );

                // Thêm vào Map
                if (!database.containsKey(dateKey)) {
                    database.put(dateKey, new ArrayList<>());
                }
                database.get(dateKey).add(schedule);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Sau khi xử lý xong, refresh lại giao diện lịch và list
        monthCalendarView.notifyCalendarChanged();
        weekCalendarView.notifyCalendarChanged();
        updateAdapterForDate(selectedDate); // Update list cho ngày đang chọn
    }

    // --- CÁC HÀM UI ---
    private void bindDayView(TextView textView, View frameLayout, View dotView, LocalDate date, boolean isCurrentMonth) {
        textView.setText(String.valueOf(date.getDayOfMonth()));

        if (isCurrentMonth) {
            textView.setTextColor(Color.BLACK);

            // Highlight ngày được chọn
            if (date.equals(selectedDate)) {
                frameLayout.setBackgroundResource(R.drawable.ic_dot); // Hoặc background tròn màu xanh
                frameLayout.getBackground().mutate().setTint(getResources().getColor(R.color.deep_blue, null));
                textView.setTextColor(Color.WHITE);
            } else {
                frameLayout.setBackground(null);
            }

            // Hiển thị chấm đỏ nếu có lịch học
            if (database.containsKey(date)) {
                dotView.setVisibility(View.VISIBLE);
            } else {
                dotView.setVisibility(View.GONE);
            }
        } else {
            textView.setTextColor(Color.GRAY);
            dotView.setVisibility(View.GONE);
            frameLayout.setBackground(null);
        }
    }

    private void selectDate(LocalDate date) {
        if (selectedDate.equals(date)) {
            return;
        }

        LocalDate oldDate = selectedDate;
        selectedDate = date;

        monthCalendarView.notifyDateChanged(oldDate);
        monthCalendarView.notifyDateChanged(selectedDate);
        weekCalendarView.notifyDateChanged(oldDate);
        weekCalendarView.notifyDateChanged(selectedDate);

        updateAdapterForDate(selectedDate);
    }

    private void updateMonthHeader(YearMonth yearMonth) {
        String title = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        tvMonthYear.setText(title);
    }

    private void updateAdapterForDate(LocalDate date) {
        tvDateSelected.setText("Schedule: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        if (database.containsKey(date)) {
            adapter.updateData(database.get(date));
        } else {
            adapter.updateData(new ArrayList<>());
        }
    }
}
