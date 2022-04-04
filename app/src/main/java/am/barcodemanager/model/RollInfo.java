package am.barcodemanager.model;

public class RollInfo {
    String palletNumber,
            LotNumber,
            RollNumber,
            Weight,
            Date,
            Meters,
            ArticleNumber;
    int Status;

    public RollInfo(String palletNumber, String RollNumber, int anInt) {
        this.palletNumber = palletNumber;
        this.RollNumber = RollNumber;
        this.Status = anInt;
    }

    public String getPalletNumber() {
        return palletNumber;
    }

    public void setPalletNumber(String palletNumber) {
        this.palletNumber = palletNumber;
    }

    public String getLotNumber() {
        return LotNumber;
    }

    public void setLotNumber(String lotNumber) {
        LotNumber = lotNumber;
    }

    public String getRollNumber() {
        return RollNumber;
    }

    public void setRollNumber(String rollNumber) {
        RollNumber = rollNumber;
    }

    public String getWeight() {
        return Weight;
    }

    public void setWeight(String weight) {
        Weight = weight;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getMeters() {
        return Meters;
    }

    public void setMeters(String meters) {
        Meters = meters;
    }

    public String getArticleNumber() {
        return ArticleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        ArticleNumber = articleNumber;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }
}

