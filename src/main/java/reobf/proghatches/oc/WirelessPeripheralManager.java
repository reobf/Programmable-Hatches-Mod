package reobf.proghatches.oc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class WirelessPeripheralManager {

    static public HashMap<UUID, li.cil.oc.api.network.Node> cards = new HashMap<>();
    static public HashMap<UUID, li.cil.oc.api.network.Node> stations = new HashMap<>();

    public static void add(Map a, UUID k, li.cil.oc.api.network.Node v) {
        a.put(k, v);
        connect(k);
    }

    public static void remove(Map a, UUID k) {
        disconnect(k);
        a.remove(k);
    }

    public static void disconnect(UUID thisUUID) {
        Optional.ofNullable(stations.get(thisUUID))
            .ifPresent(
                s -> Optional.ofNullable(cards.get(thisUUID))
                    .ifPresent(w -> s.disconnect(w)));

    }

    public static void connect(UUID thisUUID) {
        Optional.ofNullable(stations.get(thisUUID))
            .ifPresent(
                s -> Optional.ofNullable(cards.get(thisUUID))
                    .ifPresent(w -> s.connect(w)));
    }

}
