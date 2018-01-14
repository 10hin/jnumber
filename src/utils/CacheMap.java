package utils;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CacheMap<K, V> {

    private final ConcurrentMap<K, SoftReference<V>> cache = new ConcurrentHashMap<>();

    private void cleanCache() {
        Set<Entry<K, SoftReference<V>>> entrySet = this.cache.entrySet();
        for (Entry<K, SoftReference<V>> entry : entrySet) {
            if (entry.getValue().get() == null) {
                entrySet.removeIf(entry::equals);
            }
        }
    }

    public V get(final K key) {
        this.cleanCache();
        return Optional.ofNullable(this.cache.get(key)) //
                .map(Reference::get) //
                .orElse(null);
    }

    public V put(final K key, final V value) {
        this.cleanCache();
        return Optional.ofNullable(this.cache.put(key, new SoftReference<>(value))) //
                .map(Reference::get) //
                .orElse(null);
    }

    public V putIfAbsent(final K key, final V value) {
        this.cleanCache();
        SoftReference<V> beforeRef;
        V before;
        do {
            // このputIfAbsentではreferenceが値を持たない場合でも
            // referenceが残っていればputに失敗する
            beforeRef = this.cache.putIfAbsent(key, new SoftReference<>(value));
            if (beforeRef == null) {
                // putに成功したのでその旨返す
                return null;
            }
            // putに成功しなかったが、見かけ上absentなときは再度cleanしてputを試みる。
            // 取得したreferenceが値を持っていれば(before != nullなら)再試行しない。
            this.cleanCache();
        } while ((before = beforeRef.get()) == null);
        return before;
    }

}
