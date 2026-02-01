import React from 'react';
import { ExternalLink, Clock, Tag as TagIcon, Eye } from 'lucide-react';
import type { Link } from '../types';

interface LinkItemProps {
    link: Link;
    onTagClick: (tag: string) => void;
    onOpenLink: (id: number) => void;
}

export const LinkItem: React.FC<LinkItemProps> = ({ link, onTagClick, onOpenLink }) => {
    const handleOpen = () => {
        onOpenLink(link.id!);
        window.open(link.link, '_blank');
    };

    return (
        <div className="bg-white rounded-xl shadow-md hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1 border border-gray-100 animate-fade-in group flex flex-col h-full">
            <div className="p-6 flex-1 flex flex-col">
                <div className="flex items-start justify-between mb-3">
                    <h3 className="text-lg font-semibold text-gray-800 line-clamp-2 flex-1 group-hover:text-primary-600 transition-colors">
                        {link.name}
                    </h3>
                    <div className="ml-2 bg-primary-50 text-primary-700 px-2 py-1 rounded-full text-xs font-medium flex items-center gap-1">
                        <Eye size={12} />
                        {link.openedCount}
                    </div>
                </div>

                <a
                    href={link.link}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-primary-600 hover:text-primary-800 text-sm mb-4 block break-all transition-colors duration-200 line-clamp-2"
                    onClick={(e) => {
                        e.preventDefault();
                        handleOpen();
                    }}
                >
                    {link.link}
                </a>

                {link.notes && (
                    <div className="flex items-start gap-2 mb-4 p-3 bg-gray-50 rounded-lg">
                        <span className="text-gray-500 mt-0.5 flex-shrink-0">📝</span>
                        <span className="text-xs text-gray-600 line-clamp-3">{link.notes}</span>
                    </div>
                )}

                <div className="mt-auto">
                    <div className="flex items-center justify-between text-xs text-gray-500 mb-4">
                        <span className="flex items-center">
                            <Clock size={12} className="mr-1" />
                            {new Date(link.createdAt).toLocaleDateString()}
                        </span>
                    </div>

                    {link.tags.length > 0 && (
                        <div className="flex flex-wrap gap-1 mb-4">
                            {link.tags.map(tag => (
                                <button
                                    key={tag}
                                    className="bg-gray-100 hover:bg-primary-100 hover:text-primary-700 text-gray-700 px-2 py-1 rounded-full text-xs font-medium transition-colors duration-200 cursor-pointer flex items-center gap-1"
                                    onClick={() => onTagClick(tag)}
                                >
                                    <TagIcon size={10} />
                                    {tag}
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            <div className="p-4 pt-0 mt-auto border-t border-gray-100">
                <button
                    onClick={handleOpen}
                    className="w-full mt-4 bg-gradient-to-r from-primary-500 to-secondary-500 hover:from-primary-600 hover:to-secondary-600 text-white py-2 px-4 rounded-lg text-sm font-medium transition-all duration-200 transform hover:scale-[1.02] flex items-center justify-center gap-2"
                >
                    <ExternalLink size={16} />
                    Open Link
                </button>
            </div>
        </div>
    );
};
