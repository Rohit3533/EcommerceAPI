import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { productsAPI, categoriesAPI } from '../api/api';
import ProductCard from '../components/ProductCard';
import './Products.css';

const Products = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const selectedCategory = searchParams.get('category') || '';

    useEffect(() => {
        fetchCategories();
    }, []);

    useEffect(() => {
        fetchProducts();
    }, [page, selectedCategory]);

    const fetchCategories = async () => {
        try {
            const data = await categoriesAPI.getAll();
            setCategories(data || []);
        } catch (error) {
            console.error('Error fetching categories:', error);
        }
    };

    const fetchProducts = async () => {
        try {
            setLoading(true);
            let data;
            if (selectedCategory) {
                data = await productsAPI.getByCategory(selectedCategory, page, 12);
            } else {
                data = await productsAPI.getAll(page, 12);
            }
            setProducts(data.content || data || []);
            setTotalPages(data.totalPages || 1);
        } catch (error) {
            console.error('Error fetching products:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleCategoryChange = (categoryId) => {
        setPage(0);
        if (categoryId) {
            setSearchParams({ category: categoryId });
        } else {
            setSearchParams({});
        }
    };

    return (
        <div className="products-page page">
            <div className="container">
                <div className="products-layout">
                    {/* Sidebar Filters */}
                    <aside className="filters-sidebar">
                        <div className="filter-section">
                            <h3 className="filter-title">Categories</h3>
                            <div className="filter-options">
                                <label className="filter-option">
                                    <input
                                        type="radio"
                                        name="category"
                                        checked={!selectedCategory}
                                        onChange={() => handleCategoryChange('')}
                                    />
                                    <span className="radio-custom"></span>
                                    <span>All Products</span>
                                </label>
                                {categories.map((category) => (
                                    <label key={category.id} className="filter-option">
                                        <input
                                            type="radio"
                                            name="category"
                                            checked={selectedCategory === String(category.id)}
                                            onChange={() => handleCategoryChange(category.id)}
                                        />
                                        <span className="radio-custom"></span>
                                        <span>{category.name}</span>
                                    </label>
                                ))}
                            </div>
                        </div>
                    </aside>

                    {/* Products Grid */}
                    <main className="products-main">
                        <div className="products-header">
                            <h1 className="page-title">
                                {selectedCategory
                                    ? categories.find((c) => String(c.id) === selectedCategory)?.name || 'Products'
                                    : 'All Products'}
                            </h1>
                            <p className="products-count">
                                {products.length} product{products.length !== 1 ? 's' : ''} found
                            </p>
                        </div>

                        {loading ? (
                            <div className="loading-container">
                                <div className="spinner"></div>
                            </div>
                        ) : products.length > 0 ? (
                            <>
                                <div className="products-grid grid grid-4">
                                    {products.map((product) => (
                                        <ProductCard key={product.id} product={product} />
                                    ))}
                                </div>

                                {/* Pagination */}
                                {totalPages > 1 && (
                                    <div className="pagination">
                                        <button
                                            className="pagination-btn"
                                            onClick={() => setPage((p) => Math.max(0, p - 1))}
                                            disabled={page === 0}
                                        >
                                            ←
                                        </button>
                                        {[...Array(Math.min(5, totalPages))].map((_, i) => {
                                            const pageNum = page < 3 ? i : page - 2 + i;
                                            if (pageNum >= totalPages) return null;
                                            return (
                                                <button
                                                    key={pageNum}
                                                    className={`pagination-btn ${page === pageNum ? 'active' : ''}`}
                                                    onClick={() => setPage(pageNum)}
                                                >
                                                    {pageNum + 1}
                                                </button>
                                            );
                                        })}
                                        <button
                                            className="pagination-btn"
                                            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                                            disabled={page >= totalPages - 1}
                                        >
                                            →
                                        </button>
                                    </div>
                                )}
                            </>
                        ) : (
                            <div className="empty-state">
                                <p className="empty-state-icon">📦</p>
                                <p className="empty-state-title">No products found</p>
                                <p className="empty-state-text">Try selecting a different category</p>
                            </div>
                        )}
                    </main>
                </div>
            </div>
        </div>
    );
};

export default Products;
