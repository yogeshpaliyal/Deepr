import React from 'react';
import { Link as LinkIcon } from 'lucide-react';

export const Header: React.FC = () => {
    return (
        <header className="glass-effect rounded-2xl shadow-2xl mb-8 animate-fade-in">
            <div className="p-8 text-center">
                <div className="flex items-center justify-center mb-4">
                    <div className="w-16 h-16 bg-gradient-to-r from-primary-500 to-secondary-500 rounded-full flex items-center justify-center text-white shadow-lg">
                        <LinkIcon size={32} />
                    </div>
                </div>
                <h1 className="text-4xl sm:text-5xl font-bold bg-gradient-to-r from-primary-600 to-secondary-600 bg-clip-text text-transparent mb-2">
                    Deepr
                </h1>
                <p className="text-gray-600 text-lg">Your Personal Link Manager</p>
            </div>
        </header>
    );
};
