package mctest.capability;

public class SanityCapability implements ISanityCapability{

    private int Sanity = 100;
    private int maxSanity = 100;
    @Override
    public int getSanity() {
        return Sanity;
    }

    @Override
    public void setSanity(int val) {
        this.Sanity = Math.max(0, Math.min(val,maxSanity));
    }

    @Override
    public void addSanity(int val) {
        setSanity(this.Sanity + val);
    }

    @Override
    public int getMaxSanity() {
        return maxSanity;
    }
}
