import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './ProductCard.css';

const ProductCard = ({ product }) => {
    const { isAuthenticated } = useAuth();
    const { addToCart } = useCart();

    const handleAddToCart = async (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (!isAuthenticated) {
            window.location.href = '/login';
            return;
        }
        try {
            await addToCart(product.id, 1);
        } catch (error) {
            console.error('Error adding to cart:', error);
        }
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        }).format(price);
    };

    const isInStock = product.stock > 0;

    return (
        <Link to={`/products/${product.id}`} className="product-card">
            <div className="product-image-container">
                <img
                    src={product.imageUrl || 'https://via.placeholder.com/300x300?text=No+Image'}
                    alt={product.name}
                    className="product-image"
                    onError={(e) => {
                        e.target.src = 'https://via.placeholder.com/300x300?text=No+Image';
                    }}
                />
                {!isInStock && <div className="out-of-stock-overlay">Out of Stock</div>}
                <div className="product-actions">
                    <button
                        className="add-to-cart-btn"
                        onClick={handleAddToCart}
                        disabled={!isInStock}
                    >
                        {isInStock ? '🛒 Add to Cart' : 'Out of Stock'}
                    </button>
                </div>
            </div>

            <div className="product-info">
                <p className="product-category">{product.categoryName || 'Uncategorized'}</p>
                <h3 className="product-name">{product.name}</h3>
                <div className="product-footer">
                    <span className="product-price">{formatPrice(product.price)}</span>
                    {isInStock && (
                        <span className="stock-badge">
                            <span className="stock-dot in-stock"></span>
                            {product.stock} left
                        </span>
                    )}
                </div>
            </div>
        </Link>
    );
};

export default ProductCard;
