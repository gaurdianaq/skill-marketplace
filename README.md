# skill-marketplace

## Frontend
1. Ensure NPM and Node are installed
2. npm install (in the client folder)
3. npm run start (in the client folder)

## Backend

1. Ensure maven and a java development environment are installed.
2. Ensure you have two postgres databases setup (one for running the app, one for tests)
3. Run `psql -d yourdatabasename -a -f db.sql` from the skill-marketplace folder for both the main db and test db
3. Create .env file with following format
```
DB_NAME=skill_marketplace
DB_USER=gaurdianaq
DB_PASSWORD=password
DB_HOST=localhost

TEST_DB_NAME=skill_marketplace_test
TEST_DB_USER=gaurdianaq
TEST_DB_PASSWORD=password
TEST_DB_HOST=localhost

SECRET_KEY=5#KAp)3Tr!QEtk'`+Mj3Gq4m%=x;7]vjRU#:{Yh8`Xg^wl2x^R:Gto)7at(iG;v
```
4. Run mvn package in server folder
5. Copy .env file to target folder
6. run `java -jar skill_marketplace-1.0-SNAPSHOT-jar-with-dependencies.jar`