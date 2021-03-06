package spring.dao;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import spring.models.Group;
import spring.models.Student;
import spring.models.Subject;

import javax.swing.plaf.nimbus.State;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Component
public class StudentDAO {
    private static final Logger LOGGER = Logger.getLogger(StudentDAO.class);

    public List<Student> showAll() {
        List<Student> students = new ArrayList<>();
        try (Connection connection = JDBC.getInstance().getConnection();
             Statement statement = connection.createStatement();
        ) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM students");
            while (resultSet.next()) {
                Student student = new Student();
                student.setName(resultSet.getString("name"));
                student.setSurname(resultSet.getString("surname"));
                student.setId(resultSet.getInt("student_id"));
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
        return students;
    }

    public List<Student> getStudentsByGroup(int id) {
        List<Student> students = null;
        try (Connection connection = JDBC.getInstance().getConnection();
             PreparedStatement preparedStatementGroup =
                     connection.prepareStatement("SELECT \"group\" FROM STUDENTS WHERE student_id = ?;");
             PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM STUDENTS WHERE \"group\" = ?;");
        ) {
            preparedStatementGroup.setInt(1, id);
            ResultSet resultSet = preparedStatementGroup.executeQuery();
            resultSet.next();
            int groupId = resultSet.getInt("group");

            preparedStatement.setInt(1, groupId);
            ResultSet result = preparedStatement.executeQuery();
            students = new ArrayList<>();

            while (result.next()) {
                Student student = new Student();
                student.setName(result.getString("name"));
                student.setSurname(result.getString("surname"));
                student.setId(result.getInt("student_id"));
                students.add(student);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
        return students;
    }

    public Student showIndex(int id) {
        Student student = null;
        try (Connection connection = JDBC.getInstance().getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM STUDENTS " +
                             "join groups g on(g.group_id = students.group) " +
                             "join faculties f on (f.faculty_id = g.faculty)  " +
                             "WHERE student_id=?;");
        ) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            student = new Student();
            student.setId(resultSet.getInt("student_id"));
            student.setName(resultSet.getString("name"));
            student.setSurname(resultSet.getString("surname"));
            student.setPatronymic(resultSet.getString("patronymic"));

            student.setEmail(resultSet.getString("email"));
            student.setCourse(resultSet.getInt("course"));
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
        return student;
    }

    public void save(Student student) {
        try (Connection connection = JDBC.getInstance().getConnection();
             Statement statement = connection.createStatement();
             PreparedStatement preparedStatement =
                     connection.prepareStatement("insert into students(student_id, \"group\", faculty, name, surname, patronymic, email, course) " +
                             "values (?,?,?,?,?,?,?,?);");
             PreparedStatement preparedStatementLog =
                     connection.prepareStatement("insert into login_info(login, password, id, role) VALUES (?, '$2a$12$1nNZ70SEYtDIQtbldRxw2..iulgYqWhk79lO4oYmHENnM47QBMpzq',?,'USER')");
        ) {

            statement.executeUpdate("insert into users(status) values ('STUDENT');");
            ResultSet resultSet = statement.executeQuery("select * from users order by id desc;");
            resultSet.next();
            int id = resultSet.getInt("id");

            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, student.getGroupId());
            preparedStatement.setInt(3, student.getFacultyId());
            preparedStatement.setString(4, student.getName());
            preparedStatement.setString(5, student.getSurname());
            preparedStatement.setString(6, student.getPatronymic());
            preparedStatement.setString(7, student.getEmail());
            preparedStatement.setInt(8, student.getCourse());
            preparedStatement.executeUpdate();
            preparedStatementLog.setString(1, student.getEmail());
            preparedStatementLog.setInt(2, id);

            preparedStatementLog.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
    }

    public void update(int id, Student student) {
        try (Connection connection = JDBC.getInstance().getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement("UPDATE students " +
                             "SET \"group\"=?,faculty=?,name=?,surname=?,patronymic=?,email=?,course=? " +
                             "where student_id = ?;");
        ) {
            preparedStatement.setInt(1, student.getGroupId());
            preparedStatement.setInt(2, student.getFacultyId());
            preparedStatement.setString(3, student.getName());
            preparedStatement.setString(4, student.getSurname());
            preparedStatement.setString(5, student.getPatronymic());
            preparedStatement.setString(6, student.getEmail());
            preparedStatement.setInt(7, student.getCourse());
            preparedStatement.setInt(8, id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
    }

    public void delete(int id) {
        try (Connection connection = JDBC.getInstance().getConnection();
             PreparedStatement preparedStatementMarks =
                     connection.prepareStatement("DELETE FROM marks WHERE student_id = ?");
             PreparedStatement preparedStatementStudents =
                     connection.prepareStatement("DELETE FROM students WHERE student_id = ?");
             PreparedStatement preparedStatementLogInfo =
                     connection.prepareStatement("DELETE FROM login_info WHERE id = ?");
             PreparedStatement preparedStatementUsers =
                     connection.prepareStatement("DELETE FROM users WHERE id = ?");
        ) {

            preparedStatementMarks.setInt(1, id);
            preparedStatementMarks.executeUpdate();

            preparedStatementStudents.setInt(1, id);
            preparedStatementStudents.executeUpdate();

            preparedStatementLogInfo.setInt(1, id);
            preparedStatementLogInfo.executeUpdate();

            preparedStatementUsers.setInt(1, id);
            preparedStatementUsers.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
    }
}
