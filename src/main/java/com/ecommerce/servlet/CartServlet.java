package com.ecommerce.servlet;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.ApiResponse;
import com.ecommerce.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(CartServlet.class);
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            sendErrorResponse(response, "Unauthorized", 401);
            return;
        }
        
        try {
            String body = getRequestBody(request);
            JsonNode json = JsonUtil.parseJson(body);
            
            Integer productId = json.get("productId").asInt();
            Integer quantity = json.get("quantity").asInt();
            
            ProductDAO productDAO = new ProductDAO(getServletContext());
            Product product = productDAO.getProductById(productId);
            
            if (product == null) {
                sendErrorResponse(response, "Product not found", 404);
                return;
            }
            
            CartItem cartItem = new CartItem(userId, productId, quantity, product.getPrice());
            CartDAO cartDAO = new CartDAO(getServletContext());
            
            if (cartDAO.addToCart(cartItem)) {
                ApiResponse<CartItem> apiResponse = new ApiResponse<>(true, "Item added to cart successfully", cartItem, 201);
                sendSuccessResponse(response, apiResponse, 201);
                logger.info("Item added to cart for user: " + userId);
            } else {
                sendErrorResponse(response, "Failed to add item to cart", 500);
            }
        } catch (Exception e) {
            logger.error("Error adding to cart", e);
            sendErrorResponse(response, "Internal server error", 500);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            sendErrorResponse(response, "Unauthorized", 401);
            return;
        }
        
        try {
            CartDAO cartDAO = new CartDAO(getServletContext());
            List<CartItem> cartItems = cartDAO.getCartItems(userId);
            Double cartTotal = cartDAO.getCartTotal(userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("items", cartItems);
            data.put("total", cartTotal);
            
            ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>(true, "Cart retrieved successfully", data, 200);
            sendSuccessResponse(response, apiResponse, 200);
        } catch (Exception e) {
            logger.error("Error getting cart", e);
            sendErrorResponse(response, "Internal server error", 500);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            sendErrorResponse(response, "Unauthorized", 401);
            return;
        }
        
        try {
            String pathInfo = request.getPathInfo();
            String[] parts = pathInfo.split("/");
            
            if (parts.length < 2) {
                sendErrorResponse(response, "Invalid request", 400);
                return;
            }
            
            Integer cartId = Integer.parseInt(parts[1]);
            String body = getRequestBody(request);
            JsonNode json = JsonUtil.parseJson(body);
            Integer quantity = json.get("quantity").asInt();
            
            CartDAO cartDAO = new CartDAO(getServletContext());
            if (cartDAO.updateCartItem(cartId, quantity)) {
                ApiResponse<Void> apiResponse = new ApiResponse<>(true, "Cart item updated successfully", null, 200);
                sendSuccessResponse(response, apiResponse, 200);
                logger.info("Cart item updated: " + cartId);
            } else {
                sendErrorResponse(response, "Failed to update cart item", 500);
            }
        } catch (Exception e) {
            logger.error("Error updating cart item", e);
            sendErrorResponse(response, "Internal server error", 500);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            sendErrorResponse(response, "Unauthorized", 401);
            return;
        }
        
        try {
            String pathInfo = request.getPathInfo();
            String[] parts = pathInfo.split("/");
            
            if (parts.length < 2) {
                sendErrorResponse(response, "Invalid request", 400);
                return;
            }
            
            Integer cartId = Integer.parseInt(parts[1]);
            CartDAO cartDAO = new CartDAO(getServletContext());
            
            if (cartDAO.removeFromCart(cartId)) {
                ApiResponse<Void> apiResponse = new ApiResponse<>(true, "Item removed from cart successfully", null, 200);
                sendSuccessResponse(response, apiResponse, 200);
                logger.info("Item removed from cart: " + cartId);
            } else {
                sendErrorResponse(response, "Failed to remove item from cart", 500);
            }
        } catch (Exception e) {
            logger.error("Error removing from cart", e);
            sendErrorResponse(response, "Internal server error", 500);
        }
    }
    
    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
    
    private void sendSuccessResponse(HttpServletResponse response, Object data, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write(JsonUtil.toJson(data));
    }
    
    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        ApiResponse<?> apiResponse = new ApiResponse<>(false, message, null, statusCode);
        response.getWriter().write(JsonUtil.toJson(apiResponse));
    }
}