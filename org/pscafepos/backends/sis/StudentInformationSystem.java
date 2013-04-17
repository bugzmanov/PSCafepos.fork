package org.pscafepos.backends.sis;

import org.pscafepos.model.Student;
import org.pscafepos.event.POSEventListener;

/**
 * @author bagmanov
 */
public interface StudentInformationSystem {

    public Student getStudent(String studentId) throws SisException;
    public Student getAnonymousStudent();
    public void loadStudentImageAsync(Student student, POSEventListener onCompleteEventListener);
    
}
