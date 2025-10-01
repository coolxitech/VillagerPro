package cn.popcraft.villagepro.storage;


import cn.popcraft.villagepro.model.Village;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.Configuration;
import com.google.gson.Gson;


import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;


public abstract class SQLiteStorage {
    private final Plugin plugin;
    private final Gson gson;
    private Connection connection;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<Class<?>> registeredTables = new ArrayList<>();

    public SQLiteStorage(Plugin plugin, Gson gson) {
        this.plugin = plugin;
        this.gson = gson;
        connect();
    }

    public void updateTaskProgress(UUID playerId, UUID taskId, int progress) {
        String sql = "UPDATE player_tasks SET progress = ? WHERE player_uuid = ? AND task_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, progress);
            stmt.setString(2, playerId.toString());
            stmt.setString(3, taskId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void completeTask(UUID playerId, UUID taskId) {
        String sql = "UPDATE player_tasks SET completed = 1 WHERE player_uuid = ? AND task_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, taskId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public <T> void registerTable(Class<T> clazz) {
        if (!registeredTables.contains(clazz)) {
            registeredTables.add(clazz);
            try {
                createTable(clazz);
            } catch (SQLException e) {
                plugin.getLogger().severe("注册表失败: " + e.getMessage());
            }
        }
    }

    private void connect() {
        try {
            Configuration config = plugin.getConfig();
            String databasePath = config.getString("database.path", "plugins/VillagePro/data.db");
            
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            initializeTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("连接SQLite数据库失败: " + e.getMessage());
        }
    }

    private void initializeTables() {
        // 初始化村庄表
        String villagesSql = "CREATE TABLE IF NOT EXISTS villages ("
                + "id TEXT PRIMARY KEY, "
                + "ownerUuid TEXT, "
                + "data TEXT)";
        
        try {
            executeUpdate(villagesSql);
        } catch (SQLException e) {
            plugin.getLogger().severe("创建村庄数据库表失败: " + e.getMessage());
        }
        
        // 初始化任务表
        String tasksSql = "CREATE TABLE IF NOT EXISTS player_tasks ("
                + "id TEXT PRIMARY KEY, "
                + "data TEXT)";
        
        try {
            executeUpdate(tasksSql);
            plugin.getLogger().info("任务数据表初始化完成");
        } catch (SQLException e) {
            plugin.getLogger().severe("创建任务数据库表失败: " + e.getMessage());
        }
        
        // 初始化作物存储表
        String cropsSql = "CREATE TABLE IF NOT EXISTS crop_storages ("
                + "id TEXT PRIMARY KEY, "
                + "data TEXT)";
        
        try {
            executeUpdate(cropsSql);
            plugin.getLogger().info("作物存储表初始化完成");
        } catch (SQLException e) {
            plugin.getLogger().severe("创建作物存储数据库表失败: " + e.getMessage());
        }
    }

    public void save(Village village) {
        String sql = "INSERT OR REPLACE INTO villages(id, ownerUuid, data) VALUES(?,?,?)";
        
        try {
            String villageData = gson.toJson(village);
            executeUpdate(sql, village.getOwnerUuid().toString(), village.getOwnerUuid().toString(), villageData);
        } catch (SQLException e) {
            plugin.getLogger().severe("保存村庄数据失败: " + e.getMessage());
        }
    }

    public Collection<Village> findAll() {
        String sql = "SELECT data FROM villages";
        
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            
            Collection<Village> villages = new ArrayList<>();
            while (rs.next()) {
                String jsonData = rs.getString("data");
                Village village = gson.fromJson(jsonData, Village.class);
                villages.add(village);
            }
            return villages;
        } catch (SQLException e) {
            plugin.getLogger().severe("查找村庄数据失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    public <T> void save(T data, Function<T, String> idGenerator, String tableName) {
        try {
            String sql = "INSERT OR REPLACE INTO " + tableName + "(id, data) VALUES(?,?)";
            String jsonData = gson.toJson(data);
            executeUpdate(sql, idGenerator.apply(data), jsonData);
        } catch (SQLException e) {
            plugin.getLogger().severe("保存数据失败: " + e.getMessage());
        }
    }
    
    public <T> Collection<T> findAll(Class<T> clazz, String tableName) {
        String sql = "SELECT data FROM " + tableName;
        
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            
            Collection<T> dataList = new ArrayList<>();
            while (rs.next()) {
                String jsonData = rs.getString("data");
                T data = gson.fromJson(jsonData, clazz);
                dataList.add(data);
            }
            return dataList;
        } catch (SQLException e) {
            plugin.getLogger().severe("查找数据失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    public <T> void saveWithId(T data, String id, String tableName) {
        try {
            String sql = "INSERT OR REPLACE INTO " + tableName + "(id, data) VALUES(?,?)";
            String jsonData = gson.toJson(data);
            executeUpdate(sql, id, jsonData);
        } catch (SQLException e) {
            plugin.getLogger().severe("保存数据失败: " + e.getMessage());
        }
    }
    
    public <T> T findById(Class<T> clazz, String id, String tableName) {
        String sql = "SELECT data FROM " + tableName + " WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String jsonData = rs.getString("data");
                    return gson.fromJson(jsonData, clazz);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("查找数据失败: " + e.getMessage());
        }
        return null;
    }
    
    public void update(String tableName, String id, String column, Object value) {
        String sql = "UPDATE " + tableName + " SET " + column + " = ? WHERE id = ?";
        
        try {
            executeUpdate(sql, value, id);
        } catch (SQLException e) {
            plugin.getLogger().severe("更新数据失败: " + e.getMessage());
        }
    }
    
    public void delete(String tableName, String id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        
        try {
            executeUpdate(sql, id);
        } catch (SQLException e) {
            plugin.getLogger().severe("删除数据失败: " + e.getMessage());
        }
    }
    
    private void executeUpdate(String sql, Object... params) throws SQLException {
        Future<?> future = executor.submit(() -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    if (params[i] instanceof String) {
                        pstmt.setString(i + 1, (String) params[i]);
                    } else if (params[i] instanceof Integer) {
                        pstmt.setInt(i + 1, (Integer) params[i]);
                    } else if (params[i] instanceof Long) {
                        pstmt.setLong(i + 1, (Long) params[i]);
                    } else if (params[i] instanceof Double) {
                        pstmt.setDouble(i + 1, (Double) params[i]);
                    } else {
                        pstmt.setString(i + 1, params[i].toString());
                    }
                }
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("执行SQL更新失败: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
        
        try {
            future.get();
        } catch (Exception e) {
            throw new SQLException("执行异步操作失败", e);
        }
    }
    
    private <T> void createTable(Class<T> clazz) throws SQLException {
        // 默认实现，子类可以覆盖
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            executor.shutdown();
        } catch (SQLException e) {
            plugin.getLogger().severe("关闭数据库连接失败: " + e.getMessage());
        }
    }
}