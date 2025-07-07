# Collaborative-Graphical-Editor

This project is a collaborative graphical editor, similar to how Google Docs allows multiple users to edit a document simultaneously. Here, multiple clients connect to a central server, and any drawing action performed by one client is instantly reflected on all others.

The editor supports multiple drawable objects, including rectangles, ellipses, line segments, and freehand shapes.

### System architecture:

#### Clients: 

1. Each client runs two threads:

2. A network thread to communicate with the sketch server.

3. A UI thread for handling user input and drawing interactions.

#### Server:

1. A main thread handles incoming client connections.

2. Separate client handler threads manage communication with each connected client.

3. When a client draws something, it sends the drawing action to the server. The server then broadcasts this update to all connected clients, ensuring everyone sees a consistent, shared sketch.
