# Rally Server - Backend API

Spring Boot backend for the Rally pickup sports coordination platform.

## Technology Stack

- **Java 21**
- **Spring Boot 3.2**
- **Spring Security** - Session-based authentication
- **Spring Data MongoDB** - Database access
- **MongoDB** - NoSQL database (Docker)
- **Lombok** - Boilerplate reduction
- **Maven** - Build tool

## Prerequisites

- Java 21+
- Maven 3.6+ (or use included wrapper)
- Docker and Docker Compose (for MongoDB)

## Quick Start

```bash
./start.sh
```

This will:
1. Start MongoDB in Docker
2. Start the Spring Boot application
3. Load demo data

## Manual Start

### 1. Start MongoDB

```bash
docker-compose up -d mongodb
```

### 2. Start the Application

Using Maven wrapper:
```bash
./mvnw spring-boot:run
```

Or using system Maven:
```bash
mvn spring-boot:run
```

## Configuration

### application.properties

```properties
# Server
server.port=8080

# MongoDB
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=rallydb

# Logging
logging.level.com.rally=DEBUG
```

## Project Structure

```
src/main/java/com/rally/
├── config/           # Configuration classes
│   ├── SecurityConfig.java
│   └── DataInitializer.java
├── controller/       # REST controllers
│   ├── AuthController.java
│   ├── EventController.java
│   └── UserController.java
├── dto/             # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── CreateEventRequest.java
│   ├── UserResponse.java
│   ├── EventResponse.java
│   ├── ParticipantResponse.java
│   └── UserStatsResponse.java
├── model/           # MongoDB documents
│   ├── User.java
│   ├── Event.java
│   └── Participant.java
├── repository/      # MongoDB repositories
│   ├── UserRepository.java
│   ├── EventRepository.java
│   └── ParticipantRepository.java
├── service/         # Business logic
│   ├── AuthService.java
│   ├── EventService.java
│   └── UserService.java
└── RallyApplication.java  # Main application class
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout
- `GET /api/auth/me` - Get current user

### Events
- `GET /api/events` - Get all events
- `GET /api/events/{id}` - Get event by ID
- `POST /api/events` - Create event
- `POST /api/events/{id}/join` - Join event
- `POST /api/events/{id}/leave` - Leave event
- `POST /api/events/{id}/checkin` - Check-in to event

### Users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/{id}/stats` - Get user statistics
- `GET /api/users/{id}/events` - Get user's events

## MongoDB

### Access MongoDB Shell

```bash
docker exec -it rally-mongodb mongosh
```

### View Data

```javascript
use rallydb
db.users.find().pretty()
db.events.find().pretty()
db.participants.find().pretty()
```

### Mongo Express (Web UI)

Access at: http://localhost:8081
- Username: `admin`
- Password: `admin123`

## Demo Data

The application automatically loads demo data on startup:

**Users:**
- john@rally.com / password123
- jane@rally.com / password123

**Events:**
- Pickup Basketball
- Weekend Soccer Match
- Morning Pickleball

## Building

```bash
mvn clean package
```

The JAR file will be in `target/rally-server-1.0.0.jar`

## Running the JAR

```bash
java -jar target/rally-server-1.0.0.jar
```

## Testing

```bash
mvn test
```

## Logs

Application logs are written to `backend.log`

```bash
tail -f backend.log
```

## Troubleshooting

### MongoDB Connection Issues

1. Check MongoDB is running:
   ```bash
   docker ps | grep rally-mongodb
   ```

2. Test connection:
   ```bash
   docker exec -it rally-mongodb mongosh --eval "db.adminCommand('ping')"
   ```

3. Check logs:
   ```bash
   docker logs rally-mongodb
   ```

### Port Already in Use

```bash
lsof -ti:8080 | xargs kill -9
```

## Environment Variables

You can override configuration with environment variables:

```bash
export MONGODB_HOST=localhost
export MONGODB_PORT=27017
export MONGODB_DATABASE=rallydb
export SERVER_PORT=8080
```

## Production Deployment

For production, consider:

1. Use MongoDB Atlas (cloud-hosted)
2. Enable MongoDB authentication
3. Use environment-specific properties
4. Enable HTTPS
5. Configure CORS properly
6. Set up proper logging

## Documentation

- **MongoDB Setup**: See `MONGODB_SETUP.md`
- **API Documentation**: See `API.md` (if available)
- **Architecture**: See `../RallyUpUI/ARCHITECTURE.md`

## Support

For issues, check:
1. MongoDB is running: `docker ps`
2. Application logs: `tail -f backend.log`
3. MongoDB logs: `docker logs rally-mongodb`
