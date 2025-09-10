# MCP Server

This project is an MCP (Management Control Platform) server built with Spring AI. It provides tools and APIs to help you manage Docker images and containers efficiently.

## Features

- Manage Docker images (list, pull, remove)
- Control Docker containers (start, stop, exists)
- RESTful API endpoints for automation and integration
- Powered by Spring AI for intelligent operations and the Docker Model Runner

## Getting Started

### Prerequisites

- Java 17+
- Docker installed and running
- Docker model installed and enabled
- Maven or Gradle

### Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/asavvop/docker-mcp-server.git
    cd docker-mcp-server
    ```
2. Build the project:
    ```bash
    ./mvnw clean install
    ```
3. Pull a docker model:
    ```bash
    docker model pull ai/gemma3
    ```
3. Run the server:
    ```bash
    ./mvnw spring-boot:run
    ```

## Contributing

Contributions are welcome! Please open issues or submit pull requests for improvements.

## License

This project is licensed under the GNU General Public License (GPL).
