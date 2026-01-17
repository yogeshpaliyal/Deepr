# Profiles Feature Implementation

## Overview
This implementation adds support for separate profiles in the Deepr app, allowing users to organize their links and tags into different contexts (e.g., Work, Personal, Music, etc.). Each profile maintains its own independent set of links and tags.

## Key Changes

### 1. Database Schema (SQLDelight)
- **New Table: `Profile`**
  - `id`: Primary key
  - `name`: Unique profile name
  - `createdAt`: Timestamp

- **Updated Table: `Deepr` (Links)**
  - Added `profileId` column with foreign key to `Profile` table
  - Default value is 1 (Default profile)

- **Migration: `7.sqm`**
  - Creates Profile table
  - Inserts default profile with ID 1
  - Adds profileId column to existing Deepr table

### 2. Database Queries
- **Profile Operations**: Added CRUD queries for profiles (insert, get, update, delete, count)
- **Scoped Queries**: Updated all link and tag queries to filter by profileId:
  - `getLinksAndTags`: Now takes profileId as first parameter
  - `getAllTagsWithCount`: Filters tags to only show those used in current profile
  - `countOfLinks`: Counts links for specific profile
  - `countOfFavouriteLinks`: Counts favourites for specific profile

### 3. Repository Layer
- **LinkRepository Interface**: Added profile management methods
- **LinkRepositoryImpl**: Implemented profile CRUD operations
- **Import Operations**: Updated all importers to accept profileId parameter

### 4. ViewModel Layer (AccountViewModel)
- **Profile State Management**:
  - `allProfiles`: StateFlow of all profiles
  - `currentProfile`: StateFlow of currently selected profile
  - `selectedProfileId`: Flows from preferences

- **Profile Methods**:
  - `setSelectedProfile()`: Switches to a different profile
  - `insertProfile()`: Creates new profile
  - `updateProfile()`: Renames profile
  - `deleteProfile()`: Deletes profile (prevents deletion of last profile)

- **Initialization**: Creates default profile if none exists on first run

- **Updated Queries**: All link queries now use selectedProfileId

### 5. UI Components

#### ProfileManagementScreen
- Full profile CRUD interface
- Shows all profiles with selection indicator
- Create new profiles with validation
- Edit profile names
- Delete profiles (with safeguards)
- Displays current profile

#### ProfileSelectorMenu
- Dropdown menu in home screen app bar
- Quick profile switching
- Shows all available profiles
- Link to profile management screen

#### Home Screen Integration
- Added ProfileSelectorMenu to app bar trailing icons
- Icon appears next to View Type and Sort buttons

### 6. Data Migration
- **Automatic Migration**: All existing links automatically assigned to "Default" profile (ID: 1)
- **Profile Creation**: Default profile created on first app launch if none exists
- **Safe Deletion**: Last profile cannot be deleted
- **Profile Switching**: When deleting current profile, automatically switches to another profile

### 7. Preferences
- **AppPreferenceDataStore**: Added `selectedProfileId` preference
- Default value: 1L (Default profile)
- Persists across app restarts

### 8. String Resources
Added localization strings for:
- Profile management UI
- Profile creation/deletion messages
- Error messages
- Tooltips and labels

### 9. Analytics Events
Added tracking for:
- `CREATE_PROFILE`
- `SWITCH_PROFILE`
- `DELETE_PROFILE`
- `EDIT_PROFILE`

## User Experience

### Creating a Profile
1. User navigates to Profile Management (via profile selector menu)
2. Enters profile name in the creation card
3. Clicks "+" button to create
4. Profile appears in the list

### Switching Profiles
1. User clicks the Folders icon in home screen app bar
2. Dropdown menu shows all profiles
3. User selects desired profile
4. App immediately switches context - showing only that profile's links and tags

### Managing Profiles
1. Each profile in the list has Edit and Delete buttons
2. Edit: Opens dialog to rename profile
3. Delete: Shows confirmation with warning about data loss
4. Cannot delete if it's the only profile

## Benefits

1. **Organization**: Users can separate work, personal, music, recipes, etc.
2. **Clean Interface**: Tags list only shows relevant tags for current context
3. **No Clutter**: Each profile maintains its own isolated data
4. **Easy Switching**: Quick access to switch between profiles
5. **Data Safety**: Migration ensures no data loss for existing users

## Technical Details

### Data Isolation
- Links in one profile are completely invisible to other profiles
- Tags are global but their counts are profile-specific
- Switching profiles instantly updates all queries via reactive flows

### Performance
- Efficient filtering via SQL WHERE clause on profileId
- Indexed foreign key for fast joins
- Reactive updates via StateFlow - no manual refresh needed

### Edge Cases Handled
- Prevents deletion of last profile
- Auto-switches when deleting current profile
- Creates default profile on first run
- Handles profile name conflicts
- Validates profile names (non-empty)

## Future Enhancements
Potential improvements for future iterations:
- Profile export/import
- Profile icons/colors
- Quick profile switching shortcut
- Profile-specific settings
- Profile templates
