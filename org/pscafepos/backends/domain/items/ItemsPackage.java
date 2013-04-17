/*   PSCafePOS is an Open Source Point of Sale System for Schools
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
package org.pscafepos.backends.domain.items;

import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

public class ItemsPackage {
    private List<OrderItem> items;
    private String strName;
    private int id;
    private NumberFormat priceFormat;

    public ItemsPackage(String batchName) {
        strName = batchName;
        items = new ArrayList<OrderItem>();
        priceFormat = NumberFormat.getCurrencyInstance();
    }

    public void addItems(List<OrderItem> items) {
        items.addAll(items);
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public String getName() {
        return strName;
    }

    public String getTotalString() {
        return priceFormat.format(getTotal());
    }

    public BigDecimal getTotal() {
        if (items != null && items.size() > 0) {
            BigDecimal total = BigDecimal.ZERO;
            for (Object item1 : items) {
                OrderItem item = (OrderItem) item1;
                if (item != null) {
                    if (item.getNormalPrice().compareTo(BigDecimal.ZERO) > 0)
                        total = total.add(item.getNormalPrice());
                }
            }
            return total;
        }
        return BigDecimal.ZERO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}