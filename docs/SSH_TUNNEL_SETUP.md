# SSH Tunnel Setup for Database Connection

This document explains how to establish an SSH tunnel to connect to the remote PostgreSQL database.

## Overview

The application connects to PostgreSQL through `localhost:5432`, which requires an SSH tunnel to be established first. The tunnel forwards local port 5432 to the remote PostgreSQL server at `196.221.167.63:5432`.

## Connection Details

- **SSH Tunnel Host**: `196.221.167.63`
- **SSH Tunnel Port**: `22`
- **SSH Tunnel Username**: `home-server`
- **Local Port**: `5432`
- **Remote Database Port**: `5432`

## Setup Instructions

### Windows (PowerShell)

1. Open PowerShell as Administrator (if needed for port binding)
2. Run the following command:
   ```powershell
   ssh -L 5432:localhost:5432 home-server@196.221.167.63
   ```
3. Enter your SSH password when prompted
4. Keep the PowerShell window open while the application is running

### Windows (Command Prompt)

1. Open Command Prompt as Administrator
2. Run the following command:
   ```cmd
   ssh -L 5432:localhost:5432 home-server@196.221.167.63
   ```
3. Enter your SSH password when prompted
4. Keep the Command Prompt window open while the application is running

### Windows (Using PuTTY)

1. Download and open PuTTY
2. In the Session category:
   - Host Name: `home-server@196.221.167.63`
   - Port: `22`
3. Navigate to Connection → SSH → Tunnels
4. Add a new forwarded port:
   - Source port: `5432`
   - Destination: `localhost:5432`
   - Select "Local" radio button
5. Click "Add"
6. Go back to Session and click "Open"
7. Enter your SSH password when prompted
8. Keep PuTTY open while the application is running

### Linux / macOS

1. Open Terminal
2. Run the following command:
   ```bash
   ssh -L 5432:localhost:5432 home-server@196.221.167.63
   ```
3. Enter your SSH password when prompted
4. Keep the terminal window open while the application is running

### Using SSH Key Authentication (Recommended)

If you have SSH keys set up, you can use them instead of password:

```bash
ssh -i ~/.ssh/your_private_key -L 5432:localhost:5432 home-server@196.221.167.63
```

## Background SSH Tunnel (Linux/macOS)

To run the SSH tunnel in the background:

```bash
ssh -f -N -L 5432:localhost:5432 home-server@196.221.167.63
```

- `-f`: Run in background
- `-N`: Don't execute remote commands (just forward ports)

To stop the background tunnel, find the process:
```bash
ps aux | grep "ssh -L 5432"
kill <PID>
```

## Verifying the Tunnel

### Test Connection with psql

Once the tunnel is established, you can test the connection:

```bash
psql -h localhost -p 5432 -U admin -d mydb
```

### Test Connection with telnet

```bash
telnet localhost 5432
```

If the connection is successful, you should see a PostgreSQL connection response.

## Troubleshooting

### Port Already in Use

If you get an error that port 5432 is already in use:

**Windows:**
```powershell
netstat -ano | findstr :5432
taskkill /PID <PID> /F
```

**Linux/macOS:**
```bash
lsof -i :5432
kill -9 <PID>
```

### Connection Refused

- Verify the SSH tunnel is active and running
- Check that the remote PostgreSQL server is running
- Verify firewall rules allow connections on port 5432
- Ensure the SSH server allows port forwarding

### Authentication Failed

- Verify your SSH credentials are correct
- Check if SSH key authentication is required
- Ensure your user has permission to create port forwards

## Application Startup Sequence

1. **First**: Establish the SSH tunnel (keep it running)
2. **Then**: Start the Spring Boot application
3. The application will connect to `localhost:5432` which is forwarded through the tunnel

## Docker Considerations

If running the application in Docker and need SSH tunnel:

1. Use `host.docker.internal` instead of `localhost` (Windows/Mac)
2. Or use `--network host` mode in Docker
3. Or set up SSH tunnel service in docker-compose

Example docker-compose with SSH tunnel:
```yaml
version: '3.8'
services:
  ssh-tunnel:
    image: alpine/ssh-client
    command: ssh -N -L 5432:localhost:5432 home-server@196.221.167.63
    volumes:
      - ~/.ssh:/root/.ssh:ro
    network_mode: host
  
  app:
    build: .
    depends_on:
      - ssh-tunnel
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydb
```

## Security Notes

- Never commit SSH passwords or keys to version control
- Use SSH key authentication instead of passwords when possible
- Consider using SSH config file for easier management:
  ```
  Host db-tunnel
      HostName 196.221.167.63
      User home-server
      LocalForward 5432 localhost:5432
  ```
  Then connect with: `ssh db-tunnel`

