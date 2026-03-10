# Rally - MongoDB Setup Guide

## Overview

The Rally application now uses **MongoDB** as its database instead of H2. MongoDB runs in a Docker container for easy setup and management.

---

## Prerequisites

- **Docker** installed and running
- **Docker Compose** installed

### Install Docker

**macOS:**
```bash
brew install --cask docker
# Or download from https://www.docker.com/products/docker-desktop
```

**Ubuntu/Linux:**
```bash
sudo apt-get update
sudo apt-get install docker.io docker-compose
sudo systemctl start docker
sudo systemctl enable docker
```

**Windows:**
Download and install Docker Desktop from https://www.docker.com/products/docker-desktop

---

## Quick Start

### Option 1: Automatic Startup (Recommended)

The `start.sh` script now automatically starts MongoDB:

```bash
./start.sh
```

This will:
1. Start MongoDB in Docker
2. Wait for MongoDB to be ready
3. Start the backend (which will connect to MongoDB)
4. Start the frontend

### Option 2: Manual MongoDB Startup

If you want to start MongoDB separately:

```bash
# Start MongoDB
docker-compose up -d mongodb

# Verify it's running
docker ps | grep rally-mongodb

# Start the application
./start.sh
```

---

## MongoDB Configuration

### Docker Compose Configuration

The `docker-compose.yml` file defines:

- **MongoDB**: Latest version on port 27017
- **Mongo Express**: Web UI on port 8081 (optional)
- **Persistent Storage**: Data persists across container restarts

### Application Configuration

In `src/main/resources/application.properties`:

```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=rallydb
```

---

## Accessing MongoDB

### Via Mongo Express (Web UI)

1. Start the services:
   ```bash
   docker-compose up -d
   ```

2. Open browser to: http://localhost:8081

3. Login credentials:
   - Username: `admin`
   - Password: `admin123`

4. Select database: `rallydb`

### Via MongoDB Compass (Desktop App)

1. Download from: https://www.mongodb.com/products/compass

2. Connection string:
   ```
   mongodb://localhost:27017/rallydb
   ```

### Via MongoDB Shell

```bash
# Connect to MongoDB container
docker exec -it rally-mongodb mongosh

# Use the Rally database
use rallydb

# Show collections
show collections

# Query users
db.users.find().pretty()

# Query events
db.events.find().pretty()

# Query participants
db.participants.find().pretty()
```

---

## Data Management

### View Demo Data

After starting the application, demo data is automatically loaded:

```bash
# Connect to MongoDB
docker exec -it rally-mongodb mongosh

# Switch to Rally database
use rallydb

# View users
db.users.find().pretty()

# View events
db.events.find().pretty()

# View participants
db.participants.find().pretty()
```

### Clear All Data

```bash
# Connect to MongoDB
docker exec -it rally-mongodb mongosh

# Switch to Rally database
use rallydb

# Drop all collections
db.users.drop()
db.events.drop()
db.participants.drop()

# Restart the backend to reload demo data
```

### Backup Data

```bash
# Backup all data
docker exec rally-mongodb mongodump --db rallydb --out /data/backup

# Copy backup to host
docker cp rally-mongodb:/data/backup ./mongodb-backup
```

### Restore Data

```bash
# Copy backup to container
docker cp ./mongodb-backup rally-mongodb:/data/backup

# Restore data
docker exec rally-mongodb mongorestore --db rallydb /data/backup/rallydb
```

---

## Docker Commands

### Start MongoDB

```bash
docker-compose up -d mongodb
```

### Stop MongoDB

```bash
docker-compose down
```

### Stop and Remove Data

```bash
docker-compose down -v
```

### View Logs

```bash
docker logs rally-mongodb
docker logs -f rally-mongodb  # Follow logs
```

### Restart MongoDB

```bash
docker restart rally-mongodb
```

### Check Status

```bash
docker ps | grep rally-mongodb
```

---

## Troubleshooting

### MongoDB Won't Start

**Check if port 27017 is in use:**
```bash
lsof -ti:27017
```

**Kill process using port:**
```bash
lsof -ti:27017 | xargs kill -9
```

**Check Docker logs:**
```bash
docker logs rally-mongodb
```

### Backend Can't Connect

**Verify MongoDB is running:**
```bash
docker ps | grep rally-mongodb
```

**Check connection from host:**
```bash
docker exec -it rally-mongodb mongosh --eval "db.adminCommand('ping')"
```

**Verify application.properties:**
```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=rallydb
```

### Data Not Persisting

Data is stored in Docker volumes. To check:

```bash
docker volume ls | grep rally
```

To remove volumes (WARNING: deletes all data):
```bash
docker-compose down -v
```

---

## Migration from H2

The application has been fully migrated from H2 to MongoDB:

### Changes Made

1. **Dependencies**: Replaced `spring-boot-starter-data-jpa` with `spring-boot-starter-data-mongodb`
2. **Models**: Changed from JPA entities to MongoDB documents
3. **IDs**: Changed from `Long` to `String` (MongoDB ObjectId)
4. **Repositories**: Changed from `JpaRepository` to `MongoRepository`
5. **Relationships**: Changed from JPA relationships to document references

### Key Differences

| Feature | H2 (Old) | MongoDB (New) |
|---------|----------|---------------|
| Database Type | Relational | Document |
| ID Type | Long | String |
| Persistence | In-memory | Persistent |
| Relationships | Foreign Keys | Document References |
| Queries | JPQL | MongoDB Query Language |

---

## Production Considerations

For production deployment:

1. **Use MongoDB Atlas** (cloud-hosted)
   - Connection string: `mongodb+srv://...`
   - Update `application.properties`

2. **Enable Authentication**
   ```properties
   spring.data.mongodb.username=rallyuser
   spring.data.mongodb.password=securepassword
   spring.data.mongodb.authentication-database=admin
   ```

3. **Use Environment Variables**
   ```bash
   export MONGODB_HOST=your-mongodb-host
   export MONGODB_PORT=27017
   export MONGODB_DATABASE=rallydb
   ```

4. **Enable SSL/TLS**
   ```properties
   spring.data.mongodb.uri=mongodb://host:port/db?ssl=true
   ```

---

## Resources

- **MongoDB Documentation**: https://docs.mongodb.com/
- **Spring Data MongoDB**: https://spring.io/projects/spring-data-mongodb
- **Docker Documentation**: https://docs.docker.com/
- **Mongo Express**: https://github.com/mongo-express/mongo-express

---

## Support

For issues:
1. Check Docker is running: `docker ps`
2. Check MongoDB logs: `docker logs rally-mongodb`
3. Check backend logs: `tail -f backend.log`
4. Verify connection: `docker exec -it rally-mongodb mongosh`

