package com.example.client;

public class Classroom {
    private String class_id;
    private String class_info;

    public Classroom() {
        class_id = "";
        class_info = "";
    }

    public Classroom(String class_id, String data) {
        this.class_id = class_id;
        this.class_info = data;
    }

    public String getClassroomName() {
        return class_id;
    }

    public String getClassroomInfo() {
        return class_info;
    }

    public void setClassroomName(String avatarId) {
        this.class_id = avatarId;
    }

    public void setClassroomInfo(String data) {
        this.class_info = data;
    }
}
