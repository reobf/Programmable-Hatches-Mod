package reobf.proghatches.gt.metatileentity.cycle;

public final class CycleAmount<K> {

    public final K key;
    public final long amount;

    public CycleAmount(K key, long amount) {
        if (key == null) throw new NullPointerException("key");
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0: " + amount);
        this.key = key;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CycleAmount)) return false;
        CycleAmount<?> other = (CycleAmount<?>) o;
        return amount == other.amount && key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return 31 * key.hashCode() + Long.hashCode(amount);
    }

    @Override
    public String toString() {
        return key + "x" + amount;
    }
}
