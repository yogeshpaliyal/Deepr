export interface Link {
    id?: number;
    link: string;
    name: string;
    notes?: string;
    tags: string[];
    openedCount: number;
    createdAt: string;
}

export interface Tag {
    id: number;
    name: string;
    count: number;
}
