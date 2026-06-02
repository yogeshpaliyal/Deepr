# Deepr API Reference

## Base URL
`http://192.168.178.140:8080`

## Endpoints

### Links

#### GET /api/links
Retrieve all links.
- **Query Params**: `profileId` (Optional, Long)

#### POST /api/links
Add a new link.
- **Body (JSON)**:
```json
{
  "link": "https://example.com",
  "name": "Example Name",
  "notes": "Optional notes",
  "tags": [{"id": 0, "name": "tag"}],
  "profileId": 1
}
```

#### PUT /api/links/{id}
Update an existing link.
- **Body (JSON)**: Same as POST.

#### DELETE /api/links/{id}
Delete a link by ID.

#### POST /api/links/increment-count
Increment opened count for a link.
- **Query Params**: `id` (Required, Long)

### Profiles

#### GET /api/profiles
Get all available profiles.

#### POST /api/profiles
Create a new profile.
- **Body (JSON)**:
```json
{
  "name": "New Profile Name"
}
```

#### DELETE /api/profiles/{id}
Delete a profile by ID.

### Tags

#### GET /api/tags
Get all available tags.
- **Query Params**: `profileId` (Optional, Long)

#### DELETE /api/tags/{id}
Delete a tag by ID.

### Metadata

#### GET /api/link-info
Get metadata (title, image) for a URL.
- **Query Params**: `url` (Required, String)

#### GET /api/server-info
Get server status and this documentation programmatically.
