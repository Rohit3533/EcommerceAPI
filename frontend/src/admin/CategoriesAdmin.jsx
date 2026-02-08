import { useState, useEffect } from 'react';
import { adminAPI } from '../api/api';
import './AdminPages.css';

const CategoriesAdmin = () => {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editingCategory, setEditingCategory] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
    });

    useEffect(() => {
        fetchCategories();
    }, []);

    const fetchCategories = async () => {
        try {
            const data = await adminAPI.getCategories();
            setCategories(data || []);
        } catch (error) {
            console.error('Error fetching categories:', error);
        } finally {
            setLoading(false);
        }
    };

    const openModal = (category = null) => {
        if (category) {
            setEditingCategory(category);
            setFormData({
                name: category.name,
                description: category.description || '',
            });
        } else {
            setEditingCategory(null);
            setFormData({ name: '', description: '' });
        }
        setShowModal(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingCategory) {
                await adminAPI.updateCategory(editingCategory.id, formData);
            } else {
                await adminAPI.createCategory(formData);
            }
            setShowModal(false);
            fetchCategories();
        } catch (error) {
            alert(error.message || 'Failed to save category');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to deactivate this category?')) return;
        try {
            await adminAPI.deleteCategory(id);
            fetchCategories();
        } catch (error) {
            alert(error.message || 'Failed to delete category');
        }
    };

    const handleReactivate = async (id) => {
        try {
            await adminAPI.reactivateCategory(id);
            fetchCategories();
        } catch (error) {
            alert(error.message || 'Failed to reactivate category');
        }
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
                    <h1 className="page-title">Categories</h1>
                    <p className="page-subtitle">Manage product categories</p>
                </div>
                <button className="btn btn-primary" onClick={() => openModal()}>
                    + Add Category
                </button>
            </div>

            <div className="table-container card">
                <table className="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Description</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {categories.map((category) => (
                            <tr key={category.id}>
                                <td>{category.id}</td>
                                <td className="category-name">{category.name}</td>
                                <td className="description">{category.description || '-'}</td>
                                <td>
                                    <span className={`badge ${category.status === 'ACTIVE' ? 'badge-success' : 'badge-danger'}`}>
                                        {category.status}
                                    </span>
                                </td>
                                <td>
                                    <div className="action-buttons">
                                        <button className="btn btn-secondary btn-sm" onClick={() => openModal(category)}>
                                            ✏️
                                        </button>
                                        {category.status === 'ACTIVE' ? (
                                            <button className="btn btn-danger btn-sm" onClick={() => handleDelete(category.id)}>
                                                🗑️
                                            </button>
                                        ) : (
                                            <button className="btn btn-success btn-sm" onClick={() => handleReactivate(category.id)}>
                                                ♻️
                                            </button>
                                        )}
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {categories.length === 0 && <p className="no-data">No categories yet</p>}
            </div>

            {/* Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2 className="modal-title">
                                {editingCategory ? 'Edit Category' : 'Add Category'}
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
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn btn-primary">
                                    {editingCategory ? 'Update' : 'Create'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CategoriesAdmin;
