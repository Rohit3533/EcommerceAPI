import { createContext, useContext, useState, useEffect } from 'react';
import { cartAPI } from '../api/api';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) {
        throw new Error('useCart must be used within CartProvider');
    }
    return context;
};

export const CartProvider = ({ children }) => {
    const { isAuthenticated } = useAuth();
    const [cart, setCart] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isAuthenticated) {
            fetchCart();
        } else {
            setCart(null);
        }
    }, [isAuthenticated]);

    const fetchCart = async () => {
        try {
            setLoading(true);
            const data = await cartAPI.get();
            setCart(data);
        } catch (error) {
            console.error('Error fetching cart:', error);
        } finally {
            setLoading(false);
        }
    };

    const addToCart = async (productId, quantity = 1) => {
        await cartAPI.add(productId, quantity);
        await fetchCart();
    };

    const updateQuantity = async (productId, quantity) => {
        if (quantity <= 0) {
            await removeFromCart(productId);
            return;
        }
        await cartAPI.update(productId, quantity);
        await fetchCart();
    };

    const removeFromCart = async (productId) => {
        await cartAPI.remove(productId);
        await fetchCart();
    };

    const clearCart = async () => {
        await cartAPI.clear();
        await fetchCart();
    };

    const itemCount = cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;

    const totalAmount = cart?.items?.reduce(
        (sum, item) => sum + item.price * item.quantity,
        0
    ) || 0;

    return (
        <CartContext.Provider
            value={{
                cart,
                loading,
                itemCount,
                totalAmount,
                fetchCart,
                addToCart,
                updateQuantity,
                removeFromCart,
                clearCart,
            }}
        >
            {children}
        </CartContext.Provider>
    );
};
