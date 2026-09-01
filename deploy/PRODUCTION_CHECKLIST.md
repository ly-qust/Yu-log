# Yu-log v1.0 Production Checklist

This checklist is the deployment-time companion to the repository-level engineering audit.

- [ ] Domain DNS and firewall rules are configured.
- [ ] HTTPS certificate and renewal are verified.
- [ ] Production `.env`/Secret values are injected outside Git.
- [ ] Database, Redis, JWT, admin, and proxy variables are all present.
- [ ] `docker compose config` passes without placeholder secrets.
- [ ] Database backup was created and restored into a temporary database.
- [ ] Flyway history is recorded; existing databases were baselined only after schema comparison.
- [ ] `SPRING_PROFILES_ACTIVE=prod` is active.
- [ ] Admin password was initialized from deployment Secret and changed if required.
- [ ] CORS origin matches the public HTTPS origin.
- [ ] Nginx forwarding headers and trusted proxy CIDRs are correct.
- [ ] Security headers, CSP, upload limits, and persistent volumes are verified.
- [ ] Health, logs, backup schedule, and rollback owner are documented.
- [ ] Public smoke test and Admin smoke test are complete.
