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

## Not hardened yet

TO DO - DB hardening 
