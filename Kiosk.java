package farmersfridge;

/**
 *
 * @author ChristyJeyaraj
 */
public class Kiosk {
    int Id;
    String Name;
    String Address;
    double Latitude;
    double Longitude;
    
    public Kiosk(){ }
    
    public Kiosk(int id, String name, String address, double latitude, double longitude){
        Id = id;
        Name = name;
        Address = address;
        Latitude = latitude;
        Longitude = longitude;
    }
}
