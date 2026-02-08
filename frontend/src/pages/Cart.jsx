import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import './Cart.css';

const Cart = () => {
    const navigate = useNavigate();
    const { cart, loading, itemCount, totalAmount, updateQuantity, removeFromCart, clearCart } = useCart();

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

    const items = cart?.items || [];

    if (items.length === 0) {
        return (
            <div className="page">
                <div className="container">
                    <div className="empty-state">
                        <p className="empty-state-icon">🛒</p>
                        <p className="empty-state-title">Your cart is empty</p>
                        <p className="empty-state-text">Add some products to get started!</p>
                        <Link to="/products" className="btn btn-primary">
                            Continue Shopping
                        </Link>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="cart-page page">
            <div className="container">
                <h1 className="page-title">Shopping Cart</h1>
                <p className="page-subtitle">{itemCount} item{itemCount !== 1 ? 's' : ''} in your cart</p>

                <div className="cart-layout">
                    {/* Cart Items */}
                    <div className="cart-items-section">
                        {items.map((item) => (
                            <div key={item.productId} className="cart-item card">
                                <img
                                    src={item.imageUrl || 'https://via.placeholder.com/120x120?text=No+Image'}
                                    alt={item.productName}
                                    className="cart-item-image"
                                    onError={(e) => {
                                        e.target.src = 'https://via.placeholder.com/120x120?text=No+Image';
                                    }}
                                />
                                <div className="cart-item-info">
                                    <h3 className="cart-item-name">{item.productName}</h3>
                                    <p className="cart-item-price">{formatPrice(item.price)}</p>
                                </div>
                                <div className="cart-item-quantity">
                                    <div className="quantity-selector">
                                        <button
                                            className="quantity-btn"
                                            onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                                        >
                                            −
                                        </button>
                                        <span className="quantity-value">{item.quantity}</span>
                                        <button
                                            className="quantity-btn"
                                            onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                                        >
                                            +
                                        </button>
                                    </div>
                                </div>
                                <div className="cart-item-total">
                                    <span className="item-total-label">Total</span>
                                    <span className="item-total-price">{formatPrice(item.price * item.quantity)}</span>
                                </div>
                                <button
                                    className="cart-item-remove"
                                    onClick={() => removeFromCart(item.productId)}
                                >
                                    🗑️
                                </button>
                            </div>
                        ))}

                        <button className="btn btn-secondary clear-cart-btn" onClick={clearCart}>
                            🗑️ Clear Cart
                        </button>
                    </div>

                    {/* Order Summary */}
                    <div className="order-summary card">
                        <h2 className="summary-title">Order Summary</h2>

                        <div className="summary-row">
                            <span>Subtotal ({itemCount} items)</span>
                            <span>{formatPrice(totalAmount)}</span>
                        </div>
                        <div className="summary-row">
                            <span>Shipping</span>
                            <span className="free-shipping">FREE</span>
                        </div>
                        <div className="summary-divider"></div>
                        <div className="summary-row total-row">
                            <span>Total</span>
                            <span className="total-amount">{formatPrice(totalAmount)}</span>
                        </div>

                        <button
                            className="btn btn-primary btn-lg checkout-btn"
                            onClick={() => navigate('/checkout')}
                        >
                            Proceed to Checkout →
                        </button>

                        <div className="secure-checkout">
                            <span>🔒</span>
                            <span>Secure checkout</span>
                        </div>
                    </div>
                </div>

                <Link to="/products" className="continue-shopping">
                    ← Continue Shopping
                </Link>
            </div>
        </div>
    );
};

export default Cart;
