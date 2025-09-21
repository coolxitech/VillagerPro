package cn.popcraft.villagepro.storage;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.Village;
import cn.popcraft.villagepro.model.PlayerTaskData;
import cn.popcraft.villagepro.model.Task;
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
        String villagesSql = "CREATE TABLE IF NOT EXISTS villages ("
                + "id TEXT PRIMARY KEY, "
                + "ownerUuid TEXT, "
                + "data TEXT)";
        
        String cropsSql = "CREATE TABLE IF NOT EXISTS crops ("
                + "id TEXT PRIMARY KEY, "
                + "data TEXT)";
        
        String tasksSql = "CREATE TABLE IF NOT EXISTS player_tasks ("
                + "id TEXT PRIMARY KEY, "
                + "data TEXT)";
        
        try (Statement stmt = sqliteConnection.createStatement()) {
            stmt.execute(villagesSql);
            stmt.execute(cropsSql);
            stmt.execute(tasksSql);
            
            // 检查并升级表结构（兼容旧版本）
            upgradeTableStructure(stmt);
            
            plugin.getLogger().info("所有数据表初始化完成");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "创建数据库表失败", e);
        }
    }
    
    /**
     * 升级表结构以兼容旧版本
     * @param stmt 数据库语句对象
     * @throws SQLException SQL异常
     */
    private void upgradeTableStructure(Statement stmt) throws SQLException {
        // 检查villages表结构是否需要升级
        try {
            // 尝试查询新的表结构
            stmt.executeQuery("SELECT id, ownerUuid, data FROM villages LIMIT 0");
        } catch (SQLException e) {
            // 如果查询失败，说明表结构不匹配，需要升级
            plugin.getLogger().info("检测到旧版数据库结构，正在进行升级...");
            
            try {
                // 检查是否存在旧表结构
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(villages)");
                boolean hasIdColumn = false;
                boolean hasOwnerUuidColumn = false;
                boolean hasDataColumn = false;
                
                while (rs.next()) {
                    String columnName = rs.getString("name");
                    if ("id".equals(columnName)) {
                        hasIdColumn = true;
                    } else if ("ownerUuid".equals(columnName)) {
                        hasOwnerUuidColumn = true;
                    } else if ("data".equals(columnName)) {
                        hasDataColumn = true;
                    }
                }
                
                // 如果缺少id列，则需要升级表结构
                if (!hasIdColumn) {
                    // 检查旧表结构
                    try {
                        stmt.executeQuery("SELECT ownerUuid, data FROM villages_old LIMIT 0");
                        // 如果存在villages_old表，则从该表迁移数据
                        stmt.execute("DROP TABLE villages");
                        stmt.execute("ALTER TABLE villages_old RENAME TO villages");
                        plugin.getLogger().info("从备份表恢复村庄数据表结构");
                    } catch (SQLException ex) {
                        // 旧表不存在，创建新表
                        stmt.execute("DROP TABLE villages");
                        
                        String newVillagesSql = "CREATE TABLE villages (" +
                                "id TEXT PRIMARY KEY, " +
                                "ownerUuid TEXT, " +
                                "data TEXT)";
                        stmt.execute(newVillagesSql);
                        
                        plugin.getLogger().info("重新创建村庄数据表");
                    }
                } else if (!hasOwnerUuidColumn) {
                    // 如果有id列但没有ownerUuid列
                    stmt.execute("ALTER TABLE villages ADD COLUMN ownerUuid TEXT");
                    plugin.getLogger().info("村庄数据表结构已更新，添加了ownerUuid列");
                }
                
                plugin.getLogger().info("村庄数据表结构升级完成");
            } catch (SQLException upgradeException) {
                plugin.getLogger().log(Level.WARNING, "升级村庄数据表结构时发生错误，建议备份数据后删除数据库文件重新生成", upgradeException);
            }
        }
        
        // 类似地处理其他表的结构检查
        checkAndUpgradeOtherTables(stmt);
    }

    /**
     * 检查并升级其他表结构
     * @param stmt 数据库语句对象
     * @throws SQLException SQL异常
     */
    private void checkAndUpgradeOtherTables(Statement stmt) throws SQLException {
        // 检查crops表
        try {
            stmt.executeQuery("SELECT id, data FROM crops LIMIT 0");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "作物数据表结构检查失败，可能需要手动修复");
        }

        // 检查player_tasks表
        try {
            stmt.executeQuery("SELECT id, data FROM player_tasks LIMIT 0");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "任务数据表结构检查失败，可能需要手动修复");
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
            stmt.setString(1, village.getOwnerUuid().toString()); // id列
            stmt.setString(2, village.getOwnerUuid().toString()); // ownerUuid列
            stmt.setString(3, json); // data列
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
        try {
            // 首先加载玩家的任务数据
            String sql = "SELECT data FROM player_tasks WHERE id = ?";
            try (PreparedStatement selectStmt = sqliteConnection.prepareStatement(sql)) {
                selectStmt.setString(1, playerId.toString());
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        String jsonData = rs.getString("data");
                        PlayerTaskData taskData = gson.fromJson(jsonData, PlayerTaskData.class);
                        if (taskData != null) {
                            // 更新任务进度
                            Task task = taskData.getTaskById(taskId);
                            if (task != null) {
                                task.setProgress(progress);
                                
                                // 保存更新后的数据
                                String updatedJson = gson.toJson(taskData);
                                String updateSql = "INSERT OR REPLACE INTO player_tasks(id, data) VALUES(?,?)";
                                try (PreparedStatement updateStmt = sqliteConnection.prepareStatement(updateSql)) {
                                    updateStmt.setString(1, playerId.toString());
                                    updateStmt.setString(2, updatedJson);
                                    updateStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "更新任务进度失败: 玩家=" + playerId + ", 任务=" + taskId + ", 进度=" + progress, e);
        }
    }
    
    /**
     * 完成任务
     * @param playerId 玩家ID
     * @param taskId 任务ID
     */
    public void completeTask(UUID playerId, UUID taskId) {
        try {
            // 首先加载玩家的任务数据
            String sql = "SELECT data FROM player_tasks WHERE id = ?";
            try (PreparedStatement selectStmt = sqliteConnection.prepareStatement(sql)) {
                selectStmt.setString(1, playerId.toString());
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        String jsonData = rs.getString("data");
                        PlayerTaskData taskData = gson.fromJson(jsonData, PlayerTaskData.class);
                        if (taskData != null) {
                            // 移除已完成的任务
                            taskData.removeTask(taskId);
                            
                            // 保存更新后的数据
                            String updatedJson = gson.toJson(taskData);
                            String updateSql = "INSERT OR REPLACE INTO player_tasks(id, data) VALUES(?,?)";
                            try (PreparedStatement updateStmt = sqliteConnection.prepareStatement(updateSql)) {
                                updateStmt.setString(1, playerId.toString());
                                updateStmt.setString(2, updatedJson);
                                updateStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "完成任务失败: 玩家=" + playerId + ", 任务=" + taskId, e);
        }
    }
    
    /**
     * 注册数据表
     * @param clazz 数据模型类
     */
    public <T> void registerTable(Class<T> clazz) {
        String tableName = getTableName(clazz);
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "id TEXT PRIMARY KEY, "
                + "data TEXT)";
        
        try (Statement stmt = sqliteConnection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("数据表 " + tableName + " 初始化完成");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "创建数据表失败: " + tableName, e);
        }
    }
    
    /**
     * 根据类名获取表名
     * @param clazz 类
     * @return 表名
     */
    private <T> String getTableName(Class<T> clazz) {
        // 使用类名的小写形式作为表名
        return clazz.getSimpleName().toLowerCase();
    }
    
    /**
     * 查找所有指定类型的数据
     * @param clazz 数据类型
     * @param tableName 表名
     * @return 数据集合
     */
    public <T> Collection<T> findAll(Class<T> clazz, String tableName) {
        String sql = "SELECT data FROM " + tableName;
        try (Statement stmt = sqliteConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            Collection<T> results = new ArrayList<>();
            while (rs.next()) {
                String jsonData = rs.getString("data");
                T obj = gson.fromJson(jsonData, clazz);
                if (obj != null) {
                    results.add(obj);
                }
            }
            return results;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "查找所有数据失败: 类型=" + clazz.getSimpleName() + ", 表名=" + tableName, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 保存数据（带ID和表名）
     * @param data 数据对象
     * @param id 数据ID
     * @param tableName 表名
     */
    public <T> void saveWithId(T data, String id, String tableName) {
        try {
            String json = gson.toJson(data);
            String sql = "INSERT OR REPLACE INTO " + tableName + "(id, data) VALUES(?,?)";
            try (PreparedStatement stmt = sqliteConnection.prepareStatement(sql)) {
                stmt.setString(1, id);
                stmt.setString(2, json);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "保存数据失败: 类型=" + data.getClass().getSimpleName() + ", ID=" + id + ", 表名=" + tableName, e);
        }
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