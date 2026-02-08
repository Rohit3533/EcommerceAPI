import { useState, useEffect } from 'react';
import { adminAPI } from '../api/api';
import './AdminPages.css';

const ProductsAdmin = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        price: '',
        stock: '',
        imageUrl: '',
        categoryId: '',
        allowedPaymentMethods: ['CARD'], // Default to card
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [productsRes, categoriesRes] = await Promise.all([
                adminAPI.getProducts(0, 100),
                adminAPI.getCategories(),
            ]);
            console.log('Categories response:', categoriesRes);
            setProducts(productsRes.content || productsRes || []);
            // Handle both array and paginated response
            const cats = Array.isArray(categoriesRes) ? categoriesRes : (categoriesRes.content || []);
            console.log('Categories set to:', cats);
            setCategories(cats);
        } catch (error) {
            console.error('Error fetching data:', error);
        } finally {
            setLoading(false);
        }
    };

    const openModal = (product = null) => {
        if (product) {
            setEditingProduct(product);
            setFormData({
                name: product.name,
                description: product.description || '',
                price: product.price,
                stock: product.stock,
                imageUrl: product.imageUrl || '',
                categoryId: product.categoryId || '',
                allowedPaymentMethods: product.allowedPaymentMethods
                    ? product.allowedPaymentMethods.split(',')
                    : ['CARD'],
            });
        } else {
            setEditingProduct(null);
            setFormData({
                name: '',
                description: '',
                price: '',
                stock: '',
                imageUrl: '',
                categoryId: categories[0]?.id || '',
                allowedPaymentMethods: ['CARD'],
            });
        }
        setShowModal(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const data = {
                ...formData,
                price: parseFloat(formData.price),
                stock: parseInt(formData.stock),
                categoryId: parseInt(formData.categoryId),
                allowedPaymentMethods: formData.allowedPaymentMethods.join(','),
            };

            if (editingProduct) {
                await adminAPI.updateProduct(editingProduct.id, data);
            } else {
                await adminAPI.createProduct(data);
            }

            setShowModal(false);
            fetchData();
        } catch (error) {
            alert(error.message || 'Failed to save product');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this product?')) return;
        try {
            await adminAPI.deleteProduct(id);
            fetchData();
        } catch (error) {
            alert(error.message || 'Failed to delete product');
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

    return (
        <div className="admin-page">
            <div className="page-header">
                <div>
                    <h1 className="page-title">Products</h1>
                    <p className="page-subtitle">Manage your product catalog</p>
                </div>
                <button className="btn btn-primary" onClick={() => openModal()}>
                    + Add Product
                </button>
            </div>

            <div className="table-container card">
                <table className="table">
                    <thead>
                        <tr>
                            <th>Product</th>
                            <th>Category</th>
                            <th>Price</th>
                            <th>Stock</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {products.map((product) => (
                            <tr key={product.id}>
                                <td>
                                    <div className="product-cell">
                                        <img
                                            src={product.imageUrl || 'https://via.placeholder.com/40x40'}
                                            alt={product.name}
                                            className="product-thumb"
                                        />
                                        <span className="product-name">{product.name}</span>
                                    </div>
                                </td>
                                <td>{product.categoryName || 'N/A'}</td>
                                <td className="price">{formatPrice(product.price)}</td>
                                <td>{product.stock}</td>
                                <td>
                                    <span className={`badge ${product.stock > 0 ? 'badge-success' : 'badge-danger'}`}>
                                        {product.stock > 0 ? 'Active' : 'Out of Stock'}
                                    </span>
                                </td>
                                <td>
                                    <div className="action-buttons">
                                        <button className="btn btn-secondary btn-sm" onClick={() => openModal(product)}>
                                            ✏️
                                        </button>
                                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(product.id)}>
                                            🗑️
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {products.length === 0 && <p className="no-data">No products yet</p>}
            </div>

            {/* Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2 className="modal-title">
                                {editingProduct ? 'Edit Product' : 'Add Product'}
                            </h2>
                            <button className="modal-close" onClick={() => setShowModal(false)}>
                                ✕
                            </button>
                        </div>
                        <form onSubmit={handleSubmit}>
                            <div className="modal-body">
                                <div className="form-group">
                                    <label className="form-label">Name</label>
                                    <input
                                        type="text"
                                        className="form-input"
                                        value={formData.name}
                                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Description</label>
                                    <textarea
                                        className="form-input"
                                        rows={3}
                                        value={formData.description}
                                        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                    />
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">Price</label>
                                        <input
                                            type="number"
                                            step="0.01"
                                            className="form-input"
                                            value={formData.price}
                                            onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                                            required
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Stock</label>
                                        <input
                                            type="number"
                                            className="form-input"
                                            value={formData.stock}
                                            onChange={(e) => setFormData({ ...formData, stock: e.target.value })}
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Category</label>
                                    <select
                                        className="form-select"
                                        value={formData.categoryId}
                                        onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                                        required
                                    >
                                        <option value="">Select category</option>
                                        {categories.map((cat) => (
                                            <option key={cat.id} value={cat.id}>
                                                {cat.name}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Image URL</label>
                                    <input
                                        type="url"
                                        className="form-input"
                                        value={formData.imageUrl}
                                        onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                                        placeholder="https://..."
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Allowed Payment Methods</label>
                                    <div className="payment-checkboxes">
                                        {['COD', 'UPI', 'CARD'].map((method) => (
                                            <label key={method} className="checkbox-item">
                                                <input
                                                    type="checkbox"
                                                    checked={formData.allowedPaymentMethods.includes(method)}
                                                    onChange={(e) => {
                                                        if (e.target.checked) {
                                                            setFormData({
                                                                ...formData,
                                                                allowedPaymentMethods: [...formData.allowedPaymentMethods, method]
                                                            });
                                                        } else {
                                                            setFormData({
                                                                ...formData,
                                                                allowedPaymentMethods: formData.allowedPaymentMethods.filter(m => m !== method)
                                                            });
                                                        }
                                                    }}
                                                />
                                                <span>{method === 'COD' ? '💵 Cash on Delivery' : method === 'UPI' ? '📱 UPI' : '💳 Card'}</span>
                                            </label>
                                        ))}
                                    </div>
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn btn-primary">
                                    {editingProduct ? 'Update' : 'Create'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProductsAdmin;
