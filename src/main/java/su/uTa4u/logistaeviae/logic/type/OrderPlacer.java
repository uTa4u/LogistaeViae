package su.uTa4u.logistaeviae.logic.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@FunctionalInterface
public interface OrderPlacer {

    ItemStackOrder place();

    static Supplier<List<OrderPlacer>> getBlueprint(int size) {
        return () -> Arrays.asList(new OrderPlacer[size]);
    }

    static Supplier<List<OrderPlacer>> getBlueprint(OrderPlacer... orderPlacers) {
        return () -> Arrays.stream(orderPlacers)
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    static Supplier<List<OrderPlacer>> getBlueprint() {
        return Collections::emptyList;
    }

    OrderPlacer PROVIDER = () -> {
        return null;
    };

    OrderPlacer SUPPLIER = () -> {
        return null;
    };

}
