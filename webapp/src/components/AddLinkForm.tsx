import React, { useState, useRef, useEffect } from 'react';
import { Plus, X } from 'lucide-react';
import { Button } from './ui/Button';
import { Input } from './ui/Input';
import { useTags } from '../hooks/useTags';


interface AddLinkFormProps {
    onAddLink: (data: { link: string; name: string; notes?: string; tags: { name: string }[] }) => Promise<boolean>;
}

export const AddLinkForm: React.FC<AddLinkFormProps> = ({ onAddLink }) => {
    const [link, setLink] = useState('');
    const [name, setName] = useState('');
    const [notes, setNotes] = useState('');
    const [tagInput, setTagInput] = useState('');
    const [selectedTags, setSelectedTags] = useState<{ name: string }[]>([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const { tags: availableTags } = useTags();
    const suggestionsRef = useRef<HTMLDivElement>(null);

    const filteredTags = availableTags
        .filter(t => t.name.toLowerCase().includes(tagInput.toLowerCase()) && !selectedTags.some(st => st.name === t.name))
        .slice(0, 5);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (suggestionsRef.current && !suggestionsRef.current.contains(event.target as Node)) {
                setShowSuggestions(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        const success = await onAddLink({ link, name, notes, tags: selectedTags });
        if (success) {
            setLink('');
            setName('');
            setNotes('');
            setTagInput('');
            setSelectedTags([]);
        }
        setIsSubmitting(false);
    };

    const addTag = (tagName: string) => {
        if (!selectedTags.some(t => t.name === tagName)) {
            setSelectedTags([...selectedTags, { name: tagName }]);
        }
        setTagInput('');
        setShowSuggestions(false);
    };

    const removeTag = (tagName: string) => {
        setSelectedTags(selectedTags.filter(t => t.name !== tagName));
    };

    return (
        <section className="glass-effect rounded-2xl shadow-xl mb-8 animate-slide-up" style={{ animationDelay: '0.2s' }}>
            <div className="p-6 sm:p-8">
                <h2 className="text-2xl font-semibold text-gray-800 mb-6 flex items-center">
                    <Plus className="w-6 h-6 mr-2 text-primary-500" />
                    Add New Link
                </h2>

                <form onSubmit={handleSubmit} className="space-y-4 sm:space-y-0 sm:flex sm:gap-4 sm:items-start">
                    <div className="flex-1 space-y-4 sm:space-y-0 sm:flex sm:gap-4">
                        <div className="flex-1">
                            <Input
                                id="linkUrl"
                                label="Link URL"
                                type="url"
                                placeholder="https://example.com"
                                required
                                value={link}
                                onChange={e => setLink(e.target.value)}
                            />
                        </div>
                        <div className="flex-1">
                            <Input
                                id="linkName"
                                label="Link Name"
                                type="text"
                                placeholder="My Awesome Link"
                                required
                                value={name}
                                onChange={e => setName(e.target.value)}
                            />
                        </div>
                    </div>
                </form>
                <div className="flex-1 space-y-4 sm:space-y-0 sm:flex sm:gap-4 sm:items-start mt-4">
                    <div className="flex-1">
                        <Input
                            id="linkNotes"
                            label="Notes (Optional)"
                            type="text"
                            placeholder="Add notes..."
                            value={notes}
                            onChange={e => setNotes(e.target.value)}
                        />
                    </div>
                    <div className="flex-1 relative" ref={suggestionsRef}>
                        <Input
                            id="linkTags"
                            label="Tags (Optional)"
                            type="text"
                            placeholder="Type to add tags..."
                            value={tagInput}
                            onChange={e => {
                                setTagInput(e.target.value);
                                setShowSuggestions(true);
                            }}
                            onFocus={() => setShowSuggestions(true)}
                            onKeyDown={e => {
                                if (e.key === 'Enter' && tagInput.trim()) {
                                    e.preventDefault();
                                    addTag(tagInput.trim());
                                }
                            }}
                        />
                        {showSuggestions && tagInput && (
                            <div className="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg max-h-40 overflow-y-auto">
                                {filteredTags.map(tag => (
                                    <div
                                        key={tag.id}
                                        className="px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 cursor-pointer border-b flex justify-between items-center"
                                        onClick={() => addTag(tag.name)}
                                    >
                                        <span>{tag.name}</span>
                                        <span className="text-xs text-gray-500">{tag.count} links</span>
                                    </div>
                                ))}
                                {!filteredTags.some(t => t.name.toLowerCase() === tagInput.toLowerCase()) && (
                                    <div
                                        className="px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 cursor-pointer"
                                        onClick={() => addTag(tagInput)}
                                    >
                                        <Plus className="w-4 h-4 inline mr-2 text-green-500" />
                                        Create new tag: <strong>{tagInput}</strong>
                                    </div>
                                )}
                            </div>
                        )}
                        <div className="flex flex-wrap gap-2 mt-2">
                            {selectedTags.map(tag => (
                                <span key={tag.name} className="inline-flex items-center gap-1 bg-primary-100 text-primary-800 px-3 py-1 rounded-full text-sm font-medium">
                                    {tag.name}
                                    <button
                                        type="button"
                                        onClick={() => removeTag(tag.name)}
                                        className="ml-1 text-primary-600 hover:text-primary-800"
                                    >
                                        <X size={12} />
                                    </button>
                                </span>
                            ))}
                        </div>
                    </div>
                </div>
                <div className="mt-6 sm:mt-0 sm:ml-4 sm:self-end pt-2">
                    <Button
                        type="submit"
                        className="w-full sm:w-auto"
                        isLoading={isSubmitting}
                        onClick={handleSubmit} // Ensure onClick submits if button is outside form, but form wraps inputs. Adjusted layout below.
                    >
                        Add Link
                    </Button>
                </div>
            </div>
        </section>
    );
};
