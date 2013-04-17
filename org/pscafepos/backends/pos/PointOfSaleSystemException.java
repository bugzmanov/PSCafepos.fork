package org.pscafepos.backends.pos;

/**
 * @author bagmanov
 *         Date: 07.09.2009
 */
public class PointOfSaleSystemException extends Exception{
    
    public PointOfSaleSystemException() {
        super();
    }

    public PointOfSaleSystemException(String message) {
        super(message);
    }

    public PointOfSaleSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public PointOfSaleSystemException(Throwable cause) {
        super(cause);
    }
}
