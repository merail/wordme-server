# Server configuration

Files from the production VPS (Ubuntu 24.04).

| Repository                               | Server                                    |
|------------------------------------------|-------------------------------------------|
| `nginx/conf.d/wordme.conf`               | `/etc/nginx/conf.d/wordme.conf`           |
| `nginx/sites-available/wordme`           | `/etc/nginx/sites-available/wordme`, symlinked from `sites-enabled` |
| `nginx/secrets/*.map.example`            | `/etc/nginx/secrets/*.map` (root, `700` dir, `600` files) |
| `systemd/wordme.service`                 | `/etc/systemd/system/wordme.service`      |
| `backup/backup_sqlite.sh`                | `/opt/wordme/backup_sqlite.sh`            |
| `backup/sqlite-backups.cron`             | a line in `/etc/cron.d/sqlite-backups`, shared with another site |

## Secrets

Token values are never committed. Each `/etc/nginx/secrets/*.map` holds one line in the format of its `.example` file.

## Shared parts of `/etc/nginx/nginx.conf`

Not in the repository, since they are shared with another site on the host:

- a `geo` block that marks trusted source IPs;
- a shared `limit_req_zone` used by both sites.

## Access rules

- `/dayWord`, `/gameCountdownStartDate`, `/isWordExist` return 403 without a valid `X-WordMe-Token`.
- Ktor listens on `127.0.0.1:8081` only, so it is reachable through nginx alone.

## Applying changes

```sh
scp deploy/nginx/sites-available/wordme wordme:/etc/nginx/sites-available/wordme
ssh wordme 'nginx -t && systemctl reload nginx'
```

Back up the server file first (`<file>.bak-<date>` next to it).
