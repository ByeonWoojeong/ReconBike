package app.cosmos.reconbike.DTO;

public class UseHistory {

    public String date;
    public String type;
    public String device;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public UseHistory(String date, String type, String device) {
        this.date = date;
        this.type = type;
        this.device = device;
    }

    @Override
    public String toString() {
        return "UseHistory{" +
                "date='" + date + '\'' +
                ", type='" + type + '\'' +
                ", device='" + device + '\'' +
                '}';
    }
}
