package fr.arbres;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.LimitedRegion;

/** Surface sur laquelle on construit : la génération d'un chunk (LimitedRegion) ou le monde déjà chargé. */
interface Canvas {
    /** Type du bloc, ou null si on ne peut pas y toucher (hors zone / chunk non chargé). */
    Material type(int x, int y, int z);

    void set(int x, int y, int z, BlockData d);

    /** Hauteur à partir de laquelle on cherche le sol en descendant. */
    int topY(int x, int z);

    int minY();

    int maxY();

    /** Change le biome (par sa clé, ex. "basalt_deltas") de la cellule 4x4x4 qui contient ce point. */
    void setBiome(int x, int y, int z, String key);

    String biome(int x, int y, int z);

    /** Données du bloc (axe d'un tronc, distance d'une feuille...), ou null. */
    BlockData data(int x, int y, int z);
}

final class RegionCanvas implements Canvas {
    private final LimitedRegion r;
    private final int min;
    private final int max;

    RegionCanvas(LimitedRegion r, int min, int max) {
        this.r = r;
        this.min = min;
        this.max = max;
    }

    @Override
    public Material type(int x, int y, int z) {
        if (y < min || y >= max || !r.isInRegion(x, y, z)) return null;
        return r.getType(x, y, z);
    }

    @Override
    public void set(int x, int y, int z, BlockData d) {
        if (y < min || y >= max || !r.isInRegion(x, y, z)) return;
        r.setBlockData(x, y, z, d);
    }

    @Override
    public int topY(int x, int z) {
        return Math.min(max - 1, 280);
    }

    @Override
    public int minY() {
        return min;
    }

    @Override
    public int maxY() {
        return max;
    }

    @Override
    public void setBiome(int x, int y, int z, String key) {
        if (y < min || y >= max || !r.isInRegion(x, y, z)) return;
        Biome b = BiomeUtil.get(key);
        if (b != null) r.setBiome(x, y, z, b);
    }

    @Override
    public BlockData data(int x, int y, int z) {
        if (y < min || y >= max || !r.isInRegion(x, y, z)) return null;
        return r.getBlockData(x, y, z);
    }

    @Override
    public String biome(int x, int y, int z) {
        try {
            return r.getBiome(x, y, z).getKey().getKey();
        } catch (Exception e) {
            return "";
        }
    }
}

final class WorldCanvas implements Canvas {
    private final World w;

    WorldCanvas(World w) {
        this.w = w;
    }

    private boolean ok(int x, int y, int z) {
        return y >= w.getMinHeight() && y < w.getMaxHeight() && w.isChunkLoaded(x >> 4, z >> 4);
    }

    @Override
    public Material type(int x, int y, int z) {
        if (!ok(x, y, z)) return null;
        return w.getBlockAt(x, y, z).getType();
    }

    @Override
    public void set(int x, int y, int z, BlockData d) {
        if (!ok(x, y, z)) return;
        w.getBlockAt(x, y, z).setBlockData(d, false);
    }

    @Override
    public int topY(int x, int z) {
        return w.getHighestBlockYAt(x, z);
    }

    @Override
    public int minY() {
        return w.getMinHeight();
    }

    @Override
    public int maxY() {
        return w.getMaxHeight();
    }

    @Override
    public void setBiome(int x, int y, int z, String key) {
        if (!ok(x, y, z)) return;
        Biome b = BiomeUtil.get(key);
        if (b != null) w.setBiome(x, y, z, b);
    }

    @Override
    public BlockData data(int x, int y, int z) {
        if (!ok(x, y, z)) return null;
        return w.getBlockAt(x, y, z).getBlockData();
    }

    @Override
    public String biome(int x, int y, int z) {
        try {
            return w.getBiome(x, y, z).getKey().getKey();
        } catch (Exception e) {
            return "";
        }
    }
}

final class BiomeUtil {
    private BiomeUtil() {}

    private static final java.util.Map<String, Biome> CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    static Biome get(String key) {
        if (key == null || key.isBlank()) return null;
        Biome b = CACHE.get(key);
        if (b == null) {
            try {
                b = Registry.BIOME.get(NamespacedKey.minecraft(key));
            } catch (Throwable t) {
                b = null;
            }
            if (b != null) CACHE.put(key, b);
        }
        return b;
    }
}
