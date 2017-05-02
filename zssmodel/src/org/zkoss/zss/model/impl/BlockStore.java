package org.zkoss.zss.model.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.zkoss.util.logging.Log;

import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class represents an external memory block storage system.
 */
class BlockStore {
    /**
     * Logging
     */
    private static final Log _logger = Log.lookup(BlockStore.class);
    final int CACHE_SIZE = 1000;
    /* Table that persists the block store */
    private String dataStore;
    /**
     * A list of blocks
     */

    private Map<Integer, Object> blockCache;
    private Map<Integer, Object> dirtyBlocks;
    private Set<Integer> deletedBlocks;
    private int inMemBlockId = 1;
    /* Serializer */
    private Kryo kryo;

    // In Memory Block store
    BlockStore() {
        this(null, null);
    }

    // On DB block store.
    BlockStore(DBContext context, String dataStore) {
        dirtyBlocks = new HashMap<>();
        deletedBlocks = new HashSet<>();
        kryo = new Kryo();
        this.dataStore = dataStore;
        if (dataStore == null) {
            // Infinite cache size for in memory
            blockCache = new LruCache<>(-1);
        } else {
            blockCache = new LruCache<>(CACHE_SIZE);
            createSchema(context, dataStore);
        }
        //logger.info("BlockStore created - " + dataStore);
    }

    public String getDataStore() {
        return dataStore;
    }

    // Do not call any thing after drop schema.
    public void dropSchemaAndClear(DBContext context) {
        blockCache.clear();
        dirtyBlocks.clear();
        deletedBlocks.clear();
        if (dataStore == null)
            return;

        try (Statement stmt = context.getConnection().createStatement()) {
            String createTable = (new StringBuilder())
                    .append("DROP TABLE ")
                    .append(dataStore)
                    .toString();

            stmt.execute(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        dataStore = null;
    }

    public void createSchema(DBContext context, String dataStore) {
        if (dataStore == null) {
            return;
        }
        this.dataStore = dataStore;

        try (Statement stmt = context.getConnection().createStatement()) {
            String createTable = (new StringBuilder())
                    .append("CREATE TABLE IF NOT EXISTS ")
                    .append(dataStore)
                    .append("(block_id SERIAL PRIMARY KEY, data BYTEA)")
                    .toString();

            stmt.execute(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* Does not flush */
    public void putObject(int block_id, Object obj) {
        blockCache.put(block_id, obj);
        dirtyBlocks.put(block_id, obj);
    }


    public void clearCache() {
        if (!dirtyBlocks.isEmpty())
            throw new RuntimeException("Cannot clear - pending dirty blocks");
        blockCache.clear();
    }

    /**
     * Allocate a new block_id
     *
     * @return new block_id
     */
    public int getNewBlockID(DBContext context) {
        if (dataStore == null) {
            return inMemBlockId++;
        }
        int id = -1;

        String insert = "SELECT nextval(?)";
        try (PreparedStatement stmt = context.getConnection().prepareStatement(insert)) {
            stmt.setString(1, dataStore + "_block_id_seq");
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                id = rs.getInt(1);
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * Read a block
     *
     * @param block_id the index of the block to read
     * @return the block
     */
    public <T> T getObject(DBContext context, int block_id, Class<T> type) {
        T obj = (T) blockCache.get(block_id);
        if (obj != null || dataStore == null)
            return obj;
        obj = (T) dirtyBlocks.get(block_id);
        if (obj != null)
            return obj;

        String read = "SELECT data FROM " + dataStore + " WHERE block_id = ?";

        try (PreparedStatement stmt = context.getConnection().prepareStatement(read)) {
            stmt.setInt(1, block_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Input in = new Input(rs.getBytes(1));
                obj = kryo.readObject(in, type);
                in.close();
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        blockCache.put(block_id, obj);
        return obj;
    }

    public void flushDirtyBlocks(DBContext context) {
        if (dataStore == null) {
            dirtyBlocks.clear();
            deletedBlocks.stream().forEach(e -> blockCache.remove(e));
            deletedBlocks.clear();
            return;
        }

        String insertOrUpdate = (new StringBuilder())
                .append("INSERT INTO ")
                .append(dataStore)
                .append("(block_id, data) VALUES (?,?) ON CONFLICT (block_id) DO UPDATE set data = EXCLUDED.data")
                .toString();
        try (PreparedStatement stmt = context.getConnection().prepareStatement(insertOrUpdate)) {
            for (Map.Entry<Integer, Object> blockEntry : dirtyBlocks.entrySet()) {
                stmt.setInt(1, blockEntry.getKey());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Output out = new Output(byteArrayOutputStream);
                kryo.writeObject(out, blockEntry.getValue());
                stmt.setBytes(2, out.toBytes());
                out.close();
                byteArrayOutputStream.close();
                stmt.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dirtyBlocks.clear();

        // Delete blocks
        String free = "DELETE FROM " + dataStore + " WHERE block_id = ?";
        try (PreparedStatement stmt = context.getConnection().prepareStatement(free)) {
            for (int block_id : deletedBlocks) {
                stmt.setInt(1, block_id);
                stmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void freeBlock(int block_id) {
        deletedBlocks.add(block_id);
    }

}
