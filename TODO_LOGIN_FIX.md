# Login 400 Bad Request Fix TODO

## Issues Identified
- 400 Bad Request on /api/auth/login endpoint
- Database only contains dummy seller account, no regular user accounts
- Poor error handling (400 instead of 401 for auth failures)
- No logging for debugging login attempts
- No request validation

## Tasks
- [x] Add proper logging to AuthController.login method
- [x] Improve error handling (use 401 for authentication failures, 400 for bad requests)
- [x] Add request validation for login endpoint
- [x] Create test user account in database
- [x] Fix phone number validation in registration (remove + and country code)
- [x] Test login functionality
