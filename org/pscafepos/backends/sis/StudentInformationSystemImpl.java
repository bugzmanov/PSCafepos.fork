package org.pscafepos.backends.sis;

import org.pscafepos.backends.domain.sis.StudentInformationDAO;
import org.pscafepos.backends.domain.sis.SchoolOfficerDAO;
import org.pscafepos.model.Student;

import static org.pscafepos.util.StringUtils.*;
import org.pscafepos.event.POSEventListener;
import org.pscafepos.backends.sis.util.ThreadedImageManager;
import org.pscafepos.configuration.SisSettings;
import org.pscafepos.configuration.Constants;

/**
 * @author bagmanov
 */
public class StudentInformationSystemImpl implements StudentInformationSystem {
    private SisSettings settings;
    private ThreadedImageManager imageManager;
    private StudentInformationDAO studentDao;
    private SchoolOfficerDAO officerDAO;

    public StudentInformationSystemImpl(SisSettings settings, StudentInformationDAO studentDao, SchoolOfficerDAO officerDAO) {
        this.settings = settings;
        this.studentDao = studentDao;
        this.officerDAO = officerDAO;
    }

    public Student getStudent(final String studentId) throws SisException {
        if (isEmpty(studentId)) {
            throw new SisException("ID Cannot be null");
        } else if (studentId.contains(" ") && !settings.allowSpacesInID()) {
            throw new SisException("Spaces in ID numbers are not allowed as configured in the settings file.");
        } else if (!studentId.equals(Student.NOSTUDENT) && settings.idFixedWidth() &&
                (studentId.length() != settings.idFixedWidthLength())) {
            throw new SisException("Invalid Student ID, Fixed width of " + settings.idFixedWidthLength() + " required");
        }
        if(studentId.equals(SchoolStudent.NOSTUDENT)){
            return getAnonymousStudent();
        }
        Student buyer;
        if(isOfficerId(studentId)){
            buyer = officerDAO.loadOfficer(studentId.substring(1));
        } else {
            buyer = studentDao.getStudent(studentId);
        }

        if(buyer == null){
            SchoolStudent schoolStudent = new SchoolStudent();
            schoolStudent.id = String.valueOf(studentId);
            buyer = schoolStudent;
        }
        return buyer;
    }

    private boolean isOfficerId(String id){
        return id.toLowerCase().startsWith(Constants.STAFF_ID_PREFIX);
    }

    public Student getAnonymousStudent() {
        SchoolStudent student = new SchoolStudent();
        student.id = SchoolStudent.NOSTUDENT;
        return student;
    }


    //TODO:refactor to use executors, move this method to student objects
    public void loadStudentImageAsync(Student student, POSEventListener onCompleteEventListener) {
        if(student.getType().equalsIgnoreCase("staff")) {
            return;
        }
        String imagePath = settings.getImagePath();
        if (imagePath != null && imagePath.contains("{studentid}")) {
            imagePath = imagePath.replace("{studentid}", student.getStudentNumber());
            if (imagePath != null) {
                if (imageManager != null) {
                    if (imageManager.isAlive()) {
                        imageManager.interrupt();
                    }
                }
                imageManager = new ThreadedImageManager(imagePath, 150);
                imageManager.setPOSEventListener(onCompleteEventListener);
                imageManager.start();
            }
        }
    }



}
