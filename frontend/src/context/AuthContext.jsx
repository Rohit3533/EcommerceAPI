import { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../api/api';

const AuthContext = createContext(null);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        checkAuth();
    }, []);

    const checkAuth = async () => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const response = await authAPI.getMe();
                // Backend returns ApiResponse with data field
                setUser(response.data || response);
            } catch (error) {
                localStorage.removeItem('token');
                setUser(null);
            }
        }
        setLoading(false);
    };

    const login = async (email, password) => {
        const response = await authAPI.login({ email, password });
        // Backend returns ApiResponse: { success, message, data: { token, role, expiresAt } }
        const token = response.data?.token || response.token;
        localStorage.setItem('token', token);

        const userResponse = await authAPI.getMe();
        const userData = userResponse.data || userResponse;
        setUser(userData);
        return userData;
    };

    const register = async (userData) => {
        await authAPI.register(userData);
    };

    const logout = async () => {
        try {
            await authAPI.logout();
        } catch (error) {
            // Ignore logout errors
        }
        localStorage.removeItem('token');
        setUser(null);
    };

    const isAdmin = user?.role === 'ADMIN';
    const isAuthenticated = !!user;

    return (
        <AuthContext.Provider
            value={{
                user,
                loading,
                login,
                register,
                logout,
                isAdmin,
                isAuthenticated,
                checkAuth,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};
