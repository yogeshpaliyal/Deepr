import { useState, useCallback, useEffect } from 'react';
import type { Tag } from '../types';
import { api } from '../services/mockApi';
import { useToast } from './useToast';

export function useTags() {
    const [tags, setTags] = useState<Tag[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const { showToast } = useToast();

    const fetchTags = useCallback(async () => {
        try {
            const data = await api.getTags();
            setTags(data);
        } catch (err) {
            console.error(err);
            showToast('Failed to load tags', 'error');
        } finally {
            setIsLoading(false);
        }
    }, [showToast]);

    useEffect(() => {
        fetchTags();
    }, [fetchTags]);

    return {
        tags,
        isLoading,
        refetch: fetchTags
    };
}
