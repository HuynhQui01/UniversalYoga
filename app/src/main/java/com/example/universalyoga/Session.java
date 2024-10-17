package com.example.universalyoga;

public class Session {
    int Id;
    String Date;
    int InstructorId;
    String Comment;
    int ClassID;

    public Session(int ID, String date, int instructorID, String comment, int classID) {
        this.Id = ID;
        Date = date;
        InstructorId = instructorID;
        Comment = comment;
        ClassID = classID;
    }

    public Session() {
    }

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public int getInstructorId() {
        return InstructorId;
    }

    public void setInstructorId(int instructorID) {
        InstructorId = instructorID;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public int getClassId() {
        return ClassID;
    }

    public void setClassId(int classID) {
        ClassID = classID;
    }





}
