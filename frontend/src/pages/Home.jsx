import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { productsAPI, categoriesAPI } from '../api/api';
import ProductCard from '../components/ProductCard';
import './Home.css';

const Home = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [productsRes, categoriesRes] = await Promise.all([
                productsAPI.getAll(0, 8),
                categoriesAPI.getAll(),
            ]);
            setProducts(productsRes.content || productsRes || []);
            setCategories(categoriesRes || []);
        } catch (error) {
            console.error('Error fetching data:', error);
        } finally {
            setLoading(false);
        }
    };

    const categoryIcons = ['📱', '👕', '🏠', '📚', '🎮', '💄', '🍔', '⚽'];

    if (loading) {
        return (
            <div className="loading-container">
                <div className="spinner"></div>
            </div>
        );
    }

    return (
        <div className="home-page">
            {/* Hero Section */}
            <section className="hero">
                <div className="container">
                    <div className="hero-content">
                        <h1 className="hero-title">
                            Discover <span className="gradient-text">Amazing</span> Products
                        </h1>
                        <p className="hero-subtitle">
                            Shop the latest trends with unbeatable prices and fast delivery
                        </p>
                        <div className="hero-buttons">
                            <Link to="/products" className="btn btn-primary btn-lg">
                                Shop Now →
                            </Link>
                        </div>
                    </div>
                    <div className="hero-decoration">
                        <div className="decoration-circle circle-1"></div>
                        <div className="decoration-circle circle-2"></div>
                        <div className="decoration-circle circle-3"></div>
                    </div>
                </div>
            </section>

            {/* Categories Section */}
            {categories.length > 0 && (
                <section className="categories-section">
                    <div className="container">
                        <div className="section-header">
                            <h2 className="section-title">Shop by Category</h2>
                            <Link to="/products" className="section-link">View All →</Link>
                        </div>
                        <div className="categories-grid">
                            {categories.slice(0, 6).map((category, index) => (
                                <Link
                                    key={category.id}
                                    to={`/products?category=${category.id}`}
                                    className="category-card"
                                >
                                    <span className="category-icon">
                                        {categoryIcons[index % categoryIcons.length]}
                                    </span>
                                    <span className="category-name">{category.name}</span>
                                </Link>
                            ))}
                        </div>
                    </div>
                </section>
            )}

            {/* Featured Products Section */}
            <section className="products-section">
                <div className="container">
                    <div className="section-header">
                        <h2 className="section-title">Featured Products</h2>
                        <Link to="/products" className="section-link">View All →</Link>
                    </div>
                    <div className="products-grid grid grid-4">
                        {products.map((product) => (
                            <ProductCard key={product.id} product={product} />
                        ))}
                    </div>
                    {products.length === 0 && (
                        <div className="empty-state">
                            <p className="empty-state-icon">📦</p>
                            <p className="empty-state-title">No products yet</p>
                            <p className="empty-state-text">Check back soon for amazing products!</p>
                        </div>
                    )}
                </div>
            </section>

            {/* Features Section */}
            <section className="features-section">
                <div className="container">
                    <div className="features-grid">
                        <div className="feature-card">
                            <span className="feature-icon">🚚</span>
                            <h3 className="feature-title">Free Shipping</h3>
                            <p className="feature-text">On orders over $50</p>
                        </div>
                        <div className="feature-card">
                            <span className="feature-icon">🔒</span>
                            <h3 className="feature-title">Secure Payment</h3>
                            <p className="feature-text">100% secure checkout</p>
                        </div>
                        <div className="feature-card">
                            <span className="feature-icon">↩️</span>
                            <h3 className="feature-title">Easy Returns</h3>
                            <p className="feature-text">30-day return policy</p>
                        </div>
                        <div className="feature-card">
                            <span className="feature-icon">💬</span>
                            <h3 className="feature-title">24/7 Support</h3>
                            <p className="feature-text">We're here to help</p>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default Home;
