package cn.popcraft.villagepro.storage;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.Village;
import com.google.gson.Gson;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * 村庄存储管理器
 * 负责村庄数据的持久化存储和检索
 * 使用SQLite数据库进行数据存储
 */
public class VillageStorage {
    private final VillagePro plugin;
    private final Gson gson;
    private Connection sqliteConnection;

    public VillageStorage(VillagePro plugin, Gson gson) {
        this.plugin = plugin;
        this.gson = gson;
        initializeSQLite();
    }

    /**
     * 初始化SQLite数据库连接
     */
    private void initializeSQLite() {
        try {
            // 连接到SQLite数据库
            String databasePath = plugin.getDataFolder().getAbsolutePath() + File.separator + "data.db";
            File dbFile = new File(databasePath);
            
            // 如果数据库文件不存在，创建数据文件夹
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            
            sqliteConnection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            initializeTables();
            plugin.getLogger().info("成功连接到SQLite数据库: " + databasePath);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "连接SQLite数据库失败", e);
        }
    }

    /**
     * 初始化数据库表
     */
    private void initializeTables() {
        String sql = "CREATE TABLE IF NOT EXISTS villages ("
                + "id TEXT PRIMARY KEY, "
                + "ownerUuid TEXT, "
                + "data TEXT)";
        
        try (Statement stmt = sqliteConnection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("村庄数据表初始化完成");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "创建数据库表失败", e);
        }
    }

    /**
     * 保存村庄数据
     *
     * @param village 村庄对象
     * @return 是否保存成功
     */
    public CompletableFuture<Boolean> saveVillage(Village village) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = gson.toJson(village);
                saveToSQLite(village, json);
                return true;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "保存村庄数据失败: " + village.getOwnerUuid(), e);
                return false;
            }
        }).exceptionally(throwable -> {
            plugin.getLogger().log(Level.SEVERE, "保存村庄数据时发生异常: " + village.getOwnerUuid(), throwable);
            return false;
        });
    }

    /**
     * 保存到SQLite数据库
     */
    private void saveToSQLite(Village village, String json) throws SQLException {
        String sql = "INSERT OR REPLACE INTO villages(id, ownerUuid, data) VALUES(?,?,?)";
        try (PreparedStatement stmt = sqliteConnection.prepareStatement(sql)) {
            stmt.setString(1, village.getOwnerUuid().toString());
            stmt.setString(2, village.getOwnerUuid().toString());
            stmt.setString(3, json);
            stmt.executeUpdate();
        }
    }

    /**
     * 加载村庄数据
     *
     * @param ownerUuid 玩家UUID
     * @return 村庄对象，如果不存在则返回null
     */
    public CompletableFuture<Village> loadVillage(UUID ownerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadFromSQLite(ownerUuid);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "加载村庄数据失败: " + ownerUuid, e);
                return null;
            }
        }).exceptionally(throwable -> {
            plugin.getLogger().log(Level.SEVERE, "加载村庄数据时发生异常: " + ownerUuid, throwable);
            return null;
        });
    }

    /**
     * 从SQLite数据库加载
     */
    private Village loadFromSQLite(UUID ownerUuid) throws SQLException {
        String sql = "SELECT data FROM villages WHERE ownerUuid = ?";
        try (PreparedStatement stmt = sqliteConnection.prepareStatement(sql)) {
            stmt.setString(1, ownerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("data");
                    return gson.fromJson(json, Village.class);
                }
            }
        }
        return null;
    }

    /**
     * 删除村庄数据
     *
     * @param ownerUuid 玩家UUID
     * @return 是否删除成功
     */
    public CompletableFuture<Boolean> deleteVillage(UUID ownerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                deleteFromSQLite(ownerUuid);
                return true;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "删除村庄数据失败: " + ownerUuid, e);
                return false;
            }
        }).exceptionally(throwable -> {
            plugin.getLogger().log(Level.SEVERE, "删除村庄数据时发生异常: " + ownerUuid, throwable);
            return false;
        });
    }

    /**
     * 从SQLite数据库删除
     */
    private void deleteFromSQLite(UUID ownerUuid) throws SQLException {
        String sql = "DELETE FROM villages WHERE ownerUuid = ?";
        try (PreparedStatement stmt = sqliteConnection.prepareStatement(sql)) {
            stmt.setString(1, ownerUuid.toString());
            stmt.executeUpdate();
        }
    }

    /**
     * 检查村庄是否存在
     *
     * @param ownerUuid 玩家UUID
     * @return 是否存在
     */
    public CompletableFuture<Boolean> exists(UUID ownerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return existsInSQLite(ownerUuid);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "检查村庄数据存在性时发生异常: " + ownerUuid, e);
                return false;
            }
        }).exceptionally(throwable -> {
            plugin.getLogger().log(Level.SEVERE, "检查村庄数据存在性时发生异常: " + ownerUuid, throwable);
            return false;
        });
    }

    /**
     * 检查村庄在SQLite数据库中是否存在
     */
    private boolean existsInSQLite(UUID ownerUuid) throws SQLException {
        String sql = "SELECT 1 FROM villages WHERE ownerUuid = ? LIMIT 1";
        try (PreparedStatement stmt = sqliteConnection.prepareStatement(sql)) {
            stmt.setString(1, ownerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * 获取所有村庄的所有者UUID
     *
     * @return 所有村庄的所有者UUID列表
     */
    public CompletableFuture<UUID[]> getAllVillageOwners() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAllVillageOwnersFromSQLite();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "获取所有村庄所有者时发生异常", e);
                return new UUID[0];
            }
        }).exceptionally(throwable -> {
            plugin.getLogger().log(Level.SEVERE, "获取所有村庄所有者时发生异常", throwable);
            return new UUID[0];
        });
    }

    /**
     * 从SQLite数据库获取所有村庄所有者
     */
    private UUID[] getAllVillageOwnersFromSQLite() throws SQLException {
        String sql = "SELECT DISTINCT ownerUuid FROM villages";
        try (Statement stmt = sqliteConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            List<UUID> uuidList = new ArrayList<>();
            while (rs.next()) {
                uuidList.add(UUID.fromString(rs.getString("ownerUuid")));
            }
            
            return uuidList.toArray(new UUID[0]);
        }
    }

    /**
     * 查找所有村庄数据（兼容旧版SQLiteStorage接口）
     *
     * @return 所有村庄数据集合
     */
    public Collection<Village> findAll() {
        String sql = "SELECT data FROM villages";
        try (Statement stmt = sqliteConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            Collection<Village> villages = new ArrayList<>();
            while (rs.next()) {
                String jsonData = rs.getString("data");
                Village village = gson.fromJson(jsonData, Village.class);
                if (village != null) {
                    villages.add(village);
                }
            }
            return villages;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "查找所有村庄数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 保存村庄数据（兼容旧版SQLiteStorage接口）
     *
     * @param village 村庄对象
     */
    public void save(Village village) throws SQLException {
        String json = gson.toJson(village);
        saveToSQLite(village, json);
    }
    
    /**
     * 更新任务进度
     * @param playerId 玩家ID
     * @param taskId 任务ID
     * @param progress 进度
     */
    public void updateTaskProgress(UUID playerId, UUID taskId, int progress) {
        // 在实际实现中，这里应该更新数据库中的任务进度
        plugin.getLogger().info("更新任务进度: 玩家=" + playerId + ", 任务=" + taskId + ", 进度=" + progress);
    }
    
    /**
     * 完成任务
     * @param playerId 玩家ID
     * @param taskId 任务ID
     */
    public void completeTask(UUID playerId, UUID taskId) {
        // 在实际实现中，这里应该标记任务为已完成
        plugin.getLogger().info("完成任务: 玩家=" + playerId + ", 任务=" + taskId);
    }
    
    /**
     * 注册数据表
     * @param clazz 数据模型类
     */
    public <T> void registerTable(Class<T> clazz) {
        // 在实际实现中，这里应该注册数据表
        plugin.getLogger().info("注册数据表: " + clazz.getSimpleName());
    }
    
    /**
     * 查找所有指定类型的数据
     * @param clazz 数据类型
     * @param tableName 表名
     * @return 数据集合
     */
    public <T> Collection<T> findAll(Class<T> clazz, String tableName) {
        // 在实际实现中，这里应该从数据库加载所有指定类型的数据
        plugin.getLogger().info("查找所有数据: 类型=" + clazz.getSimpleName() + ", 表名=" + tableName);
        return new ArrayList<>();
    }
    
    /**
     * 保存数据（带ID和表名）
     * @param data 数据对象
     * @param id 数据ID
     * @param tableName 表名
     */
    public <T> void saveWithId(T data, String id, String tableName) {
        // 在实际实现中，这里应该将数据保存到指定表中
        plugin.getLogger().info("保存数据: 类型=" + data.getClass().getSimpleName() + ", ID=" + id + ", 表名=" + tableName);
    }
    
    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (sqliteConnection != null && !sqliteConnection.isClosed()) {
                sqliteConnection.close();
                plugin.getLogger().info("SQLite数据库连接已关闭");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "关闭SQLite数据库连接失败", e);
        }
    }
}