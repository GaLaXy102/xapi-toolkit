// From https://dockerquestions.com/2020/12/07/docker-mongodb-init-script-read-value-from-env-file/
print('mongo init start --------------------');

const pms = db.getSiblingDB(_getEnv('MONGO_INITDB_DATABASE'));

pms.createUser({
    user: _getEnv('MONGO_INITDB_USERNAME'),
    pwd: _getEnv('MONGO_INITDB_PASSWORD'),
    roles: [
        {
            role: 'readWrite',
            db: _getEnv('MONGO_INITDB_DATABASE'),
        },
    ],
});

print('mongo init end --------------------');