# 🕹️ Pong Multiplayer Network Game

This project is a secure, network-enabled multiplayer version of the classic **Pong** game. It incorporates cryptography to protect game files, authenticate players, and ensure secure communication. The game is built with **JavaFX/FXGL** and uses a modular structure with packages for gameplay, menus, and cryptographic operations.

---

## 📦 Project Structure

The project is organized into four main packages:

### 🔹 Default Package
- Contains the **networking logic** enabling multiplayer gameplay over a network.
- Manages communication between the client and server, and handles game logic.

### 🔹 `entities`
- Contains all game objects such as paddles, ball, and scoreboard.

### 🔹 `menus`
- Includes UI components and functionality for:
  - `PongMainMenu`: Start a new game, load a saved game, or exit.
  - `PongGameMenu`: Resume a game, save progress, or exit to the main menu.
- Both menus utilize cryptographic checks to prevent unauthorized access and data tampering.

### 🔹 `crypto`
- Handles all **cryptographic operations**, including:
  - Keystore creation
  - Symmetric and asymmetric key generation
  - Password hashing and validation
  - Game file encryption/decryption
  - Digital signing and signature verification of `PongApp.java`
- All security operations are executed on the **server** side to prevent local manipulation.

> 🛡️ **Security Note:** These cryptographic protections ensure that game integrity is maintained and unauthorized modifications are blocked.

---

## 🙏 Acknowledgements

This game utilizes **FXGL** (a JavaFX game development framework).  
Special thanks to **Mr. Almas Baimagambetov** for his open-source contributions that made this project possible.

---

## 🚀 How to Run the Game

### 🧱 1. Build the Project

- Open the project in **NetBeans** or your preferred Java IDE.
- Perform a **Clean and Build** operation.
- Delete any leftover cryptographic test artifacts:
  ```text
  keystore.p12
  signature
  gcmiv

### ⚙️ 2. Set Up Networking
Firstly, decide who will be server and who will be client, and execute the ipconfig command on the server’s computer.
Carefully mark down the IPv4 address displayed. Next, check if the path to the Java JDK is stored in your environment variables 
(run echo $PATH if you are on Linux). If it is not, run "export PATH=$PATH:/path/to/jdk" for a Linux machine, or edit environment 
variables for a Windows machine.

### ▶️ 4. Run the Game
You're ready to go!

Launch PongApp.java from your IDE or terminal.

On the server: select "Host Game"

On the client(s): enter the server's IP and connect

Enjoy secure and smooth Pong gameplay!
