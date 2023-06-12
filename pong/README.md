# PongNetworkGame Structure
The Pong Multiplayer Network Game is divided into four main packages: default package, entities, menus, and crypto. Each folder contains
important parts of the program, particularly the crypto classes which are responsible for creating the keystore, generating symmetric and
asymmetric keys, signing the PongApp.java file, validating and hashing the user’s password, as well as encrypting and decrypting the game
file. The default package contains all classes required to allow multiplayer service over a network. The menus package, on the other hand,
contains the two menus that will be displayed frequently to the user. The PongMainMenu holds methods to start a new game, load and start 
a game and exit from the menu, while the PongGameMenu holds methods to resume the game, save it or exit from it. Both use cryptographic
methods to ensure that the users have permission to access game files and nothing is altered in the source code that can lead to security
vulnerabilities (all tasks are run on the server). 

Most of the frontend classes implement FXGL and GameApplication functionality. Therefore, we would like to give a special thanks to 
Mr. Baimagambetov for his contribution to open source software.

## Procedure to run the program

### Build
Open the project in netbeans, clean and build the application. Delete the keystore.p12, signature and gcmiv files generated from
the tests.

### Setup
Firstly, decide who will be server and who will be client, and execute the ipconfig command on the server’s compute.
Carefully mark down the IPv4 address displayed. Next, check if the path to the Java JDK is stored in your environment variables 
(run echo $PATH if you are on Linux). If it is not, run "export PATH=$PATH:/path/to/jdk" for a Linux machine, or edit environment 
variables for a Windows machine.

### Running the program
Now you are all set! Run the Pong application.