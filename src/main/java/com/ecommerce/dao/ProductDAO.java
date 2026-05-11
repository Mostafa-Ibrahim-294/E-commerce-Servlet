package com.ecommerce.dao;

import com.ecommerce.config.DatabaseConfig;
import com.ecommerce.config.RedisConfig;
import com.ecommerce.model.Product;
import com.ecommerce.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);
    private static final String PRODUCTS_CACHE_KEY = "products_list";
    private static final long CACHE_TTL = 3600; // 1 hour
    
    public List<Product> getAllProducts() {
        // Try to get from Redis cache first
        try (Jedis jedis = RedisConfig.getJedisPool().getResource()) {
            String cached = jedis.get(PRODUCTS_CACHE_KEY);
            if (cached != null) {
                logger.info("Products fetched from Redis cache");
                return JsonUtil.fromJson(cached, List.class);
            }
        } catch (Exception e) {
            logger.warn("Error accessing Redis cache", e);
        }
        
        // If not in cache, fetch from database
        String sql = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            
            // Cache the results in Redis
            try (Jedis jedis = RedisConfig.getJedisPool().getResource()) {
                jedis.setex(PRODUCTS_CACHE_KEY, CACHE_TTL, JsonUtil.toJson(products));
                logger.info("Products cached in Redis");
            } catch (Exception e) {
                logger.warn("Error caching products in Redis", e);
            }
            
        } catch (SQLException e) {
            logger.error("Error getting all products", e);
        }
        
        return products;
    }
    
    public Product getProductById(Integer id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting product by ID", e);
        }
        return null;
    }
    
    public boolean createProduct(Product product) {
        String sql = "INSERT INTO products (name, description, price, quantity, categoryId) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantity());
            stmt.setInt(5, product.getCategoryId());
            
            boolean result = stmt.executeUpdate() > 0;
            invalidateCache();
            return result;
        } catch (SQLException e) {
            logger.error("Error creating product", e);
        }
        return false;
    }
    
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, quantity = ?, categoryId = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantity());
            stmt.setInt(5, product.getCategoryId());
            stmt.setInt(6, product.getId());
            
            boolean result = stmt.executeUpdate() > 0;
            invalidateCache();
            return result;
        } catch (SQLException e) {
            logger.error("Error updating product", e);
        }
        return false;
    }
    
    public boolean deleteProduct(Integer id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            invalidateCache();
            return result;
        } catch (SQLException e) {
            logger.error("Error deleting product", e);
        }
        return false;
    }
    
    private void invalidateCache() {
        try (Jedis jedis = RedisConfig.getJedisPool().getResource()) {
            jedis.del(PRODUCTS_CACHE_KEY);
            logger.info("Product cache invalidated");
        } catch (Exception e) {
            logger.warn("Error invalidating cache", e);
        }
    }
    
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setQuantity(rs.getInt("quantity"));
        product.setCategoryId(rs.getInt("categoryId"));
        product.setCreatedAt(rs.getTimestamp("createdAt").getTime());
        return product;
    }
}