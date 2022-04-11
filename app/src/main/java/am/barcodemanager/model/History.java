package am.barcodemanager.model;

public class History {
    String date,pallet_no,total_rolls,total_meters,transfered;

    public History() {
    }

    public History(String date, String pallet_no, String total_rolls, String total_meters, String transfered) {
        this.date = date;
        this.pallet_no = pallet_no;
        this.total_rolls = total_rolls;
        this.total_meters = total_meters;
        this.transfered = transfered;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPallet_no() {
        return pallet_no;
    }

    public void setPallet_no(String pallet_no) {
        this.pallet_no = pallet_no;
    }

    public String getTotal_rolls() {
        return total_rolls;
    }

    public void setTotal_rolls(String total_rolls) {
        this.total_rolls = total_rolls;
    }

    public String getTotal_meters() {
        return total_meters;
    }

    public void setTotal_meters(String total_meters) {
        this.total_meters = total_meters;
    }

    public String getTransfered() {
        return transfered;
    }

    public void setTransfered(String transfered) {
        this.transfered = transfered;
    }
}
