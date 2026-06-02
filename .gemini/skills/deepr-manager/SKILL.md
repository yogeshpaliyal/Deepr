---
name: deepr-manager
description: Manage Deepr links, tags, and profiles via REST API. Use when asked to add, update, delete, or organize links on a Deepr local server instance (e.g., at http://192.168.178.140:8080).
---

# Deepr Manager

This skill provides a structured way to interact with the Deepr REST API for link management.

## Connection
The default server address is `http://192.168.178.140:8080`. Always use this base URL unless otherwise specified.

## Core Workflows

### Link Management
- **Add Link**: Use `POST /api/links`. Requires `link` (URL). Optional: `name`, `notes`, `tags` (array of `{id, name}`), and `profileId`.
- **Update Link**: Use `PUT /api/links/{id}`. Requires the full link object in the body.
- **Delete Link**: Use `DELETE /api/links/{id}`.
- **Increment Count**: Use `POST /api/links/increment-count?id={id}`.

### Profile & Tag Management
- **Profiles**: List (`GET /api/profiles`), Create (`POST /api/profiles`), Delete (`DELETE /api/profiles/{id}`).
- **Tags**: List (`GET /api/tags`), Delete (`DELETE /api/tags/{id}`).

## Reference Material
For detailed JSON schemas, request formats, and response examples, refer to [references/api_docs.md](references/api_docs.md).
