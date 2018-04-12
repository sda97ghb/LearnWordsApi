# LearnWordsApi
Server that provides API for LearnWords project.

# Build

## Install MongoDB
Full instruction is here:
https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/

Following script should be enough:
```bash
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2930ADAE8CAF5059EE73BB4B58712A2291FA4AD5
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/3.6 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.6.list
sudo apt-get update
sudo apt-get install -y mongodb-org
sudo service mongod start
```

## Clone
```bash
git clone https://github.com/sda97ghb/LearnWordsApi.git
cd LearnWordsApi
```

## Build
```bash
mvn package spring-boot:repackage
```

## Run
```bash
./run.sh
```
or
```bash
sudo service mongod start
java -jar target/LearnWordsApi-1.0-SNAPSHOT.jar
```
