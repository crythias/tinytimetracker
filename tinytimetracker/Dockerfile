# SPDX-License-Identifier: GPL-2.0-only
FROM openjdk:25-slim

# Install X11 libraries
RUN apt-get update && apt-get upgrade -y && apt-get install -y libx11-6 libxext6 libxrender1 libxtst6 libxi6 libfreetype6 fontconfig-config libfontconfig1 && rm -rf /var/lib/apt/lists/*
 
# Create app directory
WORKDIR /app

# *** NOTE: ```./gradlew build``` should be run prior to making the Docker Image
RUN echo "***NOTE*** you should have ./gradelaw build before running this so it can be available in the docker image."

# Copy your JAR
COPY build/distributions/tinytimetracker-*.tar /app/
RUN tar -xf /app/tinytimetracker-*.tar -C /app --strip-components=1


# Set DISPLAY variable (host must allow X11 forwarding)
# ENV DISPLAY=:0
ENV DISPLAY=host.docker.internal:0 

# Run the app

#CMD ["tail", "-f", "/dev/null"]
CMD ["bin/tinytimetracker"]
