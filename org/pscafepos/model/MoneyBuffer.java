package org.pscafepos.model;/*   PSCafePOS is an Open Source Point of Sale System for Schools
 *   Copyright (C) 2007 Charles Syperski
 *
 *   This program is free software; you can redistribute it and/or modify 
 *   it under the terms of the GNU General Public License as published by 
 *   the Free Software Foundation; either version 2 of the License, or 
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful, 
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *   See the GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License 
 *   along with this program; if not, write to the 
 *   Free Software Foundation, Inc., 
 *   59 Temple Place, Suite 330, 
 *   Boston, MA 02111-1307 USA
 */

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;

public class MoneyBuffer {

    private BigDecimal cash, credit;

    public MoneyBuffer() {
        cash = ZERO;
        credit = ZERO;
    }

    public void addCash(BigDecimal value) {
        if (value.compareTo(ZERO) > 0) {
            cash = cash.add(value);
        }
    }

    public void addCredit(BigDecimal value) {
        if (value.compareTo(ZERO) > 0) {
            credit = credit.add(value);
        }
    }

    public void setCash(BigDecimal value) {
        if (value.compareTo(ZERO) >= 0) {
            cash = value;
        }
    }

    public void setCredit(BigDecimal value) {
        if (value.compareTo(ZERO) >= 0) {
            credit = value;
        }
    }

    public BigDecimal getCash() {
        return cash;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public BigDecimal getBufferTotal() {
        return getCash().add(getCredit());
    }

    public void flushCash() {
        cash = ZERO;
    }

    public void flushCredit() {
        credit = ZERO;
    }

    public void flushAll() {
        cash = ZERO;
        credit = ZERO;
    }
}