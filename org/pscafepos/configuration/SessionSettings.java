package org.pscafepos.configuration;

import org.pscafepos.configuration.PosSettings;

import java.math.BigDecimal;

/**
 * @author bagmanov
 */
public class SessionSettings {

    String buildingNumber;
    String buildingName;
    boolean anonymousTransactionsAllowed;
    boolean negativeBalanceAllowed;
    BigDecimal maxNegativeBalance;
    boolean allowOnlyActiveStudents;
    boolean allowOnlyStudentsExistedInSIS;
    boolean showFreeReducedData;
    boolean isDrawerEnabled;
    boolean sellOnCreditOnlyMode;
    boolean autoCheckOutMode;
    String drawerName;
    String drawerClass;
    String imageBackGround;
    SisSettings sisSettings;
    PosSettings posSettings;

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public boolean isAnonymousTransactionsAllowed() {
        return anonymousTransactionsAllowed;
    }

    public boolean isNegativeBalanceAllowed() {
        return negativeBalanceAllowed;
    }


    /**
     * @return abs value
     */
    public BigDecimal getMaxNegativeBalance() {
        return maxNegativeBalance;
    }

    public boolean isAllowOnlyActiveStudents() {
        return allowOnlyActiveStudents;
    }

    public boolean isAllowOnlyStudentsExistedInSIS() {
        return allowOnlyStudentsExistedInSIS;
    }

    public boolean isShowFreeReducedData() {
        return showFreeReducedData;
    }

    public boolean isDrawerEnabled() {
        return isDrawerEnabled;
    }

    public String getDrawerName() {
        return drawerName;
    }

    public String getDrawerClass() {
        return drawerClass;
    }

    public String getImageBackGround() {
        return imageBackGround;
    }

    public SisSettings getSisSettings() {
        return sisSettings;
    }

    public PosSettings getPosSettings() {
        return posSettings;
    }

    public boolean isSellOnCreditOnlyMode() {
        return sellOnCreditOnlyMode;
    }

    public boolean isAutoCheckOutMode() {
        return autoCheckOutMode;
    }
}
