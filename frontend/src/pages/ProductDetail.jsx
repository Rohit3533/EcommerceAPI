import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { productsAPI } from '../api/api';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './ProductDetail.css';

const ProductDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { isAuthenticated } = useAuth();
    const { addToCart } = useCart();

    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [quantity, setQuantity] = useState(1);
    const [adding, setAdding] = useState(false);

    useEffect(() => {
        fetchProduct();
    }, [id]);

    const fetchProduct = async () => {
        try {
            setLoading(true);
            const data = await productsAPI.getById(id);
            setProduct(data);
        } catch (error) {
            console.error('Error fetching product:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAddToCart = async () => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }
        try {
            setAdding(true);
            await addToCart(product.id, quantity);
        } catch (error) {
            console.error('Error adding to cart:', error);
        } finally {
            setAdding(false);
        }
    };

    const handleBuyNow = async () => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }
        try {
            await addToCart(product.id, quantity);
            navigate('/cart');
        } catch (error) {
            console.error('Error:', error);
        }
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        }).format(price);
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="spinner"></div>
            </div>
        );
    }

    if (!product) {
        return (
            <div className="page">
                <div className="container">
                    <div className="empty-state">
                        <p className="empty-state-icon">❌</p>
                        <p className="empty-state-title">Product not found</p>
                        <Link to="/products" className="btn btn-primary">
                            Back to Products
                        </Link>
                    </div>
                </div>
            </div>
        );
    }

    const isInStock = product.stock > 0;

    return (
        <div className="product-detail-page page">
            <div className="container">
                {/* Breadcrumb */}
                <nav className="breadcrumb">
                    <Link to="/">Home</Link>
                    <span className="breadcrumb-separator">/</span>
                    <Link to="/products">Products</Link>
                    <span className="breadcrumb-separator">/</span>
                    <span className="breadcrumb-current">{product.name}</span>
                </nav>

                <div className="product-detail-grid">
                    {/* Product Image */}
                    <div className="product-image-section">
                        <div className="product-image-main">
                            <img
                                src={product.imageUrl || 'https://via.placeholder.com/600x600?text=No+Image'}
                                alt={product.name}
                                onError={(e) => {
                                    e.target.src = 'https://via.placeholder.com/600x600?text=No+Image';
                                }}
                            />
                            {!isInStock && (
                                <div className="out-of-stock-badge">Out of Stock</div>
                            )}
                        </div>
                    </div>

                    {/* Product Info */}
                    <div className="product-info-section">
                        <span className="product-category-tag">{product.categoryName || 'Uncategorized'}</span>
                        <h1 className="product-title">{product.name}</h1>

                        <div className="product-price-row">
                            <span className="product-price-large">{formatPrice(product.price)}</span>
                            {isInStock && (
                                <span className="stock-indicator in-stock">
                                    <span className="stock-dot"></span>
                                    {product.stock} in stock
                                </span>
                            )}
                        </div>

                        <p className="product-description">{product.description}</p>

                        {/* Quantity Selector */}
                        <div className="quantity-row">
                            <span className="quantity-label">Quantity:</span>
                            <div className="quantity-selector">
                                <button
                                    className="quantity-btn"
                                    onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                                    disabled={!isInStock}
                                >
                                    −
                                </button>
                                <span className="quantity-value">{quantity}</span>
                                <button
                                    className="quantity-btn"
                                    onClick={() => setQuantity((q) => Math.min(product.stock, q + 1))}
                                    disabled={!isInStock || quantity >= product.stock}
                                >
                                    +
                                </button>
                            </div>
                        </div>

                        {/* Action Buttons */}
                        <div className="product-actions-row">
                            <button
                                className="btn btn-primary btn-lg action-btn"
                                onClick={handleAddToCart}
                                disabled={!isInStock || adding}
                            >
                                {adding ? '⏳ Adding...' : '🛒 Add to Cart'}
                            </button>
                            <button
                                className="btn btn-secondary btn-lg action-btn"
                                onClick={handleBuyNow}
                                disabled={!isInStock}
                            >
                                ⚡ Buy Now
                            </button>
                        </div>

                        {/* Product Meta */}
                        <div className="product-meta">
                            <div className="meta-item">
                                <span className="meta-icon">🚚</span>
                                <span>Free shipping on orders over $50</span>
                            </div>
                            <div className="meta-item">
                                <span className="meta-icon">↩️</span>
                                <span>30-day return policy</span>
                            </div>
                            <div className="meta-item">
                                <span className="meta-icon">🔒</span>
                                <span>Secure checkout</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProductDetail;
