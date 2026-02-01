import React from 'react';
import type { Link } from '../types';
import { LinkItem } from './LinkItem';
import { Loader2, SearchX } from 'lucide-react';

interface LinkListProps {
    links: Link[];
    isLoading: boolean;
    onTagClick: (tag: string) => void;
    onOpenLink: (id: number) => void;
    isFiltered: boolean;
}

export const LinkList: React.FC<LinkListProps> = ({ links, isLoading, onTagClick, onOpenLink, isFiltered }) => {
    if (isLoading) {
        return (
            <div className="flex flex-col items-center justify-center py-16">
                <Loader2 className="w-12 h-12 text-primary-500 animate-spin mb-4" />
                <h3 className="text-xl font-semibold text-gray-700 mb-2">Loading links...</h3>
                <p className="text-gray-500">Please wait while we fetch your saved links.</p>
            </div>
        );
    }

    if (links.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center py-16 animate-fade-in">
                <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                    {isFiltered ? <SearchX className="w-12 h-12 text-gray-400" /> : <span className="text-4xl">🔗</span>}
                </div>
                <h3 className="text-xl font-semibold text-gray-700 mb-2">
                    {isFiltered ? 'No matching links found!' : 'No links yet!'}
                </h3>
                <p className="text-gray-500 text-center">
                    {isFiltered ? 'Try adjusting your search terms or filters.' : 'Add your first link using the form above to get started.'}
                </p>
            </div>
        );
    }

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6 animate-slide-up" style={{ animationDelay: '0.4s' }}>
            {links.map((link) => (
                <LinkItem
                    key={link.id}
                    link={link}
                    onTagClick={onTagClick}
                    onOpenLink={onOpenLink}
                />
            ))}
        </div>
    );
};
