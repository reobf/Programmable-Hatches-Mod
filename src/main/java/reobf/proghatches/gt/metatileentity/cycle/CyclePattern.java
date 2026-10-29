package reobf.proghatches.gt.metatileentity.cycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CyclePattern<K> {

    public final int id;
    public final List<CycleAmount<K>> inputs;
    public final List<CycleAmount<K>> outputs;

    public CyclePattern(int id, List<CycleAmount<K>> inputs, List<CycleAmount<K>> outputs) {
        this.id = id;
        this.inputs = merge(inputs);
        this.outputs = merge(outputs);
    }

    private static <K> List<CycleAmount<K>> merge(List<CycleAmount<K>> list) {
        if (list == null) throw new NullPointerException("list");
        LinkedHashMap<K, Long> sum = new LinkedHashMap<>();
        for (CycleAmount<K> c : list) {
            if (c == null) throw new NullPointerException("amount");
            Long old = sum.get(c.key);
            try {
                sum.put(c.key, old == null ? c.amount : Math.addExact(old, c.amount));
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("amount of " + c.key + " overflows", e);
            }
        }
        List<CycleAmount<K>> res = new ArrayList<>(sum.size());
        for (Map.Entry<K, Long> e : sum.entrySet()) res.add(new CycleAmount<>(e.getKey(), e.getValue()));
        return Collections.unmodifiableList(res);
    }

    @Override
    public String toString() {
        return "#" + id + " " + inputs + " -> " + outputs;
    }
}
