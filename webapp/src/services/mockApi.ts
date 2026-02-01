import type { Link, Tag } from '../types';

const LINKS_KEY = 'deepr_links';

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// Initial mock data
const INITIAL_LINKS: Link[] = [
    { id: 1, link: 'https://example.com', name: 'Example Link', notes: 'This is an example', tags: ['example', 'test'], openedCount: 5, createdAt: new Date().toISOString() },
    { id: 2, link: 'https://react.dev', name: 'React Documentation', notes: 'Best place to learn React', tags: ['react', 'dev'], openedCount: 12, createdAt: new Date().toISOString() },
];

const getLinksFromStorage = (): Link[] => {
    const stored = localStorage.getItem(LINKS_KEY);
    if (!stored) {
        localStorage.setItem(LINKS_KEY, JSON.stringify(INITIAL_LINKS));
        return INITIAL_LINKS;
    }
    return JSON.parse(stored);
};

export const api = {
    getLinks: async (): Promise<Link[]> => {
        await delay(500); // Simulate network delay
        return getLinksFromStorage();
    },

    addLink: async (link: Omit<Link, 'id' | 'openedCount' | 'createdAt'>): Promise<Link> => {
        await delay(500);
        const links = getLinksFromStorage();
        const newLink: Link = {
            ...link,
            id: Date.now(),
            openedCount: 0,
            createdAt: new Date().toISOString(),
        };
        links.push(newLink);
        localStorage.setItem(LINKS_KEY, JSON.stringify(links));
        return newLink;
    },

    incrementOpenCount: async (id: number): Promise<void> => {
        // Optimistic update in local storage
        const links = getLinksFromStorage();
        const index = links.findIndex(l => l.id === id);
        if (index !== -1) {
            links[index].openedCount += 1;
            localStorage.setItem(LINKS_KEY, JSON.stringify(links));
        }
    },

    getTags: async (): Promise<Tag[]> => {
        await delay(300);
        const links = getLinksFromStorage();
        const tagCounts: Record<string, number> = {};

        links.forEach(link => {
            link.tags.forEach(tag => {
                tagCounts[tag] = (tagCounts[tag] || 0) + 1;
            });
        });

        return Object.entries(tagCounts).map(([name, count], index) => ({
            id: index,
            name,
            count
        }));
    }
};
