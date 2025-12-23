# 🐳 Docker Deployment Guide

This guide will help you run the Chat App using Docker and Docker Compose.

## 📋 Prerequisites

- Docker Desktop installed ([Download here](https://www.docker.com/products/docker-desktop))
- Docker Compose (included with Docker Desktop)
- At least 2GB of free RAM
- Ports 8080 and 5432 available

## 🚀 Quick Start

### 1. Build and Run with Docker Compose

```bash
# Navigate to the backend directory
cd backend

# Build and start all services
docker-compose up --build

# Or run in detached mode (background)
docker-compose up -d --build
```

### 2. Access the Application

- **Backend API**: http://localhost:8080
- **PostgreSQL Database**: localhost:5432
- **Health Check**: http://localhost:8080/actuator/health

### 3. Stop the Application

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This will delete all data!)
docker-compose down -v
```

## 🔧 Advanced Usage

### Build Only the Backend Image

```bash
docker build -t chat-app-backend .
```

### Run Backend Container Manually

```bash
docker run -d \
  --name chat-app-backend \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/chat_app \
  -e SPRING_DATASOURCE_USERNAME=chatapp_user \
  -e SPRING_DATASOURCE_PASSWORD=chatapp_password \
  chat-app-backend
```

### View Logs

```bash
# View all logs
docker-compose logs

# Follow logs in real-time
docker-compose logs -f

# View logs for specific service
docker-compose logs -f backend
docker-compose logs -f postgres
```

### Execute Commands in Running Container

```bash
# Access backend container shell
docker-compose exec backend sh

# Access PostgreSQL
docker-compose exec postgres psql -U chatapp_user -d chat_app
```

## 🔐 Environment Variables

You can customize the deployment by creating a `.env` file:

```bash
# Copy the example file
cp .env.example .env

# Edit the .env file with your values
```

### Important Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_DB` | Database name | chat_app |
| `POSTGRES_USER` | Database username | chatapp_user |
| `POSTGRES_PASSWORD` | Database password | chatapp_password |
| `APPLICATION_SECURITY_JWT_SECRET_KEY` | JWT secret key | (see .env.example) |
| `SPRING_PROFILES_ACTIVE` | Spring profile | docker |

## 📊 Monitoring

### Health Checks

Both services have health checks configured:

```bash
# Check service health
docker-compose ps

# Manual health check
curl http://localhost:8080/actuator/health
```

### Resource Usage

```bash
# View resource usage
docker stats

# View specific container stats
docker stats chat-app-backend
```

## 🐛 Troubleshooting

### Port Already in Use

If port 8080 or 5432 is already in use, modify the ports in `docker-compose.yml`:

```yaml
services:
  backend:
    ports:
      - "8081:8080"  # Change 8081 to any available port
  
  postgres:
    ports:
      - "5433:5432"  # Change 5433 to any available port
```

### Database Connection Issues

1. Ensure PostgreSQL is healthy:
   ```bash
   docker-compose ps
   ```

2. Check PostgreSQL logs:
   ```bash
   docker-compose logs postgres
   ```

3. Verify connection from backend:
   ```bash
   docker-compose exec backend sh
   # Inside container:
   apk add postgresql-client
   psql -h postgres -U chatapp_user -d chat_app
   ```

### Application Won't Start

1. Check backend logs:
   ```bash
   docker-compose logs backend
   ```

2. Verify environment variables:
   ```bash
   docker-compose config
   ```

3. Rebuild without cache:
   ```bash
   docker-compose build --no-cache
   docker-compose up
   ```

### Out of Memory

Increase Docker Desktop memory allocation:
- Open Docker Desktop Settings
- Go to Resources → Advanced
- Increase Memory to at least 4GB

## 🧹 Cleanup

### Remove All Containers and Images

```bash
# Stop and remove containers
docker-compose down

# Remove images
docker rmi chat-app-backend postgres:16-alpine

# Remove all unused images, containers, and volumes
docker system prune -a --volumes
```

### Remove Only Data Volumes

```bash
# Remove volumes (WARNING: This deletes all data!)
docker-compose down -v
```

## 🔄 Update and Redeploy

```bash
# Pull latest code
git pull

# Rebuild and restart
docker-compose up -d --build

# Or rebuild specific service
docker-compose up -d --build backend
```

## 📝 Production Considerations

For production deployment, consider:

1. **Use External Database**: Don't use the containerized PostgreSQL for production
2. **Secrets Management**: Use Docker secrets or environment variable injection
3. **Reverse Proxy**: Use Nginx or Traefik in front of the application
4. **SSL/TLS**: Configure HTTPS
5. **Logging**: Configure centralized logging (ELK stack, etc.)
6. **Monitoring**: Add Prometheus and Grafana
7. **Backup**: Implement database backup strategy

### Example Production docker-compose.yml

```yaml
version: '3.8'

services:
  backend:
    image: your-registry/chat-app-backend:latest
    restart: always
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://your-db-host:5432/chat_app
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_PROFILES_ACTIVE: prod
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
```

## 🎯 Next Steps

- Configure CI/CD pipeline for automated builds
- Set up container orchestration (Kubernetes, Docker Swarm)
- Implement monitoring and alerting
- Configure automated backups
- Set up load balancing

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)

## 🆘 Support

If you encounter any issues:

1. Check the logs: `docker-compose logs -f`
2. Verify configuration: `docker-compose config`
3. Check service health: `docker-compose ps`
4. Review this guide's troubleshooting section

---

**Happy Dockerizing! 🐳**
