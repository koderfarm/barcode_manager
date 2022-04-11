package am.barcodemanager.model;

public class Pallet {
    String lot_no,article_no,roll_no,prod_qty;

    public Pallet(String lot_no, String article_no, String roll_no, String prod_qty) {
        this.lot_no = lot_no;
        this.article_no = article_no;
        this.roll_no = roll_no;
        this.prod_qty = prod_qty;
    }

    public String getLot_no() {
        return lot_no;
    }

    public void setLot_no(String lot_no) {
        this.lot_no = lot_no;
    }

    public String getArticle_no() {
        return article_no;
    }

    public void setArticle_no(String article_no) {
        this.article_no = article_no;
    }

    public String getRoll_no() {
        return roll_no;
    }

    public void setRoll_no(String roll_no) {
        this.roll_no = roll_no;
    }

    public String getProd_qty() {
        return prod_qty;
    }

    public void setProd_qty(String prod_qty) {
        this.prod_qty = prod_qty;
    }
}
