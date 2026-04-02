# Egypt Geo Data Import

This document explains how to import Egyptian regions and cities using the GeoDB Cities API.

## Prerequisites

1. **GeoDB API Key**: Get your API key from [RapidAPI](https://rapidapi.com/wirefreethought/api/geodb-cities)
2. **PostgreSQL Database**: Ensure your PostgreSQL database is running and accessible

## Environment Variables

Set the following environment variables:

```bash
# GeoDB Cities API Key (required)
export GEODB_API_KEY="your_rapidapi_key_here"

# Database Connection (if not already set)
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/mydb"
export SPRING_DATASOURCE_USERNAME="your_username"
export SPRING_DATASOURCE_PASSWORD="your_password"
```

## Database Setup

Run the SQL script to create the required tables:

```sql
-- Execute the script from database/create_geo_tables.sql
```

## API Endpoints

### Import Egyptian Geo Data
```http
POST /api/geo-data/import/egypt
```

**Response:**
```json
{
  "message": "Import completed successfully",
  "totalRegions": 27,
  "totalCities": 231
}
```

### Check Import Status
```http
GET /api/geo-data/import/status
```

**Response:**
```json
{
  "status": "Geo data import service is ready"
}
```

## Features

- **Batch Processing**: Uses batch inserts for optimal performance
- **Rate Limiting**: Implements automatic retry logic with exponential backoff
- **Duplicate Prevention**: Avoids duplicate entries using unique constraints
- **Error Handling**: Comprehensive error handling for API and database errors
- **Logging**: Detailed logging for monitoring and debugging
- **Pagination**: Automatically handles API pagination to fetch all cities

## Process Flow

1. **Fetch Regions**: Gets all Egyptian regions using country code "EG"
2. **Store Regions**: Saves regions to database with unique constraints
3. **Fetch Cities**: For each region, fetches all cities with pagination
4. **Store Cities**: Batch inserts cities with foreign key relationships
5. **Return Summary**: Provides total count of imported regions and cities

## Error Handling

- **API Rate Limits**: Automatic retry with exponential backoff
- **Network Errors**: Configurable retry attempts (3 by default)
- **Database Errors**: Transaction rollback on failure
- **Validation**: Skips invalid data (empty names, missing codes)

## Performance Optimizations

- **Batch Inserts**: Uses JPA `saveAll()` for bulk operations
- **Connection Pooling**: Configured HikariCP settings
- **Rate Limiting**: 100ms delay between API calls to respect limits
- **Caching**: Regions cached in memory during city import

## Testing

You can test the import using curl:

```bash
curl -X POST http://localhost:8080/api/geo-data/import/egypt \
  -H "Content-Type: application/json"
```

## Monitoring

Check application logs for detailed information:
- Region import progress
- City fetch counts per region
- Error details and retry attempts
- Performance metrics
