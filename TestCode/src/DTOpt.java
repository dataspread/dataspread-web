import org.zkoss.zss.model.CellRegion;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mangesh on 11/26/2016.
 */
public class DTOpt {
    // Dependency graph De
    static Map<CellRegion, CellRegion> dt;

    public static void main(String[] args)
    {
        dt = new HashMap<>();
        dt.put(new CellRegion("B1:B100"), new CellRegion("A1"));

        for(CellRegion region:dt.keySet())
            System.out.println(region.toString() + " " +  region.getCellCount());
    }



}
