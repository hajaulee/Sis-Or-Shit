package com.hajaulee.sisorshit;

public class Subject {
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
        return new Subject(Integer.valueOf(a[0]), Integer.valueOf(a[3]), Integer.valueOf(a[5]),
                a[1], a[2], a[7],
                Float.valueOf(a[5]), Float.valueOf(a[6]));
    }
}
