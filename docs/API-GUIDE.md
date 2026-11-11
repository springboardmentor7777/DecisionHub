# DecisionHub API guide

This guide walks through a typical API workflow: create an account, create a decision board, vote, and comment. It also explains the request format and common response fields.

For local development, the API base URL is `http://localhost:8080`. All routes in this guide are relative to that URL and use JSON unless noted otherwise.

## Before you start

Start the backend using the instructions in the [project README](../README.md) or the [backend setup guide](../backend/README.md).

Protected requests require the JWT returned by register or login. Add it as a bearer token:

```http
Authorization: Bearer <token>
```

In the examples, replace `<token>`, `<board-id>`, and `<option-id>` with the values from your actual responses. IDs are assigned by the server.

## 1. Register an account

`POST /api/auth/register` is public and creates an account. Passwords must be between 8 and 72 characters; the email must be valid.

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "alex@example.com",
  "password": "correct-horse-battery",
  "displayName": "Alex"
}
```

Successful response (`201 Created`):

```json
{
  "token": "<jwt>",
  "id": 12,
  "email": "alex@example.com",
  "displayName": "Alex",
  "role": "USER"
}
```

Save the token securely and use it in later protected requests. To sign in to an existing account instead, send the same `email` and `password` fields to `POST /api/auth/login` (`200 OK`).

## 2. Create a decision board

`POST /api/boards` requires authentication. A new board needs between 2 and 20 options.

```http
POST /api/boards
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Where should we hold the team offsite?",
  "description": "Choose a location for our next team gathering.",
  "category": "WORK",
  "visibility": "PUBLIC",
  "pollType": "SINGLE_CHOICE",
  "options": [
    {
      "name": "Mountains",
      "description": "A quiet lodge outside the city",
      "pros": "Scenic and relaxing",
      "cons": "Longer travel time",
      "score": 80
    },
    {
      "name": "Beach",
      "description": "A seaside venue",
      "pros": "Warm weather and outdoor activities",
      "cons": "Can be crowded",
      "score": 70
    }
  ]
}
```

The response (`201 Created`) contains the board and server-assigned IDs:

```json
{
  "id": 34,
  "title": "Where should we hold the team offsite?",
  "description": "Choose a location for our next team gathering.",
  "category": "WORK",
  "visibility": "PUBLIC",
  "pollType": "SINGLE_CHOICE",
  "ownerId": 12,
  "ownerName": "Alex",
  "createdAt": "2026-11-10T03:40:00Z",
  "options": [
    {
      "id": 71,
      "name": "Mountains",
      "description": "A quiet lodge outside the city",
      "pros": "Scenic and relaxing",
      "cons": "Longer travel time",
      "score": 80
    },
    {
      "id": 72,
      "name": "Beach",
      "description": "A seaside venue",
      "pros": "Warm weather and outdoor activities",
      "cons": "Can be crowded",
      "score": 70
    }
  ]
}
```

Use the returned board ID to open the board. Public boards can be read without signing in:

```http
GET /api/boards/34
```

Other useful board endpoints:

| Method | Route | Authentication | Purpose |
|---|---|---|---|
| `GET` | `/api/boards/public` | No | List public boards |
| `GET` | `/api/boards/mine` | Yes | List your boards |
| `GET` | `/api/boards/{id}` | No for public boards | Read a board; private boards are owner-only |
| `PUT` | `/api/boards/{id}` | Yes, owner only | Update board details |
| `POST` | `/api/boards/{id}/options` | Yes, owner only | Add an option |
| `DELETE` | `/api/boards/{id}` | Yes, owner only | Delete a board (`204 No Content`) |

## 3. Vote on an option

`POST /api/boards/{boardId}/votes` accepts anonymous votes without a token. To cast a vote associated with your account, include your token and set `anonymous` to `false`.

```http
POST /api/boards/34/votes
Authorization: Bearer <token>
Content-Type: application/json

{
  "optionId": 71,
  "anonymous": false
}
```

The response includes the total number of votes and counts for each option:

```json
{
  "boardId": 34,
  "totalVotes": 1,
  "votesByOption": {
    "71": 1,
    "72": 0
  }
}
```

For an anonymous vote, set `"anonymous": true`; the request can then be made without an `Authorization` header. The selected option must belong to the board. For `RATING` boards, provide a `rating` from 1 to 5; do not provide a rating for other poll types. `MULTIPLE_CHOICE` allows an authenticated user to vote for multiple options, but not to select the same option twice.

Read current totals with `GET /api/boards/{boardId}/votes`.

## 4. Read and add comments

Comments can be read on a public board without signing in. Adding a comment requires authentication.

```http
GET /api/boards/34/comments
```

To add a top-level comment:

```http
POST /api/boards/34/comments
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "The mountain option would be great for hiking.",
  "parentId": null
}
```

To reply, set `parentId` to the ID of an existing comment on the same board. The response includes the comment ID, author, timestamp, and any replies.

## Other API areas

- **Profile:** `GET` and `PUT /api/users/me`; update the password with `PUT /api/users/me/password`.
- **Communities:** `GET /api/communities` is public. Create, join, leave, or delete communities using the routes in the [backend API overview](../backend/README.md).
- **Notifications:** `GET /api/notifications` and `PATCH /api/notifications/{id}/read` require authentication.
- **Dashboard:** `GET /api/analytics/dashboard` requires authentication.

## Common errors

Errors are JSON objects with an `error` message. For example:

```json
{
  "error": "Only the board owner can modify this board"
}
```

| Status | Meaning |
|---|---|
| `400 Bad Request` | Invalid field, malformed request, or operation not allowed for the poll |
| `401 Unauthorized` | Missing/invalid authentication or incorrect login credentials |
| `403 Forbidden` | Signed-in user is not allowed to perform this operation |
| `404 Not Found` | Requested board, profile, or other resource does not exist |
| `409 Conflict` | Request conflicts with existing data, such as a duplicate account |

Validation errors return an `error` message naming the first invalid field. Make sure required fields are present and that enum values use the exact uppercase spelling shown in the examples.

## Related documentation

- [Project overview and quick start](../README.md)
- [Backend configuration and complete route overview](../backend/README.md)
