package vn.edu.usth.classroomschedulemanagementapp.Calendar;

public class Schedule {

    String id;
    String subject;
    String time;
    String room;
    String lecturer;
    String date;
    String category;

    public Schedule(String id, String subject, String time, String room, String lecturer, String date, String category) {
        this.id = id;
        this.subject = subject;
        this.time = time;
        this.room = room;
        this.lecturer = lecturer;
        this.date = date;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }
}
