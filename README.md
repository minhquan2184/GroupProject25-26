# Classroom Schedule Management App (Android Client)

A comprehensive Android application designed for **University of Science and Technology of Hanoi (USTH)** students and lecturers to manage academic schedules, course enrollments, and attendance efficiently.

##  Project Overview

This is a native Android application built with **Java**. It follows a Client-Server architecture where the Android app acts as the client, communicating with a Node.js backend via RESTful APIs. The app features a role-based authentication system separating **Students** and **Lecturers** functionalities.

##  Key Features

###  For Students
* **Smart Calendar:** View weekly/monthly class schedules with visual indicators for upcoming classes.
* **Course Enrollment:** Browse available subjects and self-enroll in classes. The app handles "Enrolled" status logic to prevent duplicate registrations.
* **My Courses:** View list of enrolled courses and access detailed information like attendance history.
* **Academic Results:** View grades, student ID, and major information.

### ‍ For Lecturers
* **Teaching Schedule:** View assigned classes and room locations on the calendar.
* **Class Dashboard:** Manage specific class details (Room, Time, Documents).

---

## 🛠 Tech Stack

The project utilizes modern Android development standards and libraries:

* **Language:** Java (JDK 11).
* **Minimum SDK:** API 24 (Android 7.0) | **Target SDK:** API 36.
* **Networking:**
    * **Retrofit 2:** Type-safe HTTP client for API calls.
    * **GSON:** JSON serialization/deserialization.
* **UI Components:**
    * **Material Design 3:** For modern UI elements.
    * **Kizitonwose Calendar View:** A highly customizable calendar library for handling complex schedule views.
    * **ConstraintLayout & RecyclerView:** For responsive layouts and list rendering.
    * **Navigation Drawer:** For main application navigation.

---

##  Project Architecture

The codebase is organized by **Features** to ensure scalability and maintainability:
