db.createUser({
    "user": "cinema_user",
    "pwd": "password",
    "roles": [
        {
            "role": "readWrite",
            "db": "cinema"
        }
    ]
});
