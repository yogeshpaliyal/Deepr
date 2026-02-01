import { useState, useMemo } from 'react';
import { Header } from './components/Header';
import { AddLinkForm } from './components/AddLinkForm';
import { FilterBar } from './components/FilterBar';
import { LinkList } from './components/LinkList';
import { useLinks } from './hooks/useLinks';
import { useTags } from './hooks/useTags';
import { Link } from 'lucide-react';

function App() {
  const { links, isLoading, addLink, incrementOpenCount, refetch: refetchLinks } = useLinks();
  const { tags, refetch: refetchTags } = useTags();

  const [searchTerm, setSearchTerm] = useState('');
  const [selectedTag, setSelectedTag] = useState('');
  const [sortBy, setSortBy] = useState<'createdAt' | 'name' | 'openedCount'>('createdAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');

  const filteredLinks = useMemo(() => {
    let result = links;

    // Search
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      result = result.filter(link =>
        link.name.toLowerCase().includes(term) ||
        link.link.toLowerCase().includes(term)
      );
    }

    // Tag Filter
    if (selectedTag) {
      result = result.filter(link => link.tags.includes(selectedTag));
    }

    // Sort
    result = [...result].sort((a, b) => {
      let comparison = 0;
      if (sortBy === 'name') {
        comparison = a.name.localeCompare(b.name);
      } else if (sortBy === 'openedCount') {
        comparison = a.openedCount - b.openedCount;
      } else {
        // createdAt
        comparison = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      }
      return sortOrder === 'asc' ? comparison : -comparison;
    });

    return result;
  }, [links, searchTerm, selectedTag, sortBy, sortOrder]);

  const handleAddLink = async (data: any) => {
    const success = await addLink(data);
    if (success) {
      refetchTags(); // Refresh tags list
    }
    return success;
  };

  const handleClearFilters = () => {
    setSearchTerm('');
    setSelectedTag('');
    setSortBy('createdAt');
    setSortOrder('desc');
  };

  const activeFiltersCount = (searchTerm ? 1 : 0) + (selectedTag ? 1 : 0);
  const isFiltered = activeFiltersCount > 0;

  return (
    <div className="max-w-7xl mx-auto">
      <Header />
      <AddLinkForm onAddLink={handleAddLink} />

      <section className="glass-effect rounded-2xl shadow-xl animate-slide-up" style={{ animationDelay: '0.4s' }}>
        <div className="p-6 sm:p-8">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
            <h2 className="text-2xl font-semibold text-gray-800 flex items-center">
              <Link className="w-6 h-6 mr-2 text-primary-500" />
              Your Links
              <span className="ml-2 bg-gray-100 text-gray-600 px-2 py-1 rounded-full text-sm font-normal">
                {filteredLinks.length}
              </span>
            </h2>
            <button
              onClick={() => { refetchLinks(); refetchTags(); }}
              className="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-lg font-medium transition-all duration-200 transform hover:scale-105 flex items-center gap-2"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
              </svg>
              Refresh
            </button>
          </div>

          <FilterBar
            searchTerm={searchTerm}
            onSearchChange={setSearchTerm}
            selectedTag={selectedTag}
            onTagChange={setSelectedTag}
            availableTags={tags}
            sortBy={sortBy}
            onSortChange={setSortBy}
            sortOrder={sortOrder}
            onSortOrderChange={() => setSortOrder(prev => prev === 'asc' ? 'desc' : 'asc')}
            onClearFilters={handleClearFilters}
            totalLinks={links.length}
            filteredCount={filteredLinks.length}
          />

          <LinkList
            links={filteredLinks}
            isLoading={isLoading}
            onTagClick={setSelectedTag}
            onOpenLink={incrementOpenCount}
            isFiltered={isFiltered}
          />
        </div>
      </section>
    </div>
  );
}

export default App;
