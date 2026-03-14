mvn package 
cp target/discord-minecraft-bot*.jar Server/plugins/
cd Server
java -jar -Xmx6G server.jar nogui
cd ..