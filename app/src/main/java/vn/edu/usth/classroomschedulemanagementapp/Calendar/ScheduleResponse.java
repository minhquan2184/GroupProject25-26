package vn.edu.usth.classroomschedulemanagementapp.Calendar;

import com.google.gson.annotations.SerializedName;

public class ScheduleResponse {

    @SerializedName("scheduleId")
    private String scheduleId;
    @SerializedName("subjectName")
    private String subjectName;

    @SerializedName("roomName")
    private String roomName;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("lecturerName")
    private String lecturerName;

    @SerializedName("category")
    private String category;

    public String getScheduleId() {
        return scheduleId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public String getCategory() {
        return category;
    }
}
