public class PreLargeSet {
    private int TotalDbSize;
    private int size;
    private double TotalDbUtility;

    public int getTotalDbSize() {
        return TotalDbSize;
    }

    public int getSize() {
        return size;
    }

    public double getTotalDbUtility() {return TotalDbUtility;}

    public void setTotalDbSize(int TotalDbSize) {
        this.TotalDbSize = TotalDbSize;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTotalDbUtility(double dbUtility) {this.TotalDbUtility = dbUtility;}
}
