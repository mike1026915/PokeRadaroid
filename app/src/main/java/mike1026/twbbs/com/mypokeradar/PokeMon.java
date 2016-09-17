package mike1026.twbbs.com.mypokeradar;

import java.io.Serializable;

/**
 * Created by mike on 2016/9/10.
 */
public class PokeMon implements Serializable {
    public int id = 0;
    public double longtitude = 0.0;
    public double latitude = 0.0;
    public PokeMon(int tmp_id, double longt, double lat)
    {
        id = tmp_id;
        longtitude = longt;
        latitude = lat;

    }
}
