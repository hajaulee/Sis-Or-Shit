package com.hajaulee.sisorshit;

import android.util.Log;

import java.util.Arrays;

public class Subject {
    private static final String TAG = "Subject";

    private int semester;
    private int credit;
    private int classId;

    private String courseId;
    private String courseName;
    private String rank;

    private float midTermScore;
    private float finalExamScore;

    private Subject(int semester, int credit, int classId,
                    String courseId, String courseName, String rank,
                    float midTermScore, float finalExamScore) {
        this.semester = semester;
        this.credit = credit;
        this.classId = classId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.rank = rank;
        this.midTermScore = midTermScore;
        this.finalExamScore = finalExamScore;
    }

    public static Subject createSubject(String[] a) {
        try {
            return new Subject(Integer.parseInt(a[0]), Integer.parseInt(a[3]), Integer.parseInt(a[4]),
                    a[1], a[2], a[7],
                    Float.parseFloat(a[5]),
                    Float.parseFloat(a[6]));
        }catch (Exception e){
            Log.e(TAG, Arrays.toString(a) + e.toString());
            return null;
        }
    }

    public static Subject createSubject(String a){
        return createSubject(a.split("__"));
    }

    public int getSemester() {
        return semester;
    }

    public int getCredit() {
        return credit;
    }

    public int getClassId() {
        return classId;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getRank() {
        return rank;
    }

    public float getMidTermScore() {
        return midTermScore;
    }

    public float getFinalExamScore() {
        return finalExamScore;
    }
}
