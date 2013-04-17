package org.pscafepos.backends.domain.sis;

import org.pscafepos.model.Student;
import org.pscafepos.backends.sis.SisException;

/**
 * @author bagmanov
 */
public interface StudentInformationDAO {
    Student getStudent(String studentId) throws SisException;
}
