package de.otto.edison.aggregator.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.UNORDERED;
import static java.util.stream.Collector.of;

public class Collectors {
    private Collectors() {}

    public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
        return of(
                ImmutableList::<T>builder,
                ImmutableList.Builder::add,
                (builder1, builder2) -> builder1.addAll(builder2.build()),
                ImmutableList.Builder::build
        );
    }

    public static <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {
        return of(
                ImmutableSet::<T>builder,
                ImmutableSet.Builder::add,
                (builder1, builder2) -> builder1.addAll(builder2.build()),
                ImmutableSet.Builder::build,
                UNORDERED
        );
    }

    public static <T, K, V> Collector<T, ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> toImmutableMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return of(
                ImmutableMap.Builder::new,
                (builder, t) -> builder.put(keyMapper.apply(t), valueMapper.apply(t)),
                (builder1, builder2) -> builder1.putAll(builder2.build()),
                ImmutableMap.Builder::build);
    }

    public static <T, K, V> Collector<T, LinkedHashMap<K, V>, LinkedHashMap<K, V>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return of(
                LinkedHashMap<K, V>::new,
                (map, t) -> {map.put(keyMapper.apply(t), valueMapper.apply(t));},
                (map1, map2) -> {map1.putAll(map2); return map1;}
        );
    }
}
