package org.pscafepos.backends.domain.sis;

import org.pscafepos.model.Student;

/**
 * @author bagmanov
 */
public interface SchoolOfficerDAO {

    public Student loadOfficer(String id);
}
