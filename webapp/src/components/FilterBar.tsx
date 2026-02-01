import React from 'react';
import { Search, RotateCcw, X, ArrowUp, ArrowDown } from 'lucide-react';
import type { Tag } from '../types';

interface FilterBarProps {
    searchTerm: string;
    onSearchChange: (value: string) => void;
    selectedTag: string;
    onTagChange: (value: string) => void;
    availableTags: Tag[];
    sortBy: 'createdAt' | 'name' | 'openedCount';
    onSortChange: (value: 'createdAt' | 'name' | 'openedCount') => void;
    sortOrder: 'asc' | 'desc';
    onSortOrderChange: () => void;
    onClearFilters: () => void;
    totalLinks: number;
    filteredCount: number;
}

export const FilterBar: React.FC<FilterBarProps> = ({
    searchTerm,
    onSearchChange,
    selectedTag,
    onTagChange,
    availableTags,
    sortBy,
    onSortChange,
    sortOrder,
    onSortOrderChange,
    onClearFilters,
    totalLinks,
    filteredCount
}) => {
    return (
        <div className="mb-6 space-y-4">
            {/* Search Input */}
            <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Search className="w-5 h-5 text-gray-400" />
                </div>
                <input
                    type="text"
                    placeholder="Search links by name or URL..."
                    className="w-full pl-10 pr-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-800 outline-none"
                    value={searchTerm}
                    onChange={(e) => onSearchChange(e.target.value)}
                />
                {searchTerm && (
                    <button
                        onClick={() => onSearchChange('')}
                        className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                    >
                        <X className="w-5 h-5" />
                    </button>
                )}
            </div>

            {/* Filter Controls */}
            <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
                {/* Tag Filter */}
                <div className="flex-1 w-full sm:w-auto">
                    <select
                        value={selectedTag}
                        onChange={(e) => onTagChange(e.target.value)}
                        className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-800 bg-white outline-none"
                    >
                        <option value="">All Tags</option>
                        {availableTags.map(tag => (
                            <option key={tag.id} value={tag.name}>
                                {tag.name} ({tag.count})
                            </option>
                        ))}
                    </select>
                </div>

                {/* Sort Controls */}
                <div className="flex gap-2">
                    <select
                        value={sortBy}
                        onChange={(e) => onSortChange(e.target.value as any)}
                        className="px-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-800 bg-white outline-none"
                    >
                        <option value="createdAt">Date Added</option>
                        <option value="name">Name</option>
                        <option value="openedCount">Opens</option>
                    </select>
                    <button
                        onClick={onSortOrderChange}
                        className="px-4 py-3 bg-gray-100 hover:bg-gray-200 rounded-xl transition-all duration-200 flex items-center gap-2"
                        title={sortOrder === 'asc' ? 'Ascending' : 'Descending'}
                    >
                        {sortOrder === 'asc' ? <ArrowUp size={16} /> : <ArrowDown size={16} />}
                    </button>
                </div>

                {/* Clear Filters */}
                <button
                    onClick={onClearFilters}
                    className="px-4 py-3 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-xl transition-all duration-200 flex items-center gap-2"
                >
                    <RotateCcw size={16} />
                    Clear
                </button>
            </div>

            <div className="text-sm text-gray-500">
                Showing {filteredCount} of {totalLinks} links
            </div>
        </div>
    );
};
