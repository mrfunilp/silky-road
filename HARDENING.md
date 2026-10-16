# Hardening

Security measures in the SilkyRoad Docker stack, and what each one protects against.

## Container hardening

These measures do not stop an attacker from getting in. They limit what an attacker can do after gaining code execution inside a container.

### Read-only filesystem

| | |
|---|---|
| **Applies to** | `frontend`, `backend` |
| **Where** | `read_only: true` in `docker-compose.yml` |
| **Writable exception** | `/tmp`, as an in-memory `tmpfs` that is emptied on every restart |

Protects against:

- **Tampering with served code.** The JavaScript bundle that nginx delivers to every user cannot be modified, so an attacker cannot inject code that steals users' login tokens.
- **Tampering with the application.** The backend's JAR file cannot be replaced or patched.
- **Persistence.** Nothing an attacker writes survives a container restart.
- **Bringing in tools.** Downloaded scripts or binaries can only land in `/tmp`, which is lost on restart.

### No root user

| | |
|---|---|
| **Applies to** | `frontend` (user `nginx`), `backend` (user `spring`) |
| **Where** | `frontend`: base image `nginxinc/nginx-unprivileged`. `backend`: `USER spring` in `backend/Dockerfile` |

Protects against:

- **Full control of the container.** A compromised process cannot install packages, change system files or read files owned by other users.
- **Container escape.** Most known techniques for breaking out of a container onto the host require root inside the container.

Side effect: a non-root process cannot bind ports below 1024, so nginx listens on 8080 inside the container.

## Application hardening

### Picture upload

| | |
|---|---|
| **Applies to** | `backend`: `POST /api/items/{id}/images`, `GET /api/images/{name}` |
| **Where** | `ImageStorageService`, `MarketplaceItemService`, `application.properties` |

| Measure | Protects against |
|---|---|
| Only JPEG, PNG, GIF, WebP, detected from the file content | Uploading scripts, HTML or SVG disguised as images |
| Every picture is re-encoded | Hidden payloads and metadata (e.g. GPS) inside image files |
| Max. 5 MB per file, 8000 px per side, 3 per item | Filling the disk, decompression bombs |
| Random file names chosen by the server | Path traversal (`../../`) and overwriting files |
| Only the owner can upload, JWT required | Adding pictures to someone else's listing |
| Served with fixed `Content-Type` and `nosniff` | The browser running a file as a page or script |
| Clients cannot set image URLs themselves | Listings that load images from attacker-controlled servers |

## Not hardened yet

TO DO - DB hardening 
