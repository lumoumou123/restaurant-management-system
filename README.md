# Restaurant Management System

A comprehensive restaurant management system with menu management and map integration.

## Features

- **Restaurant Management**: Add, edit, and delete restaurants
- **Menu Management**: Create and manage restaurant menus
- **Map Integration**: View restaurants on Google Maps
- **Map Pin Selection**: Select locations directly on the map
- **User Management**: Login, registration, and role-based access
- **Comments System**: Users can leave comments for restaurants
- **Rating System**: Rate restaurants and view average ratings

## Technologies

### Frontend
- Vue.js
- Element UI
- Google Maps API
- Axios

### Backend
- Spring Boot
- MyBatis-Plus
- MySQL Database

## Getting Started

### Prerequisites
- Node.js (v14+)
- Java JDK 11+
- MySQL 5.7+

### Installation

1. Clone the repository
```
git clone https://github.com/lumoumou123/restaurant-management-system.git
cd restaurant-management-system
```

2. Set up backend
```
cd canting
mvn install
```

3. Configure database
- Import SQL files from `canting/sql/` directory
- Update database connection in `application.properties`

4. Run backend server
```
mvn spring-boot:run
```

5. Set up frontend
```
cd ../big-data
npm install
```

6. Run frontend development server
```
npm run serve
```

7. Access the application at `http://localhost:8080`

## User Roles

- **Manager**: Can manage all restaurants, menus, and users
- **Owner**: Can manage their own restaurants and menus
- **Customer**: Can view restaurants, leave comments, and ratings

## License

[MIT License](LICENSE) 