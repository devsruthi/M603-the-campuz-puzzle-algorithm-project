public class Course {
    String class_id;
    int number_of_enrolled_students;
    String professor_id;

    Course(String class_id, int number_of_enrolled_students, String professor_id) {
        this.class_id = class_id;
        this.number_of_enrolled_students = number_of_enrolled_students;
        this.professor_id = professor_id;
    }
}
