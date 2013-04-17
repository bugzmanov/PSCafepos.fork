package org.pscafepos.backends.domain.items;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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

import java.text.NumberFormat;
import java.math.BigDecimal;
import java.util.Arrays;
import java.io.Serializable;

public class OrderItem implements Cloneable, Serializable {
    private int id;
    private String title, description, category, buildingNumber;
    private BigDecimal price, reducedPrice;
    private boolean isFree, isReduced, isTypeA;
    private boolean sellAsFree, sellAsReduced;
    private int mealType;
    private byte[] ico;

    private NumberFormat moneyFormat;

    //TODO: this should be enum
    public static final int MEAL_TYPE_FREE_REDUCED_BREAKFAST = 0;
    public static final int MEAL_TYPE_FREE_REDUCED_LUNCH = 1;

    public OrderItem(int id, String itemTitle, String description, String category, String buildingNumber, BigDecimal price, BigDecimal reducedPrice, boolean free, boolean reduced, boolean typeA, int freeBL) {
        this.id = id;
        title = itemTitle;
        this.description = description;
        this.category = category;
        this.buildingNumber = buildingNumber;
        this.price = price;
        this.reducedPrice = reducedPrice;
        isFree = free;
        isReduced = reduced;
        isTypeA = typeA;
        mealType = freeBL;

        sellAsFree = false;
        sellAsReduced = false;

        moneyFormat = NumberFormat.getCurrencyInstance();
    }

    public void sellAsFree(boolean free) {
        if (isFree) {
            if (free) {
                sellAsReduced = false;
                sellAsFree = free;
            } else {
                sellAsFree = false;
            }
        } else {
            sellAsFree = false;
        }
    }

    public boolean isSoldAsFree() {
        return sellAsFree;
    }

    public boolean isSoldAsReduced() {
        return sellAsReduced;
    }

    public String getSellTypeString() {
        if (sellAsFree)
            return "Free";
        else if (sellAsReduced)
            return "Reduced";
        else
            return "Normal";
    }

    public void sellAsReduced(boolean reduced) {
        if (isReduced) {
            if (reduced) {
                sellAsFree = false;
                sellAsReduced = reduced;
            } else {
                sellAsReduced = false;
            }
        } else {
            sellAsReduced = false;
        }
    }

    public String getEffectivePriceString() {
        return moneyFormat.format(getEffectivePrice());
    }

    public BigDecimal getEffectivePrice() {
        if (sellAsFree)
            return BigDecimal.ZERO;

        if (sellAsReduced && reducedPrice.compareTo(BigDecimal.ZERO) >= 0)
            return reducedPrice;

        if (price.compareTo(BigDecimal.ZERO) >= 0)
            return price;

        return BigDecimal.ZERO;
    }

    public int getDBID() {
        return id;
    }

    public String getName() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getBuilding() {
        return buildingNumber;
    }

    public BigDecimal getNormalPrice() {
        return price;
    }

    public String getNormalPriceString() {
        return moneyFormat.format(price);
    }

    public BigDecimal getReducedPrice() {
        return reducedPrice;
    }

    public boolean isFree() {
        return isFree;
    }

    public boolean isReduced() {
        return isReduced;
    }

    public boolean isTypeA() {
        return isTypeA;
    }

    public boolean isSpecialLunch() {
        return mealType == MEAL_TYPE_FREE_REDUCED_LUNCH;
    }

    public boolean isSpecialBrekfast() {
        return mealType == MEAL_TYPE_FREE_REDUCED_BREAKFAST;
    }

    public boolean completeItem() {
        if (id < 0)
            return false;

        if (title == null)
            return false;

        if (category == null)
            return false;

        if (buildingNumber == null)
            return false;

        if (price.compareTo(BigDecimal.ZERO) < 0)
            return false;

        if(isReduced && reducedPrice.compareTo(BigDecimal.ZERO) <= 0){
            return false;
        }
        
        if (isFree || isReduced) {
            if (mealType != MEAL_TYPE_FREE_REDUCED_BREAKFAST && mealType != MEAL_TYPE_FREE_REDUCED_LUNCH)
                return false;
        }

        return true;
    }

    public byte[] getIco() {
        if(ico != null) {
            return Arrays.copyOf(ico, ico.length);
        } else {
            return null;
        }
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            OrderItem newItem = new OrderItem(id, title, description, category, buildingNumber, price, reducedPrice, isFree, isReduced, isTypeA, mealType);
            newItem.ico = ico;
            newItem.sellAsFree = sellAsFree;
            newItem.sellAsReduced = sellAsReduced;
            return newItem;
        }
    }

    public void setIco(byte[] ico) {
        this.ico = Arrays.copyOf(ico, ico.length);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", buildingNumber='" + buildingNumber + '\'' +
                ", price=" + price +
                ", reducedPrice=" + reducedPrice +
                ", isFree=" + isFree +
                ", isReduced=" + isReduced +
                ", isTypeA=" + isTypeA +
                ", sellAsFree=" + sellAsFree +
                ", sellAsReduced=" + sellAsReduced +
                ", mealType=" + mealType +
                ", money=" + moneyFormat +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderItem orderItem = (OrderItem) o;

        if (id != orderItem.id) return false;
        if (buildingNumber != null ? !buildingNumber.equals(orderItem.buildingNumber) : orderItem.buildingNumber != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (buildingNumber != null ? buildingNumber.hashCode() : 0);
        return result;
    }
}

