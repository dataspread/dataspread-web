package org.zkoss.zss.model.impl.navigation;

/**
 * Created by Sajjadur on 10/12/2017.
 */
import java.util.ArrayList;
import java.util.List;

public class StockModel {

    private List<Stock> stock = new ArrayList<Stock>();

    public StockModel(){
        stock.add(generateStock("Csco"));
        stock.add(generateStock("Goog"));
        stock.add(generateStock("Yhoo"));
        stock.add(generateStock("Msft"));
        stock.add(generateStock("Orcl"));
        stock.add(generateStock("Amaz"));
        stock.add(generateStock("Fabc"));
    }

    public List<Stock> getStocks() {
        return stock;
    }

    private static Stock generateStock(String stockName) {
        return new Stock(stockName, getValue(), getValue(), getVolume(), generateQuarters());
    }

    private static List<Quarter> generateQuarters() {
        List<Quarter> quarters = new ArrayList<Quarter>();
        //generate each quarter
        for(int i =0; i<4; i++) {
            quarters.add(generateQuarter(i * 3));
        }
        return quarters;
    }

    private static Quarter generateQuarter(int start) {
        List<Month> months = new ArrayList<Month>();

        for(int i=0;i<3;i++) {
            months.add(new Month(start + i, getValue(), getValue(), getVolume()));
        }

        return new Quarter((start / 3) + 1, months);
    }

    private static double getValue() {
        return Math.random() * 50 + 40;
    }

    private static double getVolume() {
        return Math.random() * 50000 + 65536;
    }
}
