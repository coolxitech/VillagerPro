// src/main/java/cn/popcraft/villagepro/manager/CropManager.java
package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.CropStorage;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CropManager {
    private final VillagePro plugin;
    private final Map<UUID, CropStorage> cropStorageCache;
    private Connection connection;
    
    public CropManager(VillagePro plugin) {
        this.plugin = plugin;
        this.cropStorageCache = new ConcurrentHashMap<>();
        initializeDatabase();
    }
    
    /**
     * 初始化数据库
     */
    private void initializeDatabase() {
        try {
            // 创建数据库连接
            String dbPath = plugin.getDataFolder().getAbsolutePath() + "/crops.db";
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            
            // 创建表
            createTables();
            
            plugin.getLogger().info("作物数据库初始化成功");
        } catch (SQLException e) {
            plugin.getLogger().severe("作物数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建数据库表
     */
    private void createTables() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS crop_storage (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_uuid TEXT NOT NULL,
                crop_type TEXT NOT NULL,
                amount INTEGER NOT NULL DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(player_uuid, crop_type)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            
            // 创建索引
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_uuid ON crop_storage(player_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_crop_type ON crop_storage(crop_type)");
        }
    }
    
    /**
     * 获取玩家的作物存储
     */
    public CropStorage getPlayerCropStorage(UUID playerId) {
        // 先从缓存中获取
        CropStorage cached = cropStorageCache.get(playerId);
        if (cached != null) {
            return cached;
        }
        
        // 从数据库加载
        CropStorage storage = loadFromDatabase(playerId);
        cropStorageCache.put(playerId, storage);
        return storage;
    }
    
    /**
     * 从数据库加载玩家作物数据
     */
    private CropStorage loadFromDatabase(UUID playerId) {
        Map<String, Integer> crops = new HashMap<>();
        
        String sql = "SELECT crop_type, amount FROM crop_storage WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String cropType = rs.getString("crop_type");
                    int amount = rs.getInt("amount");
                    crops.put(cropType, amount);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("加载玩家作物数据失败: " + e.getMessage());
        }
        
        return new CropStorage(playerId, crops);
    }
    
    /**
     * 保存作物存储到数据库
     */
    public void saveCropStorage(CropStorage storage) {
        UUID playerId = storage.getPlayerId();
        
        // 异步保存到数据库
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // 先删除玩家的所有作物记录
                String deleteSQL = "DELETE FROM crop_storage WHERE player_uuid = ?";
                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSQL)) {
                    deleteStmt.setString(1, playerId.toString());
                    deleteStmt.executeUpdate();
                }
                
                // 插入新的作物数据
                String insertSQL = "INSERT INTO crop_storage (player_uuid, crop_type, amount) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
                    for (Map.Entry<String, Integer> entry : storage.getCrops().entrySet()) {
                        if (entry.getValue() > 0) {
                            insertStmt.setString(1, playerId.toString());
                            insertStmt.setString(2, entry.getKey());
                            insertStmt.setInt(3, entry.getValue());
                            insertStmt.addBatch();
                        }
                    }
                    insertStmt.executeBatch();
                }
                
            } catch (SQLException e) {
                plugin.getLogger().warning("保存作物数据失败: " + e.getMessage());
            }
        });
        
        // 更新缓存
        cropStorageCache.put(playerId, storage);
    }
    
    /**
     * 添加作物
     */
    public boolean addCrop(UUID playerId, String cropType, int amount) {
        if (amount <= 0) return false;
        
        CropStorage storage = getPlayerCropStorage(playerId);
        storage.addCrop(cropType, amount);
        saveCropStorage(storage);
        return true;
    }
    
    /**
     * 移除作物
     */
    public boolean removeCrop(UUID playerId, String cropType, int amount) {
        if (amount <= 0) return false;
        
        CropStorage storage = getPlayerCropStorage(playerId);
        if (!storage.removeCrop(cropType, amount)) {
            return false;
        }
        
        saveCropStorage(storage);
        return true;
    }
    
    /**
     * 检查玩家是否有足够的作物
     */
    public boolean hasCrop(UUID playerId, String cropType, int amount) {
        CropStorage storage = getPlayerCropStorage(playerId);
        return storage.hasCrop(cropType, amount);
    }
    
    /**
     * 获取作物数量
     */
    public int getCropAmount(UUID playerId, String cropType) {
        CropStorage storage = getPlayerCropStorage(playerId);
        return storage.getCropAmount(cropType);
    }
    
    /**
     * 获取所有可用的作物类型
     */
    public List<String> getAvailableCropTypes() {
        return Arrays.asList(
            "wheat", "carrot", "potato", "beetroot", 
            "pumpkin", "melon", "cocoa", "sugar_cane",
            "nether_wart", "chorus_fruit", "sweet_berries"
        );
    }
    
    /**
     * 获取作物生长加成
     */
    public double getCropGrowthBonus(int level) {
        return 0.1 * level;
    }
    
    /**
     * 获取作物收获加成
     */
    public double getCropHarvestBonus(int level) {
        return 0.1 * level;
    }
    
    /**
     * 获取作物的显示名称
     */
    public String getCropDisplayName(String cropType) {
        Map<String, String> displayNames = new HashMap<>();
        displayNames.put("wheat", "小麦");
        displayNames.put("carrot", "胡萝卜");
        displayNames.put("potato", "土豆");
        displayNames.put("beetroot", "甜菜根");
        displayNames.put("pumpkin", "南瓜");
        displayNames.put("melon", "西瓜");
        displayNames.put("cocoa", "可可豆");
        displayNames.put("sugar_cane", "甘蔗");
        displayNames.put("nether_wart", "地狱疣");
        displayNames.put("chorus_fruit", "紫颂果");
        displayNames.put("sweet_berries", "甜浆果");
        
        return displayNames.getOrDefault(cropType.toLowerCase(), cropType);
    }
    
    /**
     * 获取排行榜数据
     */
    public Map<UUID, Integer> getTopCropProducers(String cropType, int limit) {
        Map<UUID, Integer> topProducers = new LinkedHashMap<>();
        
        String sql = "SELECT player_uuid, amount FROM crop_storage WHERE crop_type = ? ORDER BY amount DESC LIMIT ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cropType.toLowerCase());
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID playerId = UUID.fromString(rs.getString("player_uuid"));
                    int amount = rs.getInt("amount");
                    topProducers.put(playerId, amount);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("获取排行榜数据失败: " + e.getMessage());
        }
        
        return topProducers;
    }
    
    /**
     * 清理缓存
     */
    public void clearCache() {
        cropStorageCache.clear();
    }
    
    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("关闭数据库连接失败: " + e.getMessage());
        }
    }
}
