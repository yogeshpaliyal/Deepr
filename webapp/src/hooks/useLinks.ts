import { useState, useCallback, useEffect } from 'react';
import type { Link } from '../types';
import { api } from '../services/mockApi';
import { useToast } from './useToast';

export function useLinks() {
    const [links, setLinks] = useState<Link[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const { showToast } = useToast();

    const fetchLinks = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await api.getLinks();
            setLinks(data);
        } catch (err) {
            console.error(err);
            setError('Failed to load links');
            showToast('Failed to load links', 'error');
        } finally {
            setIsLoading(false);
        }
    }, [showToast]);

    const addLink = useCallback(async (linkData: { link: string; name: string; notes?: string; tags: { name: string }[] }) => {
        try {
            const tags = linkData.tags.map(t => t.name);
            const newLink = await api.addLink({
                ...linkData,
                tags
            });
            setLinks(prev => [...prev, newLink]);
            showToast('Link added successfully!', 'success');
            return true;
        } catch (err) {
            console.error(err);
            showToast('Failed to add link', 'error');
            return false;
        }
    }, [showToast]);

    const incrementOpenCount = useCallback(async (id: number) => {
        try {
            await api.incrementOpenCount(id);
            setLinks(prev => prev.map(link =>
                link.id === id ? { ...link, openedCount: link.openedCount + 1 } : link
            ));
        } catch (err) {
            console.error(err);
        }
    }, []);

    useEffect(() => {
        fetchLinks();
    }, [fetchLinks]);

    return {
        links,
        isLoading,
        error,
        refetch: fetchLinks,
        addLink,
        incrementOpenCount
    };
}
