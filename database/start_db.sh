#!/bin/bash

# Start the database
sudo docker run -d --name catan-ai-db-container -p 5432:5432 -v ./data:/var/lib/postgresql/data catan-ai-db