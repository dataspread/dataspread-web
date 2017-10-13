package org.zkoss.zss.model.impl.navigation;

/**
 * Created by Sajjadur on 10/12/2017.
 */
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Month {
    private final double high, low, volume;
    private final Calendar month = Calendar.getInstance();
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
    private final String stringMonth;

    public Month(int month, double high, double low, double volume) {
        this.high = high;
        this.low = low;
        this.volume = volume;
        this.month.set(Calendar.MONTH, month);
        this.stringMonth = monthFormat.format(this.month.getTime());
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getVolume() {
        return volume;
    }

    public String getMonth() {
        return stringMonth;
    }
}
